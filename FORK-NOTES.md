# Padavan Control — patched fork

Source copied from the patched fork:

- https://github.com/antoxa78/padavan-control
- Release: https://github.com/antoxa78/padavan-control/releases/tag/v1.2.2-buttons

## Changes

- Scans the phone's local `/24` subnet, so the RM-AC2100 is found even in AP mode.
- Name-agnostic detection: accepts any `/index.asp` with HTTP Basic auth, `Server: httpd`, or known Padavan markers.
- Dashboard reads the real `wifi2.state` / `wifi5.state` from `system_status_data.asp`.
- Dashboard, Settings, and the login screen display the discovered router name and IP correctly.
- Mobile clients are classified correctly as 2.4 GHz / 5 GHz / wireless instead of appearing as LAN cables.
- Port status is derived from the router's own port list, so RM-AC2100 shows 4 ports and Newifi D2 shows 5.
- Remember Credentials now respects the login checkbox and Settings toggle.
- Polling now runs only while a screen is visible, backs off after failures, and caches band-status pages.
- Faster startup: ViewModels are created on demand and regex patterns are cached.
- Gentler `/24` discovery: lower concurrency, jitter, Wi-Fi-only scanning, and no SSDP when a router is already found. This avoids temporary DNS/DHCP disruption on weak routers.
- Fixed action buttons that Padavan was silently ignoring:
  - Wi-Fi radio toggles now send `sid_list=WLANConfig11a/11b`.
  - Device block/unblock now uses the `MFList` group and reject-list mode.
  - Factory reset now posts `RestoreNVRAM`/`RestoreStorage` to `apply.cgi`.
  - Dashboard Save and Commit buttons now use the `CommitFlash` action.
  - Wireless saves now use the real NVRAM field names for SSID, PSK, guest, bridge, RADIUS and MAC-filter settings.
- Manual IP and saved router address still work.

## Build

Requirements: JDK 21, Android SDK 36. The local `local.properties` points to `/home/anton/Android/Sdk`.

```sh
cd padavan-control
bash gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Install

- Android 8.0 or newer.
- The included APK is **debug-signed** with the same debug key as `1.1-subnet`, `1.2-subnet` and `1.2.1-subnet`, so it can be installed over the previous release.
- Connect the phone to the router's main Wi-Fi.
- If auto-scan misses the router, use **Set IP Manually** with `192.168.31.10`.

## APK

- `dist/Padavan_Control-1.2.2-buttons.apk`
- SHA256: `1019ac025ac18d3dbb37104b9d26116662c250e99c6de90471deff1b9b241ab5`
