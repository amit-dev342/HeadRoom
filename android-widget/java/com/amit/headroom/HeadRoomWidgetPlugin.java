package com.amit.headroom;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "HeadRoomWidget")
public class HeadRoomWidgetPlugin extends Plugin {
    @PluginMethod
    public void syncTasks(PluginCall call) {
        String tasksJson = call.getString("tasks", "[]");
        long updatedAt = System.currentTimeMillis();

        try {
            String updatedAtValue = call.getString("updatedAt");
            if (updatedAtValue != null) {
                updatedAt = Long.parseLong(updatedAtValue);
            }

            WidgetTaskStore.save(getContext(), tasksJson, updatedAt);
            refreshWidgets();

            JSObject result = new JSObject();
            result.put("updatedAt", String.valueOf(updatedAt));
            result.put("count", WidgetTaskStore.readTasks(getContext()).length());
            call.resolve(result);
        } catch (Exception error) {
            call.reject("Unable to sync widget tasks", error);
        }
    }

    @PluginMethod
    public void getTaskSnapshot(PluginCall call) {
        JSObject result = new JSObject();
        result.put("tasks", WidgetTaskStore.readTasksJson(getContext()));
        result.put("updatedAt", String.valueOf(WidgetTaskStore.readUpdatedAt(getContext())));
        call.resolve(result);
    }

    private void refreshWidgets() {
        HeadRoomWidget.refreshData(getContext());
    }
}
