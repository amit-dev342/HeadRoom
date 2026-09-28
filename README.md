# HeadRoom

A local-first task manager built with Next.js + TypeScript and packaged for Android with Capacitor.

## Features
- Local-only task persistence
- Add, edit and delete tasks
- High / Medium / Low priority gradients
- Due date, tag and notes
- Complete / reactivate tasks while keeping completed tasks visible
- Monday–Sunday collapsible week groups
- Compact / Default / Expanded task sizes
- Card / List / Board layouts
- Mobile-first dark layered UI
- Native Android home-screen widget bridge
- GitHub Actions debug APK build

## Web development
```bash
npm install
npm run dev
```

## Android APK
Every push to `main` runs **Build HeadRoom APK**. Open the GitHub Actions run, download the **HeadRoom-APK** artifact, unzip it, and install `app-debug.apk` on Android.

The workflow builds the Next.js static export, generates the Capacitor Android shell, installs the native widget sources, and runs Gradle `assembleDebug`.
