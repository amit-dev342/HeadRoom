package com.amit.headroom;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

public class HeadRoomWidget extends AppWidgetProvider {
    static final String ACTION_WIDGET_INTERACTION = "com.amit.headroom.ACTION_WIDGET_INTERACTION";
    static final String EXTRA_TASK_ID = "taskId";
    static final String EXTRA_INTERACTION = "interaction";
    static final String INTERACTION_CARD_TAP = "cardTap";
    static final String INTERACTION_ADD_TASK = "addTask";

    private static final int COMPACT_HEIGHT_DP = 150;
    private static final int DETAILED_HEIGHT_DP = 230;
    private static final long DOUBLE_TAP_WINDOW_MS = 550L;
    private static final String GESTURE_PREFS = "HeadRoomWidgetGestures";
    private static final String LAST_TAP_TASK_KEY = "lastTapTask";
    private static final String LAST_TAP_TIME_KEY = "lastTapTime";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        for (int widgetId : widgetIds) {
            update(context, manager, widgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(
            Context context,
            AppWidgetManager manager,
            int widgetId,
            Bundle newOptions
    ) {
        super.onAppWidgetOptionsChanged(context, manager, widgetId, newOptions);
        update(context, manager, widgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (!ACTION_WIDGET_INTERACTION.equals(intent.getAction())) return;

        String interaction = intent.getStringExtra(EXTRA_INTERACTION);

        if (INTERACTION_ADD_TASK.equals(interaction)) {
            WidgetTaskStore.requestAddTask(context);
            launchApp(context);
            return;
        }

        if (!INTERACTION_CARD_TAP.equals(interaction)) return;

        long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L);
        if (taskId < 0L || !isDoubleTap(context, taskId)) return;

        if (!WidgetTaskStore.toggle(context, taskId)) return;
        refreshData(context);
    }

    static void refreshData(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, HeadRoomWidget.class);
        int[] widgetIds = manager.getAppWidgetIds(provider);

        for (int widgetId : widgetIds) {
            update(context, manager, widgetId);
        }
        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widgetList);
    }

    static void update(Context context, AppWidgetManager manager, int widgetId) {
        int widgetHeightDp = getWidgetHeightDp(context, manager, widgetId);
        int displayMode;
        if (widgetHeightDp <= 0) {
            displayMode = HeadRoomWidgetService.MODE_DETAILED;
        } else if (widgetHeightDp < COMPACT_HEIGHT_DP) {
            displayMode = HeadRoomWidgetService.MODE_COMPACT;
        } else if (widgetHeightDp < DETAILED_HEIGHT_DP) {
            displayMode = HeadRoomWidgetService.MODE_TITLE_ONLY;
        } else {
            displayMode = HeadRoomWidgetService.MODE_DETAILED;
        }
        boolean compact = displayMode == HeadRoomWidgetService.MODE_COMPACT;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.headroom_widget);

        views.setViewVisibility(R.id.widgetHeader, compact ? View.GONE : View.VISIBLE);
        views.setViewVisibility(R.id.widgetCounts, compact ? View.GONE : View.VISIBLE);
        views.setViewPadding(
                R.id.widgetRoot,
                dp(context, compact ? 4 : 6),
                dp(context, compact ? 4 : 8),
                dp(context, compact ? 4 : 6),
                dp(context, compact ? 4 : 8)
        );
        views.setViewPadding(
                R.id.widgetList,
                0,
                compact ? 0 : dp(context, 6),
                0,
                0
        );

        Intent serviceIntent = new Intent(context, HeadRoomWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntent.putExtra(
                "snapshotUpdatedAt",
                WidgetTaskStore.readUpdatedAt(context)
        );
        serviceIntent.putExtra(HeadRoomWidgetService.EXTRA_DISPLAY_MODE, displayMode);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setRemoteAdapter(R.id.widgetList, serviceIntent);
        views.setEmptyView(R.id.widgetList, R.id.widgetEmpty);
        views.setDisplayedChild(R.id.widgetList, 0);

        Intent interactionIntent = new Intent(context, HeadRoomWidget.class);
        interactionIntent.setAction(ACTION_WIDGET_INTERACTION);
        PendingIntent interactionTemplate = PendingIntent.getBroadcast(
                context,
                widgetId,
                interactionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
        views.setPendingIntentTemplate(R.id.widgetList, interactionTemplate);

        int active = WidgetTaskStore.activeCount(context);
        int completed = WidgetTaskStore.completedCount(context);
        views.setTextViewText(
                R.id.widgetCounts,
                active + " active • " + completed + " completed"
        );

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

    private static int getWidgetHeightDp(
            Context context,
            AppWidgetManager manager,
            int widgetId
    ) {
        Bundle options = manager.getAppWidgetOptions(widgetId);
        boolean landscape = context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;

        int height = options.getInt(
                landscape
                        ? AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
                        : AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                0
        );

        if (height <= 0) {
            height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        }

        return height;
    }

    private static boolean isDoubleTap(Context context, long taskId) {
        SharedPreferences preferences = context.getSharedPreferences(
                GESTURE_PREFS,
                Context.MODE_PRIVATE
        );

        long now = SystemClock.elapsedRealtime();
        long previousTask = preferences.getLong(LAST_TAP_TASK_KEY, -1L);
        long previousTime = preferences.getLong(LAST_TAP_TIME_KEY, 0L);

        boolean doubleTap = previousTask == taskId
                && previousTime > 0L
                && now - previousTime <= DOUBLE_TAP_WINDOW_MS;

        if (doubleTap) {
            preferences.edit()
                    .remove(LAST_TAP_TASK_KEY)
                    .remove(LAST_TAP_TIME_KEY)
                    .apply();
        } else {
            preferences.edit()
                    .putLong(LAST_TAP_TASK_KEY, taskId)
                    .putLong(LAST_TAP_TIME_KEY, now)
                    .apply();
        }

        return doubleTap;
    }

    private static void launchApp(Context context) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(openIntent);
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
