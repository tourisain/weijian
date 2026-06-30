# Weijian Visual Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Weijian feel more pure and cohesive by reducing visual weight across global style, dialogs, settings rows, and note list rows.

**Architecture:** Add small style metrics to `AppleNotesStyle`, then reuse them in common UI components and note list rendering. Keep behavior unchanged and test the visual rules with lightweight unit tests.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Gradle release build with R8.

## Global Constraints

- Do not add new product features.
- Keep the existing Apple Notes-inspired direction.
- Prefer lighter surfaces, consistent row heights, restrained accent use, and fewer heavy cards.
- Every release build must be archived under `release/`.
- The app version auto-increments after a successful release build.

---

### Task 1: Add Visual Rhythm Rules

**Files:**
- Modify: `app/src/main/java/com/tourisain/weijian/presentation/note/components/AppleNotesStyle.kt`
- Test: `app/src/test/java/com/tourisain/weijian/presentation/note/components/AppleNotesStyleTest.kt`

**Interfaces:**
- Produces constants: `GroupRadiusDp`, `SearchRadiusDp`, `SettingsRowVerticalPaddingDp`, `NoteRowVerticalPaddingDp`, `ListIconSizeDp`, `ListIconGlyphSizeDp`, `DialogMaxHeightDp`.

- [ ] Write failing tests for the constants.
- [ ] Run the targeted test and verify missing references.
- [ ] Add constants to `AppleNotesStyle`.
- [ ] Re-run the targeted test.

### Task 2: Apply Rhythm to Settings and Dialogs

**Files:**
- Modify: `app/src/main/java/com/tourisain/weijian/presentation/settings/SettingsComponents.kt`
- Modify: `app/src/main/java/com/tourisain/weijian/presentation/common/AppleDialog.kt`

**Interfaces:**
- Consumes `AppleNotesStyle.SettingsRowVerticalPaddingDp`, `ListIconSizeDp`, `ListIconGlyphSizeDp`, `DialogMaxHeightDp`.

- [ ] Use the rhythm constants in settings rows.
- [ ] Keep settings groups light and consistent.
- [ ] Make alert dialogs use the unified max height and softer container.
- [ ] Compile with release unit tests.

### Task 3: Lighten Note List Rows

**Files:**
- Modify: `app/src/main/java/com/tourisain/weijian/presentation/note/list/NoteListScreen.kt`

**Interfaces:**
- Consumes `AppleNotesStyle.NoteRowVerticalPaddingDp`, `ListIconSizeDp`, `ListIconGlyphSizeDp`.

- [ ] Reduce row icon weight.
- [ ] Make metadata use a middle dot instead of a hyphen.
- [ ] Use consistent row padding and softer pinned state.
- [ ] Compile with release unit tests.

### Task 4: Verify and Build

**Files:**
- Read: `app/build/outputs/apk/release/output-metadata.json`
- Copy: `app/build/outputs/apk/release/app-release.apk`
- Copy: `app/build/outputs/mapping/release/mapping.txt`

- [ ] Run `:app:testReleaseUnitTest`.
- [ ] Run `:app:assembleRelease`.
- [ ] Archive APK and mapping using the built version name.
