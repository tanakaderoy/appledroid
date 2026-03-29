# appledroid

A minimal Android app that intercepts Apple web links and silently routes them to native Android apps.

No UI. The router activity is invisible — it dispatches the intent and closes immediately.

## Supported mappings

| Apple URL | Android target | Fallback |
|---|---|---|
| `maps.apple.com` | Google Maps (`geo:` URI) | Browser |
| `music.apple.com` | Spotify search | open.spotify.com/search |
| `podcasts.apple.com` | Google Search (`{show} podcast`) | Browser |
| `tv.apple.com` | — | Browser |

### URL parsing details

**Maps** — extracts the first available param in priority order:
`ll` → `geo:lat,lon` · `q` → `geo:0,0?q=…` · `address` → `geo:0,0?q=…` · `auid` → `geo:0,0?q=…`

**Music** — reads path segments (`/{locale}/artist|album|song/{slug}/{id}`) and converts the slug to a Spotify search query.

## Building

Open the project in Android Studio. On first sync it will prompt you to generate the Gradle wrapper JAR — accept, or run manually:

```bash
gradle wrapper --gradle-version 8.11.1
./gradlew assembleDebug
./gradlew test
```

## Adding a new mapping

1. Add the host to the intent filters in `AndroidManifest.xml`.
2. Add a route entry in `RouteMappings.ROUTES` pointing to a new handler function.
3. Implement a `*UrlParser` if the URL needs non-trivial parsing.
4. Add unit tests in `src/test/`.

## Enabling link interception (App Links)

Android requires the domain owner to host a verification file for automatic link handling. Since these are Apple's domains, users must manually enable the app in Settings:

> Settings → Apps → Apple Link Router → Open by default → Supported web addresses → enable all four domains

**Developer shortcut (ADB):** force-enable all domains in one command:

```bash
adb shell pm set-app-links --package com.tanakaderoy.appledroid 2 \
  maps.apple.com \
  music.apple.com \
  podcasts.apple.com \
  tv.apple.com
```

This persists across reinstalls but not a full OS wipe.

## Manual testing (ADB)

```bash
adb shell am start -a android.intent.action.VIEW \
  -d "https://maps.apple.com/?q=Eiffel+Tower"

adb shell am start -a android.intent.action.VIEW \
  -d "https://music.apple.com/us/artist/radiohead/1053394"
```

## Package

`com.tanakaderoy.appledroid` · minSdk 26 · targetSdk 35
