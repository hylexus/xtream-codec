# AGENTS.md - xtream-codec

## Repository Facts

- **Project version**: `0.7.0-alpha.0`, read from `gradle.properties` (`projectVersion` is the source of truth)
- **JDK**: 21, managed by `mise.toml` as `temurin-21`
- **Node.js**: 22, required by `docs/package.json`
- **pnpm**: 9.12.1 or newer for the docs module
- **Gradle**: 9.8.1 through `./gradlew`
- **Build scripts**: Kotlin DSL
- **OpenSpec schema**: `spec-driven`, configured in `openspec/config.yaml`

## Build and Test

Common commands:

```bash
# Core module tests
./gradlew :xtream-codec-core:test

# Run one test class
./gradlew :module:test --tests "io.example.FullyQualifiedTestName"

# Debug/demo module test
./gradlew :debug:xtream-codec-core-debug:test \
  --tests "io.github.hylexus.xtream.debug.codec.core.demo005.DemoMessage005Test"

# Full local build with slow checks disabled
./gradlew build \
  -P xtream.backend.build.checkstyle.enabled=false \
  -P xtream.backend.build.errorprone.enabled=false

# Full build with checkstyle
./gradlew clean build -P xtream.backend.build.checkstyle.enabled=true

# Update project version references outside release notes
./gradlew updateVersion
```

The default local settings in `gradle.properties` disable checkstyle and Error Prone:

- `xtream.backend.build.checkstyle.enabled=false`
- `xtream.backend.build.errorprone.enabled=false`

If `JAVA_HOME` points to a removed JDK, use the JDK resolved by `mise` before running Gradle:

```bash
JAVA_HOME="$(mise where java)" ./gradlew <task>
```

## Module Structure

```
xtream-codec-dependencies/     # Dependency BOM/constraints
xtream-codec-base/             # Shared base APIs, expressions, utilities
xtream-codec-core/             # Annotation-driven codec
xtream-codec-server-reactive/  # Async non-blocking TCP/UDP server
ext/jt/                        # JT/T 808 and JT/T 1078 extensions
quick-start/                   # Runnable quick-start applications
debug/                         # Debug entities, protocol demos, and focused tests
docs/                          # VuePress documentation site
openspec/                      # Change proposals, specs, and archived changes
build-script/                 # Shared Gradle, checkstyle, license, and publishing scripts
```

`debug/` is part of the verification and documentation workflow. Do not ignore it when a change touches demos,
documentation examples, or debug-module tests.

## Documentation

The docs site is VuePress/Vite:

```bash
cd docs
pnpm docs:dev       # local server
pnpm docs:build     # production build
pnpm docs:clean-dev # dev server with a clean VuePress cache
```

`docs/src/.vuepress/config.ts` defines source-code import aliases:

- `@project` → repository root
- `@core-test` → `xtream-codec-core/src/test/java`
- `@core-debug-test` → `debug/xtream-codec-core-debug/src/test/java`
- `@core-debug` → `debug/xtream-codec-core-debug/src/main/java`
- `@src` → `docs/src/code-snippet`

When a Java test, demo, or type is referenced by a docs page, annotate the type with:

```java
@ReferencedByDocs("guide/core/annotation-driven/example.md")
```

The path is relative to `docs/src/`. Keep the annotation path and the VuePress `@[code](...)` import path in sync.
The docs build must be run when changing referenced source snippets.

## OpenSpec Workflow

Use the OpenSpec CLI and the repository-local skills for change work:

```bash
# Discover active changes
openspec list --json

# Create a new change; do not manually scaffold openspec/changes/<name>
openspec new change "<change-name>"

# Check artifacts and implementation task progress
openspec status --change "<change-name>" --json
openspec instructions apply --change "<change-name>" --json

# Validate a change or all specs
openspec validate "<change-name>" --strict
openspec validate --specs

# Archive after implementation and task completion
openspec instructions archive --change "<change-name>" --json
```

For a new capability, the main spec must contain one `## Purpose` section and one `## Requirements` section.
Requirements use `### Requirement:` and scenarios use exactly `#### Scenario:`. Do not append a second
`## ... Requirements` section; merge additional requirements into the main `## Requirements` section before archive.

When archiving a change with delta specs:

1. Compare each delta under `openspec/changes/<name>/specs/` with its corresponding main spec under `openspec/specs/`.
2. Sync new or modified requirements into the main spec.
3. Validate the synced main spec and the change with `--strict`.
4. Move the completed change to `openspec/changes/archive/YYYY-MM-DD-<change-name>/`.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## 5. Code Conventions

**These rules must be followed in ALL generated code.**

### 5.1 @since Tag Version

When adding `@since` to JavaDoc, use the stable public release line, not the current pre-release artifact version.

- Current artifact version: `0.7.0-alpha.0` (`projectVersion=0.7.0-alpha.0` in `gradle.properties`)
- Current public API `@since` target: `0.7.0`
- New APIs added now → `@since 0.7.0`
- Strip pre-release suffixes such as `-alpha.*`, `-beta.*`, and `-rc.*` from `projectVersion` for Javadoc.
- Do NOT hardcode outdated versions; always check `gradle.properties` first and derive the stable release line.

```java
// Correct (new code):
/**
 * @since 0.7.0
 */
default boolean isDerived() { return false; }

// Wrong (version doesn't match gradle.properties):
/**
 * @since 0.7.0-alpha.0
 */
default boolean isDerived() { return false; }
```

### 5.2 @Nullable Placement (Jspecify)

`@Nullable` is a **type-use annotation**. It MUST be placed immediately before the type it modifies, NOT on a separate line before the method declaration.

```java
// Correct — @Nullable before the return type:
public @Nullable String getDisplayName() { ... }
default @Nullable Object getProperty(Object instance) { ... }
public static @Nullable String getVariable(String name) { ... }

// Wrong — @Nullable on its own line before default/modifier:
// @Nullable         ← wrong
// default Object getProperty(...) { ... }

// Wrong — @Nullable separated from the return type by a modifier:
// @Nullable private String name;              ← wrong
// private @Nullable String name;              ← correct (no modifier between @Nullable and the type)
```

Rationale: Per [Jspecify 1.0](https://jspecify.dev/), type-use annotations should be adjacent to the annotated type to avoid ambiguity about what they modify.

### 5.3 Comment Language

All code comments (including JavaDoc, inline comments, TODO, FIXME, etc.) MUST be written in **Simplified Chinese** unless the comment targets an international audience (e.g., SPI interface docs meant for external contributors).

```java
// Correct (简体中文):
// 将状态码转换为业务枚举
@Nullable
StatusEnum resolveStatusCode(int code);

// Wrong (English comments in Chinese project):
// Convert status code to business enum
@Nullable
StatusEnum resolveStatusCode(int code);
```

### 5.4 Javadoc Authors

When adding or substantially updating a class-level Javadoc:

- Preserve existing `@author` entries.
- Add `@author Codex (AI)` when Codex contributed to the change.
- Do not replace the human author's attribution with the AI attribution.

## 6. Change Safety

- Inspect `git status --short` before editing.
- Never revert or clean unrelated user changes.
- Use `apply_patch` for manual file edits.
- Keep changes scoped to the request; remove only imports or code made unused by your own change.
- Before finishing, run the narrowest relevant tests, `git diff --check`, and the relevant style/docs validation.
