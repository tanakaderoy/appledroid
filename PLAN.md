# appledroid — Implementation Plan

## Context

Build a minimal Android app (Kotlin + Gradle) that intercepts Apple web links and silently routes them to native Android apps (Google Maps, Spotify, etc.). The app has no UI — it launches as a transparent activity, translates the URL, dispatches a new intent to the target app, and immediately calls `finish()`.

The working directory `/storage/emulated/0/Download/github/appledroid` is empty. The machine has Java 21 but no Android SDK or Gradle installed. All project files must be written by hand. The project will be openable in Android Studio (or buildable on a machine with the Android SDK set up).

---

## Environment Note

The project is being authored in **Termux on Android** and will be built/run on a **laptop via Android Studio**. All source files are written by hand. The `gradle-wrapper.jar` (a binary, ~60 KB) **cannot be written by hand** and is excluded from the initial commits.

**Bootstrap step on laptop (one-time):**
```bash
# Option A — Android Studio handles it automatically on first sync
# Option B — if Gradle is installed on the laptop:
gradle wrapper --gradle-version 8.11.1
git add gradle/wrapper/gradle-wrapper.jar
git commit -m "chore: add gradle wrapper jar"
```

Everything else is committed from Termux.

---

## Project Configuration

| Setting | Value |
|---|---|
| Package | `com.example.appledroid` |
| Min SDK | 26 (Android 8.0, ~96% device coverage) |
| Target SDK | 35 (Android 15) |
| Kotlin | 2.0.21 |
| Gradle plugin | 8.7.0 |
| Gradle wrapper | 8.11.1 |
| Test framework | JUnit 4 + Mockito + Robolectric |

---

## Architecture

```
LinkRouterActivity          (entry point, invisible activity)
    │
    └── AppleLinkRouter     (routes Uri → RouteResult)
            │
            ├── RouteMappings   (host rules, delegates to parsers)
            │       ├── Maps    → MapsUrlParser → geo: URI / Google Maps
            │       ├── Music   → MusicUrlParser → Spotify search
            │       ├── Podcasts → fallback/search
            │       └── Tv       → fallback/search
            │
            ├── UrlParsers      (pure functions: Uri → structured data)
            │       ├── MapsUrlParser
            │       └── MusicUrlParser
            │
            └── RoutingFallbacks  (browser fallback, search fallback)
```

### Sealed result type
```kotlin
sealed class RouteResult {
    data class LaunchIntent(val intent: Intent) : RouteResult()
    data class FallbackIntent(val intent: Intent) : RouteResult()
    object NoMatch : RouteResult()
}
```

---

## Commit Chunks

### Chunk 1 — Git init + Gradle project scaffolding
Files: `.gitignore`, `settings.gradle.kts`, `build.gradle.kts` (root), `app/build.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `gradlew.bat`, `app/proguard-rules.pro`

The Gradle wrapper `.jar` is **not** committed here (binary not writable by hand). The README explains how to bootstrap it.

### Chunk 2 — Manifest + transparent activity shell
Files: `app/src/main/AndroidManifest.xml`, `app/src/main/kotlin/.../LinkRouterActivity.kt`, `app/src/main/res/values/themes.xml`

- Theme: `@android:style/Theme.NoDisplay`
- `android:exported="true"` + intent filters for all 4 Apple hosts
- Intent filter categories: `BROWSABLE`, `DEFAULT`; action: `ACTION_VIEW`; scheme: `https`
- Activity reads `intent.data`, delegates to router, calls `finish()`

### Chunk 3 — Router architecture (sealed types + `AppleLinkRouter`)
Files: `RouteResult.kt`, `AppleLinkRouter.kt`, `RouteMappings.kt` (stub), `RoutingFallbacks.kt`

- `AppleLinkRouter.route(uri)` switches on `uri.host`
- `RoutingFallbacks.browser(uri)` builds `ACTION_VIEW` intent for original URL
- `RoutingFallbacks.noOp()` returns `RouteResult.NoMatch`

### Chunk 4 — Apple Maps → Google Maps routing
Files: `MapsUrlParser.kt`, update `RouteMappings.kt`

- `MapsUrlParser.parse(uri)`: extracts `q`, `ll`, `address` query params
- Priority: `ll` → `geo:lat,lon` URI; `q`/`address` → `geo:0,0?q=...` URI
- Builds explicit intent for `com.google.android.apps.maps`
- Falls back to browser if Maps not installed (check via `PackageManager.getLaunchIntentForPackage`)

### Chunk 5 — Apple Music → Spotify routing
Files: `MusicUrlParser.kt`, update `RouteMappings.kt`

- `MusicUrlParser.parse(uri)`: reads path segments `/{locale}/artist|album|song/{name}/{id}`
- Extracts human-readable name from path (segment before the numeric ID)
- Builds Spotify search URI: `spotify:search:{name}` (falls back to `https://open.spotify.com/search/{name}`)
- Checks for `com.spotify.music`, falls back to browser

### Chunk 6 — Podcast + TV fallback routing
Files: update `RouteMappings.kt`

- `podcasts.apple.com`: extract show/episode name from path if present; build search intent or browser fallback
- `tv.apple.com`: pass through to browser (no clear Android equivalent)
- Both configurable via the `RouteMappings` object — easy to swap in a real target later

### Chunk 7 — Unit tests
Files:
- `app/src/test/.../MapsUrlParserTest.kt`
- `app/src/test/.../MusicUrlParserTest.kt`
- `app/src/test/.../AppleLinkRouterTest.kt`
- `app/src/test/.../RoutingFallbacksTest.kt`
- `app/src/test/.../LinkRouterActivityRobolectricTest.kt` (activity finish behavior)

Test cases:
- Maps: `?q=coffee`, `?ll=37.3,-122.0`, `?address=1+Infinite+Loop`, empty/malformed
- Music: `/us/artist/taylor-swift/159260351`, `/us/album/folklore/…`, missing segments
- Router: each host dispatches to correct mapping, unknown hosts return `NoMatch`
- Fallbacks: browser intent built correctly, no crash on null URI

### Chunk 8 — Cleanup + README
Files: `README.md`, final `.gitignore` review

README covers:
- Supported Apple → Android mappings table
- How to add a new mapping
- How to bootstrap the Gradle wrapper
- Build + test commands

---

## Critical Files

| File | Purpose |
|---|---|
| `app/src/main/AndroidManifest.xml` | Intent filters, exported activity declaration |
| `app/src/main/kotlin/.../LinkRouterActivity.kt` | Entry point, calls finish() |
| `app/src/main/kotlin/.../AppleLinkRouter.kt` | Central routing dispatcher |
| `app/src/main/kotlin/.../RouteMappings.kt` | Host → handler mapping |
| `app/src/main/kotlin/.../MapsUrlParser.kt` | Apple Maps URL parsing |
| `app/src/main/kotlin/.../MusicUrlParser.kt` | Apple Music URL parsing |
| `app/src/main/kotlin/.../RoutingFallbacks.kt` | Browser and search fallbacks |
| `app/src/main/res/values/themes.xml` | `Theme.NoDisplay` or transparent style |
| `app/build.gradle.kts` | Dependencies, SDK versions |

---

## Routing Details

### `maps.apple.com`
```
?ll=37.332,-122.03  →  geo:37.332,-122.03
?q=coffee+shops     →  geo:0,0?q=coffee shops
?address=1+Infinite+Loop  →  geo:0,0?q=1 Infinite Loop
```
Package target: `com.google.android.apps.maps`

### `music.apple.com`
```
/us/artist/radiohead/1053394      →  spotify:search:radiohead
/us/album/ok-computer/1097861203  →  spotify:search:ok computer
/us/song/karma-police/1097862350  →  spotify:search:karma police
```
Package target: `com.spotify.music`

### `podcasts.apple.com`
```
/us/podcast/my-show/id123456  →  search or browser fallback
```
No fixed package target (configurable).

### `tv.apple.com`
Browser pass-through (no Android equivalent yet).

---

## Fallback Chain
1. Check `packageManager.getLaunchIntentForPackage(targetPackage) != null`
2. If installed → `RouteResult.LaunchIntent` with explicit package
3. If not installed → `RouteResult.FallbackIntent` with browser or Spotify web URL
4. Malformed/unrecognized URI → `RouteResult.NoMatch` (activity still calls `finish()`)

---

## Verification

Once the project is opened in Android Studio with SDK installed:
```bash
./gradlew test              # run unit tests
./gradlew assembleDebug     # confirm build succeeds
./gradlew connectedCheck    # instrumented tests (needs device/emulator)
```

Manual test: use ADB to send an intent and verify correct dispatch:
```bash
adb shell am start -a android.intent.action.VIEW \
  -d "https://maps.apple.com/?q=Eiffel+Tower" \
  com.example.appledroid
```
