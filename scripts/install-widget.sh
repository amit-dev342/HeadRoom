#!/usr/bin/env bash
set -euo pipefail
BASE="android/app/src/main"
SRC="android-widget"
PKG="$BASE/java/com/amit/headroom"
mkdir -p "$PKG" "$BASE/res/xml" "$BASE/res/layout" "$BASE/res/drawable" "$BASE/res/raw"
cp "$SRC"/java/com/amit/headroom/*.java "$PKG"/
cp "$SRC"/res/xml/*.xml "$BASE/res/xml"/
cp "$SRC"/res/layout/*.xml "$BASE/res/layout"/
cp "$SRC"/res/drawable/*.xml "$BASE/res/drawable"/
cp "$SRC"/res/raw/* "$BASE/res/raw"/
MANIFEST="$BASE/AndroidManifest.xml"
python3 - "$MANIFEST" <<'PY'
from pathlib import Path
import sys
p=Path(sys.argv[1]);s=p.read_text()
nodes='''\n        <receiver android:name=".HeadRoomWidget" android:exported="true">\n            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>\n            <meta-data android:name="android.appwidget.provider" android:resource="@xml/headroom_widget_info" />\n        </receiver>\n        <service android:name=".HeadRoomWidgetService" android:permission="android.permission.BIND_REMOTEVIEWS" android:exported="false" />\n'''
if 'android:name=".HeadRoomWidget"' not in s:s=s.replace('</application>',nodes+'    </application>')
p.write_text(s)
PY
MAIN="$PKG/MainActivity.java"
cat > "$MAIN" <<'JAVA'
package com.amit.headroom;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(HeadRoomWidgetPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
JAVA
