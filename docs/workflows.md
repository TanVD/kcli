# Reusable Workflows

kcli publishes two reusable GitHub Actions workflows for building and releasing GraalVM native binaries. They can be
called from any repository.

## reusable-native-build

Builds a native binary for all four platforms in parallel, runs a smoke test on each, and uploads the artifacts.

**Platforms:** `linux-x64`, `windows-x64`, `macos-x64`, `macos-arm64`

### Inputs

| Input                     | Required | Default   | Description                                                                     |
|---------------------------|----------|-----------|---------------------------------------------------------------------------------|
| `gradle-task`             | yes      | —         | Gradle task for native compilation, e.g. `:myapp:nativeCompile`                 |
| `binary-name`             | yes      | —         | Base name of the output binary, e.g. `myapp`                                    |
| `binary-dir`              | yes      | —         | Directory containing the compiled binary, e.g. `app/build/native/nativeCompile` |
| `java-version`            | no       | `21`      | Java version for GraalVM                                                        |
| `graalvm-distribution`    | no       | `graalvm` | GraalVM distribution                                                            |
| `gradle-args`             | no       | `""`      | Extra arguments passed to Gradle                                                |
| `artifact-retention-days` | no       | `7`       | How long to keep uploaded artifacts                                             |

### Outputs

Each platform uploads an artifact named `{binary-name}-{platform}-{arch}` containing the binary and a `.sha256` checksum
file.

---

## reusable-native-release

Downloads the artifacts produced by `reusable-native-build`, verifies all four platforms are present, and creates a
GitHub Release with release notes and checksums.

### Inputs

| Input          | Required | Default                    | Description                                             |
|----------------|----------|----------------------------|---------------------------------------------------------|
| `binary-name`  | yes      | —                          | Must match the `binary-name` used in the build workflow |
| `version`      | yes      | —                          | Version string, e.g. `1.2.3`                            |
| `tag-name`     | no       | triggering tag             | Git tag to attach the release to                        |
| `release-name` | no       | `{binary-name} v{version}` | Release title                                           |
| `prerelease`   | no       | `false`                    | Mark as pre-release                                     |

---

## Usage

Call both workflows from a single caller workflow in your repository. The release job depends on the build workflow
completing first.

```yaml
# .github/workflows/release.yml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    uses: tanvd/kcli/.github/workflows/reusable-native-build.yml@main
    with:
      gradle-task: ':myapp:nativeCompile'
      binary-name: myapp
      binary-dir: myapp/build/native/nativeCompile

  release:
    needs: build
    uses: tanvd/kcli/.github/workflows/reusable-native-release.yml@main
    with:
      binary-name: myapp
      version: ${{ github.ref_name }}   # e.g. v1.2.3 from the tag
    permissions:
      contents: write
```

### CI-only (no release)

If you only want to verify native compilation on each push without creating a release:

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:

jobs:
  native:
    uses: tanvd/kcli/.github/workflows/reusable-native-build.yml@main
    with:
      gradle-task: ':myapp:nativeCompile'
      binary-name: myapp
      binary-dir: myapp/build/native/nativeCompile
      artifact-retention-days: 1
```

---

## Gradle setup

Your project needs the GraalVM native plugin applied to the module being compiled:

```kotlin
// gradle/libs.versions.toml
[versions]
graalvm - native = "0.10.6"

[plugins]
graalvm - native = { id = "org.graalvm.buildtools.native", version.ref = "graalvm-native" }
```

```kotlin
// myapp/build.gradle.kts
plugins {
    alias(libs.plugins.graalvm.native)
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("myapp")
            mainClass.set("com.example.MainKt")
            buildArgs.addAll(
                "--no-fallback",
                "--initialize-at-build-time=kotlin",
                "--initialize-at-build-time=kotlinx.coroutines",
                "-H:+RemoveUnusedSymbols",
                "-H:+DeleteLocalSymbols",
                "--strict-image-heap",
            )
        }
    }
    metadataRepository {
        enabled.set(true)
    }
}
```
