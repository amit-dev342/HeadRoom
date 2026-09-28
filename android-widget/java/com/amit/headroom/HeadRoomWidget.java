package com.amit.headroom;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class HeadRoomWidget extends AppWidgetProvider {
    static final String ACTION_TOGGLE_TASK = "com.amit.headroom.ACTION_TOGGLE_TASK";
    static final String EXTRA_TASK_ID = "taskId";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        for (int widgetId : widgetIds) {
            update(context, manager, widgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (!ACTION_TOGGLE_TASK.equals(intent.getAction())) return;

        long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L);
        if (taskId < 0L || !WidgetTaskStore.toggle(context, taskId)) return;

        refreshAll(context);
    }

    static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, HeadRoomWidget.class);
        int[] widgetIds = manager.getAppWidgetIds(provider);

        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widgetList);
        for (int widgetId : widgetIds) {
            update(context, manager, widgetId);
        }
    }

    static void update(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.headroom_widget);

        Intent serviceIntent = new Intent(context, HeadRoomWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widgetList, serviceIntent);
        views.setEmptyView(R.id.widgetList, R.id.widgetEmpty);

        Intent toggleIntent = new Intent(context, HeadRoomWidget.class);
        toggleIntent.setAction(ACTION_TOGGLE_TASK);
        PendingIntent toggleTemplate = PendingIntent.getBroadcast(
                context,
                widgetId,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
        views.setPendingIntentTemplate(R.id.widgetList, toggleTemplate);

        int active = WidgetTaskStore.activeCount(context);
        int completed = WidgetTaskStore.completedCount(context);
        views.setTextViewText(R.id.widgetCounts, active + " active • " + completed + " completed");

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openApp = PendingIntent.getActivity(
                context,
                10_000 + widgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widgetTitle, openApp);

        manager.updateAppWidget(widgetId, views);
    }
}
