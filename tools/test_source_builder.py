"""Host regression checks for the phone-side worker; no toolchain download required."""
import importlib.util
import io
import json
from pathlib import Path
import os
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
WORKER = ROOT / "components/faphub/source/impl/src/androidMain/assets/nanashi_builder.py"
spec = importlib.util.spec_from_file_location("nanashi_builder", WORKER)
worker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(worker)


class BuilderTest(unittest.TestCase):
    def test_metadata_is_discovered_without_executing_the_manifest(self):
        with tempfile.TemporaryDirectory() as root:
            project = Path(root)
            marker = project / "should-not-run"
            (project / "application.fam").write_text(
                f"open({str(marker)!r}, 'w').write('bad')\n"
                "App(appid='tagtinker', apptype=FlipperAppType.EXTERNAL, fap_category='Infrared')"
            )
            self.assertEqual(("tagtinker", "Infrared"), worker.app_description(project))
            self.assertFalse(marker.exists())

    def test_dynamic_or_ambiguous_app_ids_are_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            project = Path(root)
            for manifest in (
                "App(appid=get_id(), apptype=FlipperAppType.EXTERNAL)",
                "App(appid='../escape', apptype=FlipperAppType.EXTERNAL)",
                "App(appid='a', apptype=FlipperAppType.EXTERNAL)\nApp(appid='b', apptype=FlipperAppType.EXTERNAL)",
            ):
                (project / "application.fam").write_text(manifest)
                with self.assertRaises((ValueError, TypeError)):
                    worker.app_description(project)

    def test_archive_traversal_never_writes_outside_the_workspace(self):
        with tempfile.TemporaryDirectory() as root:
            archive, destination = Path(root) / "source.zip", Path(root) / "source"
            destination.mkdir()
            with zipfile.ZipFile(archive, "w") as output:
                output.writestr("../outside", "bad")
            with self.assertRaises(ValueError):
                worker.extract(archive, destination)
            self.assertFalse((Path(root) / "outside").exists())

    def test_toolchain_wrappers_are_idempotent_and_leave_libraries_untouched(self):
        with tempfile.TemporaryDirectory(prefix="builder test ") as root:
            root = Path(root)
            prefix, toolchain = root / "prefix", root / "gcc"
            loader = prefix / "glibc/lib/ld-linux-aarch64.so.1"
            loader.parent.mkdir(parents=True)
            loader.write_bytes(b"loader")
            compiler = toolchain / "bin/arm-none-eabi-gcc"
            compiler.parent.mkdir(parents=True)
            compiler.write_bytes(b"\x7fELFcompiler")
            library = toolchain / "libexec/gcc/arm-none-eabi/12/liblto_plugin.so"
            library.parent.mkdir(parents=True)
            library.write_bytes(b"\x7fELFlibrary")
            worker.wrap_toolchain(toolchain, prefix)
            first = compiler.read_bytes()
            worker.wrap_toolchain(toolchain, prefix)
            self.assertEqual(first, compiler.read_bytes())
            self.assertEqual(b"\x7fELFcompiler", Path(str(compiler) + ".real").read_bytes())
            self.assertEqual(b"\x7fELFlibrary", library.read_bytes())
            self.assertFalse(Path(str(library) + ".real").exists())

    def test_cancellation_terminates_the_started_process(self):
        class CancelledBroker:
            def report(self, text):
                pass

            def request(self, path):
                return io.BytesIO(b'{"cancelled":true}')

        created = []
        original = subprocess.Popen

        def spawn(*args, **kwargs):
            process = original(*args, **kwargs)
            created.append(process)
            return process

        with tempfile.TemporaryDirectory() as root, patch.object(worker.subprocess, "Popen", spawn):
            with self.assertRaisesRegex(RuntimeError, "cancelled"):
                worker.run([sys.executable, "-c", "import time; time.sleep(60)"], root, dict(os.environ), CancelledBroker())
        self.assertIsNotNone(created[0].returncode)

    def test_success_uses_a_fresh_output_and_cleans_the_job_workspace(self):
        with tempfile.TemporaryDirectory() as root:
            root = Path(root)

            class FakeBroker:
                artifact = None

                def request(self, path, data=None, headers=None):
                    if path == "/recipe":
                        return io.BytesIO(json.dumps({"protocol": 1, "project": "repo", "sourceSha256": "a", "sdkSha256": "b"}).encode())
                    if path == "/artifact":
                        self.artifact = data
                    return io.BytesIO(b"")

                def download(self, route, output, expected):
                    if route == "/source":
                        with zipfile.ZipFile(output, "w") as archive:
                            archive.writestr("repo/application.fam", "App(appid='test', apptype=FlipperAppType.EXTERNAL, fap_category='Tools')")
                            archive.writestr("repo/.env", "UFBT_HOME=wrong")
                            archive.writestr("repo/dist/test.fap", "stale")
                    else:
                        output.write_bytes(b"sdk")

            def run(command, project, environment, *_args, **_kwargs):
                if len(command) == 1:
                    self.assertFalse((project / ".env").exists())
                    self.assertFalse((project / "dist").exists())
                    (project / "dist").mkdir()
                    (project / "dist/test.fap").write_bytes(b"fresh-fap")
                    sdk_state = Path(environment["UFBT_HOME"])
                    (sdk_state / "build").mkdir(parents=True)
                    (sdk_state / "build/test.o").write_bytes(b"intermediate")
                    (sdk_state / "current").mkdir()
                    (sdk_state / "current/sdk.json").write_text("SDK profile")

            broker = FakeBroker()
            with patch.object(worker.platform, "machine", return_value="aarch64"), \
                    patch("importlib.metadata.version", return_value="0.2.6"), \
                    patch.object(worker.Path, "home", return_value=root), \
                    patch.object(worker, "run", side_effect=run), \
                    patch.object(worker, "wrap_toolchain"), \
                    patch.object(worker.subprocess, "check_output", return_value=b"GCC 12\n"):
                worker.build(broker)
            self.assertEqual(b"fresh-fap", broker.artifact)
            self.assertEqual([], list((root / ".flipper-nanashi-builder").glob("job-*")))
            sdk_state = root / ".flipper-nanashi-builder/sdk-b"
            self.assertFalse((sdk_state / "build").exists())
            self.assertTrue((sdk_state / "current/sdk.json").is_file())


if __name__ == "__main__":
    unittest.main()
