#!/usr/bin/env bash
# Run from the repository root. Include new/untracked tests without preparing
# empty test variants across every Android module in the repository.
set -euo pipefail

find components instances \
    -type d \( -name build -o -name .gradle -o -name .git \) -prune -o \
    -type f \( -name '*.kt' -o -name '*.java' \) \
    \( -path '*/src/test/*' -o -path '*/src/testDebug/*' \
       -o -path '*/src/androidHostTest/*' -o -path '*/src/commonTest/*' \
       -o -path '*/src/jvmSharedTest/*' \) -print0 |
while IFS= read -r -d '' source_file; do
    module_dir=${source_file%%/src/*}
    build_file="$module_dir/build.gradle.kts"
    if [ ! -f "$build_file" ]; then
        build_file="$module_dir/build.gradle"
    fi
    if grep -Eq 'flipper\.multiplatform|kotlin\.multiplatform' "$build_file"; then
        task=testAndroidHostTest
    elif grep -Eq 'flipper\.android-|com\.android\.(application|library)' "$build_file"; then
        task=testDebugUnitTest
    else
        echo "error: cannot select the Android unit-test task for $module_dir" >&2
        exit 1
    fi
    printf ':%s:%s\n' "${module_dir//\//:}" "$task"
done | LC_ALL=C sort -u
