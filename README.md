# HeadRoom

A local-first task manager rebuilt in Next.js + TypeScript from the ColorTodo PR #10 feature set.

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
- Static export configured for Capacitor Android packaging

## Run
```bash
npm install
npm run dev
```

## Build
```bash
npm run build
```

The exported web application is written to `out/` and can be packaged with Capacitor.
