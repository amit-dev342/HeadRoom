package com.amit.headroom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class HeadRoomWidgetService extends RemoteViewsService {
    static final String EXTRA_COMPACT = "compact";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        boolean compact = intent.getBooleanExtra(EXTRA_COMPACT, false);
        return new Factory(getApplicationContext(), compact);
    }

    private static final class Factory implements RemoteViewsFactory {
        private final Context context;
        private final boolean compact;
        private JSONArray tasks = new JSONArray();

        Factory(Context context, boolean compact) {
            this.context = context;
            this.compact = compact;
        }

        @Override
        public void onCreate() {
            reload();
        }

        @Override
        public void onDataSetChanged() {
            reload();
        }

        @Override
        public void onDestroy() {}

        @Override
        public int getCount() {
            return tasks.length();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= tasks.length()) return null;

            JSONObject task = tasks.optJSONObject(position);
            if (task == null) return null;

            boolean done = task.optBoolean("done", false);
            String priority = task.optString("priority", "medium");

            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    compact
                            ? R.layout.headroom_widget_task_compact
                            : R.layout.headroom_widget_task
            );

            views.setInt(R.id.taskCard, "setBackgroundResource", backgroundFor(done, priority));
            views.setTextViewText(R.id.taskStatus, done ? "COMPLETED" : "ACTIVE");
            views.setTextViewText(
                    R.id.taskPriority,
                    priority.toUpperCase(Locale.ROOT) + " PRIORITY"
            );
            views.setTextViewText(R.id.taskTitle, task.optString("text", ""));
            views.setInt(
                    R.id.taskTitle,
                    "setPaintFlags",
                    done ? Paint.STRIKE_THRU_TEXT_FLAG : 0
            );

            long taskId = task.optLong("id", -1L);

            if (compact) {
                Intent cardTap = new Intent();
                cardTap.putExtra(HeadRoomWidget.EXTRA_TASK_ID, taskId);
                cardTap.putExtra(
                        HeadRoomWidget.EXTRA_INTERACTION,
                        HeadRoomWidget.INTERACTION_CARD_TAP
                );
                views.setOnClickFillInIntent(R.id.taskCard, cardTap);
            } else {
                String due = task.optString("due", "");
                String tag = task.optString("tag", "").trim();

                views.setTextViewText(R.id.taskDue, dueLabel(due));
                views.setTextViewText(
                        R.id.taskTag,
                        tag.isEmpty() ? "UNTAGGED" : tag.toUpperCase(Locale.ROOT)
                );
                views.setTextViewText(
                        R.id.taskAction,
                        done ? "✓  MARK ACTIVE" : "○  MARK COMPLETE"
                );

                Intent action = new Intent();
                action.putExtra(HeadRoomWidget.EXTRA_TASK_ID, taskId);
                action.putExtra(
                        HeadRoomWidget.EXTRA_INTERACTION,
                        HeadRoomWidget.INTERACTION_TOGGLE
                );
                views.setOnClickFillInIntent(R.id.taskAction, action);
            }

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            JSONObject task = tasks.optJSONObject(position);
            return task == null ? position : task.optLong("id", position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void reload() {
            tasks = WidgetTaskStore.readTasks(context);
        }

        private int backgroundFor(boolean done, String priority) {
            if (done) return R.drawable.widget_card_done;
            if ("high".equals(priority)) return R.drawable.widget_card_high;
            if ("low".equals(priority)) return R.drawable.widget_card_low;
            return R.drawable.widget_card;
        }

        private String dueLabel(String due) {
            if (due.isEmpty()) return "NO DUE DATE";
            try {
                Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(due);
                return new SimpleDateFormat("EEE, d MMM", Locale.US).format(parsed);
            } catch (Exception ignored) {
                return due;
            }
        }
    }
}
