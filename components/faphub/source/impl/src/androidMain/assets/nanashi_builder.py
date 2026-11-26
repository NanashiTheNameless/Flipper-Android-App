"""Flipper-Nanashi's versioned Termux build worker. No shell interpolation of source input.

Toolchain compatibility approach documented by:
https://github.com/DarkFlippers/unleashed-firmware/blob/dev/documentation/BuildingFapsOnAndroid.md
"""
import ast
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import selectors
import shlex
import shutil
import signal
import subprocess
import sys
import tempfile
import time
import urllib.request
import urllib.parse
import zipfile

PROTOCOL = 1
UFBT_VERSION = "0.2.6"
MAX_EXPANDED = 256 * 1024 * 1024


def extract(archive, destination):
    total = 0
    seen = set()
    root = destination.resolve()
    with zipfile.ZipFile(archive) as source:
        entries = source.infolist()
        if not 0 < len(entries) <= 20000:
            raise ValueError("Empty archive or too many files")
        for entry in entries:
            name = entry.filename
            if (name.startswith("/") or "\\" in name or ":" in name or "\x00" in name
                    or ".." in Path(name).parts or len(name) > 2048):
                raise ValueError("Unsafe archive path")
            target = (root / name).resolve()
            if root not in target.parents or target in seen:
                raise ValueError("Unsafe or duplicate archive entry")
            seen.add(target)
            if entry.is_dir():
                target.mkdir(parents=True, exist_ok=True)
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            length = 0
            with source.open(entry) as stream, target.open("wb") as output:
                while True:
                    data = stream.read(65536)
                    if not data:
                        break
                    length += len(data)
                    total += len(data)
                    if total > MAX_EXPANDED or length > 64 * 1024 * 1024:
                        raise ValueError("Archive expands beyond limits")
                    output.write(data)


def app_description(project):
    """Discover only literal app metadata; uFBT executes the selected manifest later."""
    syntax = ast.parse((project / "application.fam").read_text())
    apps = []
    for node in ast.walk(syntax):
        if not isinstance(node, ast.Call) or not isinstance(node.func, ast.Name) or node.func.id != "App":
            continue
        kwargs = {item.arg: item.value for item in node.keywords}
        kind = kwargs.get("apptype")
        if not isinstance(kind, ast.Attribute) or kind.attr != "EXTERNAL":
            continue
        app_id = ast.literal_eval(kwargs["appid"])
        if not isinstance(app_id, str) or not re.fullmatch(r"[A-Za-z0-9_-]{1,80}", app_id):
            raise ValueError("App ID must contain only letters, digits, underscores or hyphens")
        category = ast.literal_eval(kwargs["fap_category"]) if "fap_category" in kwargs else "Tools"
        for value in (app_id, category):
            if not isinstance(value, str) or not 0 < len(value) <= 80 or value in (".", "..") or any(
                char in "/\\\r\n\x00" or ord(char) < 32 for char in value
            ):
                raise ValueError("Invalid app ID or category")
        apps.append((app_id, category))
    if len(apps) != 1:
        raise ValueError("Select a project with one standard external App and literal appid/category")
    return apps[0]


def wrap_toolchain(root, prefix):
    loader = prefix / "glibc/lib/ld-linux-aarch64.so.1"
    if not loader.is_file():
        raise RuntimeError("Run builder setup: glibc-runner is missing")
    for executable in (root / "bin").glob("python3*"):
        if executable.is_file() or executable.is_symlink():
            executable.unlink()
            executable.symlink_to(prefix / "bin/python3")
    candidates = list((root / "bin").glob("arm-none-eabi-*"))
    candidates += list((root / "arm-none-eabi/bin").glob("*"))
    candidates += list((root / "libexec/gcc/arm-none-eabi").glob("*/*"))
    for executable in candidates:
        if not executable.is_file() or executable.is_symlink() or ".so" in executable.name or executable.name.endswith(".real"):
            continue
        original = Path(str(executable) + ".real")
        if original.exists():
            continue
        with executable.open("rb") as stream:
            if stream.read(4) != b"\x7fELF":
                continue
        executable.rename(original)
        executable.write_text(
            "#!" + str(prefix / "bin/bash") + "\nexec " + shlex.quote(str(loader))
            + " --library-path " + shlex.quote(str(loader.parent) + ":" + str(root / "lib"))
            + " " + shlex.quote(str(original)) + ' "$@"\n'
        )
        executable.chmod(0o700)


class Broker:
    def __init__(self, url, token):
        if not url.startswith("http://127.0.0.1:"):
            raise ValueError("Invalid artifact broker")
        self.url, self.token = url, token

    def request(self, path, data=None, headers=None):
        request = urllib.request.Request(self.url + path, data=data, headers={
            "Authorization": "Bearer " + self.token, **(headers or {})
        })
        return urllib.request.urlopen(request, timeout=20)

    def report(self, text):
        with self.request("/log", str(text).encode("utf-8", "replace")[-16384:]):
            pass

    def download(self, route, output, expected):
        digest = hashlib.sha256()
        length = 0
        with self.request(route) as stream, output.open("wb") as target:
            while True:
                data = stream.read(65536)
                if not data:
                    break
                length += len(data)
                if length > 128 * 1024 * 1024:
                    raise ValueError("Input exceeds download limit")
                digest.update(data)
                target.write(data)
        if digest.hexdigest() != expected:
            raise ValueError("Input hash does not match pinned recipe")


def run(command, directory, environment, broker, allow_failure=False):
    broker.report("\nRunning " + Path(command[0]).name + " " + " ".join(command[1:])[:512] + "\n")
    process = subprocess.Popen(command, cwd=directory, env=environment, stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT, start_new_session=True)
    selector = selectors.DefaultSelector()
    selector.register(process.stdout, selectors.EVENT_READ)
    deadline = time.monotonic() + 30 * 60
    last_check = 0
    try:
        while True:
            if time.monotonic() > deadline:
                raise TimeoutError("Build command exceeded 30 minutes")
            if time.monotonic() - last_check > 2:
                with broker.request("/state") as response:
                    if json.load(response)["cancelled"]:
                        raise RuntimeError("Build cancelled")
                last_check = time.monotonic()
            for key, _ in selector.select(timeout=1):
                data = os.read(key.fileobj.fileno(), 4096)
                if data:
                    broker.report(data.decode("utf-8", "replace"))
                else:
                    selector.unregister(key.fileobj)
            if process.poll() is not None and not selector.get_map():
                break
        if process.returncode and not allow_failure:
            raise RuntimeError("Build command exited with status " + str(process.returncode))
    finally:
        selector.close()
        # Kill the whole build process group, including compilers or manifest-spawned children.
        try:
            os.killpg(process.pid, signal.SIGTERM)
            time.sleep(0.1)
            os.killpg(process.pid, signal.SIGKILL)
        except ProcessLookupError:
            pass
        process.wait()
        process.stdout.close()


def build(broker):
    with broker.request("/recipe") as response:
        recipe = json.load(response)
    if recipe["protocol"] != PROTOCOL:
        raise RuntimeError("Builder protocol mismatch")
    if platform.machine() not in ("aarch64", "arm64"):
        raise RuntimeError("The Beta builder requires an ARM64 Android phone")
    from importlib.metadata import version
    if version("ufbt") != UFBT_VERSION:
        raise RuntimeError("Run setup to install uFBT " + UFBT_VERSION)
    prefix = Path("/data/data/com.termux/files/usr")
    cache = Path.home() / ".flipper-nanashi-builder"
    cache.mkdir(mode=0o700, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="job-", dir=cache) as temporary:
        workspace = Path(temporary)
        source_zip, sdk_zip = workspace / "source.zip", workspace / "sdk.zip"
        broker.download("/source", source_zip, recipe["sourceSha256"])
        broker.download("/sdk", sdk_zip, recipe["sdkSha256"])
        source = workspace / "source"
        source.mkdir()
        extract(source_zip, source)
        project = (source / recipe["project"]).resolve()
        if source.resolve() not in project.parents and project != source.resolve():
            raise ValueError("Invalid project selection")
        if not (project / "application.fam").is_file():
            raise ValueError("Selected application.fam is missing")
        for file in source.rglob(".gitmodules"):
            if file.read_text().strip():
                raise ValueError("This project uses submodules. Supply a complete source ZIP instead.")
        for file in source.rglob("*"):
            if file.is_file() and file.stat().st_size < 1024:
                with file.open("rb") as stream:
                    if stream.read(80).startswith(b"version https://git-lfs.github.com/spec/v1"):
                        raise ValueError("Source archive contains unresolved Git LFS files")
        # Source-controlled dotenv files must never redirect the SDK or toolchain.
        for file in source.rglob(".env"):
            file.unlink()
        app_id, category = app_description(project)
        state = cache / ("sdk-" + recipe["sdkSha256"])
        environment = {key: value for key, value in os.environ.items() if key not in (
            "LD_PRELOAD", "PYTHONPATH", "PYTHONHOME", "UFBT_HOME", "UFBT_STATE_DIR", "FBT_TOOLCHAIN_PATH"
        )}
        environment.update(UFBT_HOME=str(state), FBT_TOOLCHAIN_PATH=str(cache / "tools"),
                           PATH=str(prefix / "bin") + ":" + environment.get("PATH", ""))
        ufbt = str(prefix / "bin/ufbt")
        run([ufbt, "update", "--local", str(sdk_zip), "--hw-target", "f7"], workspace, environment, broker)
        # The first invocation bootstraps GCC; its glibc Python may fail until wrapped.
        run([ufbt, "-h"], workspace, environment, broker, allow_failure=True)
        wrap_toolchain(cache / "tools/toolchain/aarch64-linux", prefix)
        compiler = cache / "tools/toolchain/aarch64-linux/bin/arm-none-eabi-gcc"
        compiler_version = subprocess.check_output([str(compiler), "--version"], env=environment, timeout=20).decode("utf-8", "replace").splitlines()[0][:180]
        # A fresh workspace guarantees that an old dist/ file cannot impersonate a successful build.
        for directory in (project / "dist", project / ".ufbt"):
            if directory.exists():
                shutil.rmtree(directory)
        run([ufbt], project, environment, broker)
        artifact = project / "dist" / (app_id + ".fap")
        if not artifact.is_file() or not 0 < artifact.stat().st_size <= 32 * 1024 * 1024:
            raise ValueError("Build did not produce a fresh dist/" + app_id + ".fap")
        # Binaries use an HTTP stream rather than Android's size-limited intent result extras.
        with broker.request("/artifact", artifact.read_bytes(), {
            "X-App-Id": app_id, "X-App-Category": urllib.parse.quote(category), "X-Builder-Version": UFBT_VERSION,
            "X-Toolchain-Version": compiler_version,
        }) as response:
            response.read()
        # uFBT puts object files, debug ELFs and another FAP copy outside the job
        # workspace. Keep the SDK profile, but remove these source-specific outputs.
        shutil.rmtree(state / "build", ignore_errors=True)
    # Keep SDK/compiler caches, but no source archive, unpacked project, or intermediate outputs.
    profiles = sorted(cache.glob("sdk-*"), key=lambda item: item.stat().st_mtime, reverse=True)
    for old in profiles[3:]:
        if old != state:
            shutil.rmtree(old)


def main():
    broker = Broker(sys.argv[1], sys.argv[2])
    try:
        if len(sys.argv) > 3 and sys.argv[3] == "--probe":
            with broker.request("/recipe") as response:
                if json.load(response)["protocol"] != PROTOCOL:
                    raise RuntimeError("Builder protocol mismatch")
            from importlib.metadata import version
            details = {"architecture": platform.machine(), "python": platform.python_version()}
            for package in ("ufbt", "ansi", "colorlog", "cxxheaderparser", "heatshrink2", "pyelftools", "scons", "Pillow"):
                details[package] = version(package)
            if details["ufbt"] != UFBT_VERSION:
                raise RuntimeError("Run setup to install uFBT " + UFBT_VERSION)
            details["glibc"] = Path("/data/data/com.termux/files/usr/glibc/lib/ld-linux-aarch64.so.1").is_file()
            if not details["glibc"]:
                raise RuntimeError("Run setup: glibc-runner is missing")
            with broker.request("/capabilities", json.dumps(details).encode()):
                pass
        else:
            build(broker)
    except BaseException as error:
        try:
            with broker.request("/error", str(error).encode("utf-8", "replace")[:8192]):
                pass
        except Exception:
            pass
        raise


if __name__ == "__main__":
    main()
