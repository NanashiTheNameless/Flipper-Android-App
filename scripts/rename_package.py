#!/usr/bin/env python3
"""Rename the com.flipperdevices package to dev.namelessnanashi.flipperzero.

Idempotent: re-run after merging upstream changes. Leaves alone:
- com.flipperdevices.protobuf.*: generated from the protobuf submodule, which we do not edit
- github.com/flipperdevices URLs
- submodules, build output, .git, .github (replaced by our own workflows)

Usage: python3 scripts/rename_package.py   (from the repository root)
"""
import os
import re
import shutil
import sys

OLD = "com.flipperdevices"
NEW = "dev.namelessnanashi.flipperzero"
OLD_PATH = os.path.join("com", "flipperdevices")
NEW_PATH = os.path.join("dev", "namelessnanashi", "flipperzero")

TEXT_EXTENSIONS = {".kt", ".kts", ".java", ".xml", ".pro", ".proto", ".ftl", ".yaml", ".yml", ".properties",
                   ".c", ".cpp", ".h"}
SKIP_DIRS = {".git", ".gradle", ".github", "build", ".idea", ".kotlin"}
SKIP_PATHS = {
    os.path.join("components", "bridge", "pbutils", "src", "main", "proto"),
    os.path.join("components", "nfc", "tools", "impl", "src", "main", "cpp", "nfc-tools"),
}

PACKAGE_RE = re.compile(r"com\.flipperdevices(?!\.protobuf\b)")
# Template paths like kotlin/com/flipperdevices/... in geminio recipes
PATH_RE = re.compile(r"(?<![.\w])com/flipperdevices/")
# JNI symbols mangle the package: Java_com_flipperdevices_..._Class_method
JNI_RE = re.compile(r"\bJava_com_flipperdevices_")
JNI_NEW = "Java_" + NEW.replace(".", "_") + "_"


def walk(root):
    for dirpath, dirnames, filenames in os.walk(root):
        rel = os.path.relpath(dirpath, root)
        dirnames[:] = [
            d for d in dirnames
            if d not in SKIP_DIRS and os.path.normpath(os.path.join(rel, d)) not in SKIP_PATHS
        ]
        yield dirpath, dirnames, filenames


def rewrite_text(root):
    changed = 0
    for dirpath, _, filenames in walk(root):
        for name in filenames:
            if os.path.splitext(name)[1] not in TEXT_EXTENSIONS:
                continue
            path = os.path.join(dirpath, name)
            try:
                with open(path, encoding="utf-8") as f:
                    text = f.read()
            except UnicodeDecodeError:
                continue
            new_text = PACKAGE_RE.sub(NEW, text)
            new_text = PATH_RE.sub(NEW_PATH.replace(os.sep, "/") + "/", new_text)
            new_text = JNI_RE.sub(JNI_NEW, new_text)
            if new_text != text:
                with open(path, "w", encoding="utf-8") as f:
                    f.write(new_text)
                changed += 1
    return changed


def move_source_dirs(root):
    moved = 0
    candidates = []
    for dirpath, dirnames, _ in walk(root):
        if os.path.basename(dirpath) == "com" and "flipperdevices" in dirnames:
            candidates.append(dirpath)
    for com_dir in candidates:
        source = os.path.join(com_dir, "flipperdevices")
        target = os.path.join(os.path.dirname(com_dir), NEW_PATH)
        os.makedirs(target, exist_ok=True)
        for entry in os.listdir(source):
            src = os.path.join(source, entry)
            dst = os.path.join(target, entry)
            if os.path.exists(dst):
                sys.exit(f"Refusing to overwrite {dst}")
            shutil.move(src, dst)
        os.rmdir(source)
        if not os.listdir(com_dir):
            os.rmdir(com_dir)
        moved += 1
    return moved


def rename_dotted_names(root):
    """Rename files and folders named after a class, e.g. Room schemas/com.flipperdevices...AppDatabase."""
    renamed = 0
    matches = []
    for dirpath, dirnames, filenames in walk(root):
        for name in dirnames + filenames:
            if PACKAGE_RE.search(name):
                matches.append(os.path.join(dirpath, name))
    for path in matches:
        target = os.path.join(os.path.dirname(path), PACKAGE_RE.sub(NEW, os.path.basename(path)))
        if os.path.exists(target):
            sys.exit(f"Refusing to overwrite {target}")
        shutil.move(path, target)
        renamed += 1
    return renamed


def main():
    root = os.getcwd()
    if not os.path.isfile(os.path.join(root, "settings.gradle.kts")):
        sys.exit("Run from the repository root")
    print(f"Rewrote {rewrite_text(root)} files")
    print(f"Moved {move_source_dirs(root)} source directories")
    print(f"Renamed {rename_dotted_names(root)} package-named files and folders")


if __name__ == "__main__":
    main()
