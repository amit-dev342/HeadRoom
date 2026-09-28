package com.amit.headroom;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginMethod;

@CapacitorPlugin(name = "HeadRoomWidget")
public class HeadRoomWidgetPlugin extends Plugin {
    @PluginMethod
    public void syncTasks(PluginCall call) {
        String tasks = call.getString("tasks", "[]");
        getContext().getSharedPreferences("HeadRoomWidget", Context.MODE_PRIVATE)
                .edit().putString("tasks", tasks).commit();

        AppWidgetManager manager = AppWidgetManager.getInstance(getContext());
        ComponentName provider = new ComponentName(getContext(), HeadRoomWidget.class);
        int[] widgetIds = manager.getAppWidgetIds(provider);
        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widgetList);
        for (int widgetId : widgetIds) {
            HeadRoomWidget.update(getContext(), manager, widgetId);
        }
        call.resolve(new JSObject());
    }
}
