# Padavan Control — patched fork

Source copied from the patched fork:

- https://github.com/antoxa78/padavan-control
- Original release: https://github.com/antoxa78/padavan-control/releases/tag/v1.2.3-fixes
- New signed release: https://github.com/antoxa78/padavan-control/releases/tag/v1.2.5-fixes

## 1.2.5-fixes

- Uptime is now always shown in the dashboard internet card. In AP mode Padavan leaves `wan0_uptime` empty, so the card falls back to the router system uptime.
- DNS entries separated by `<br/>` in the WAN status page are parsed correctly.

## 1.2.4-fixes

- Dashboard internet card now shows live connection details: WAN IP, gateway, DNS, connection type and uptime.
- Dark theme coverage fixed across LAN, WAN, Firewall, USB, Administration, Scripts, VPN and Wireless screens, plus breadcrumbs and dropdown menus.
- Login screen now scrolls on short or landscape screens, so discovered routers and the Connect button stay reachable.
- Fixed locale-dependent time formatting that could send non-Latin digits to the router.
- Updated AndroidX/Kotlin dependencies and moved to compile/target SDK 37.
- Removed unused resources and fixed lint/API warnings.
- This build is signed with the project release key, not the Android debug key.

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
- Fixed radio schedule time-of-day fields so hours/minutes can be typed and edited.
- Fixed numeric wireless inputs (RSSI thresholds, fragmentation, RTS, DTIM, beacon, key rotation, RADIUS port) so backspace and partial input work.
- Fixed Wireless Professional validation blocking saves when opened without visiting General first.
- Fixed HTML parser prefix collisions (`rt_gmode` vs `rt_gmode_protection`, `telnetd` vs `telnetd_port`) that could downgrade 2.4 GHz Wi-Fi to 802.11b.
- Fixed 2.4 GHz extension channel parsing (`rt_HT_EXTCHA_old`).
- Fixed SSIDs and passwords containing `+` being loaded with spaces.
- Fixed admin device name parsing (`computer_name2`) and mapped the timezone dropdown to real Padavan timezone codes.
- Fixed firewall day-of-week schedules being shifted by one day.
- Fixed untranslated section-key comparisons that blanked the WAN Connection screen in non-English languages.
- Manual IP and saved router address still work.

## Build

Requirements: JDK 21, Android SDK 37.2. The local `local.properties` points to `/home/anton/Android/Sdk`.

```sh
cd padavan-control
bash gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Install

- Android 8.0 or newer.
- The included APK is signed with the project release key. Upgrading from 1.2.4 is fine. Because the 1.2.3 and older APKs were debug-signed, Android will refuse to install 1.2.5 over them; uninstall the old app first. That clears saved settings and credentials.
- Connect the phone to the router's main Wi-Fi.
- If auto-scan misses the router, use **Set IP Manually** with `192.168.31.10`.

## APK

- `dist/Padavan_Control-1.2.5-fixes.apk`
- SHA256: `76e0b6cfb02c9cb9941524cc160bb896ac27a1bf756728d938157ba35a9f3dd1`
