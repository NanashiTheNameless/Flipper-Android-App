#!/usr/bin/env bash
set -euo pipefail

selector="$(cd "$(dirname "$0")" && pwd)/unit_test_tasks.sh"
fixture=$(mktemp -d)
trap 'rm -rf "$fixture"' EXIT
cd "$fixture"

mkdir -p components/parser/src/test/kotlin components/parser/src/commonTest/kotlin \
    components/empty/src/main/kotlin components/empty/build/generated/src/test/kotlin \
    components/modern/src/androidHostTest/kotlin components/shared/src/jvmSharedTest/kotlin \
    instances/app/src/testDebug/java
for module in parser empty modern shared; do
    printf 'plugins { id("flipper.multiplatform") }\n' > "components/$module/build.gradle.kts"
done
printf 'plugins { id("flipper.android-app") }\n' > instances/app/build.gradle.kts
touch components/parser/src/test/kotlin/ParserTest.kt \
    components/parser/src/commonTest/kotlin/SharedTest.kt \
    components/empty/src/main/kotlin/Main.kt \
    components/empty/build/generated/src/test/kotlin/GeneratedTest.kt \
    components/modern/src/androidHostTest/kotlin/ModernTest.kt \
    components/shared/src/jvmSharedTest/kotlin/SharedTest.kt \
    instances/app/src/testDebug/java/AppTest.java
expected=':components:modern:testAndroidHostTest
:components:parser:testAndroidHostTest
:components:shared:testAndroidHostTest
:instances:app:testDebugUnitTest'
actual=$(bash "$selector")
if [ "$actual" != "$expected" ]; then
    printf 'Unexpected test task selection:\n%s\n' "$actual" >&2
    exit 1
fi

# Unsupported conventions must fail loudly instead of silently losing coverage.
mkdir -p components/unknown/src/test/kotlin
touch components/unknown/src/test/kotlin/UnknownTest.kt
printf 'plugins { id("unknown") }\n' > components/unknown/build.gradle.kts
if bash "$selector" > /dev/null 2>&1; then
    echo 'Expected unsupported test module to fail task selection' >&2
    exit 1
fi
echo 'Unit-test task discovery: OK'
