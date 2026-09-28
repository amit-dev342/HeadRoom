# HeadRoom Android home-screen widget

This native Android layer is designed to live alongside the Capacitor-generated Android project. It mirrors HeadRoom tasks into Android SharedPreferences so an AppWidget can render them.

## Integration
1. Run `npm run build`, `npx cap add android`, then `npx cap sync android`.
2. Copy the contents of this folder into the corresponding paths under the generated `android/app/src/main/` project.
3. Add the receiver and service snippets from `AndroidManifest.widget.xml` to the generated application's `AndroidManifest.xml`.
4. Build the Android project normally.

The widget is intentionally native because Android launcher widgets cannot be implemented as Next.js DOM components.
