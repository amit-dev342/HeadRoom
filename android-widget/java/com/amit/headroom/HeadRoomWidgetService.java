package com.amit.headroom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class HeadRoomWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static final class Factory implements RemoteViewsFactory {
        private final Context context;
        private JSONArray tasks = new JSONArray();

        Factory(Context context) {
            this.context = context;
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
            String due = task.optString("due", "");
            String tag = task.optString("tag", "").trim();
            String notes = task.optString("notes", "").trim();

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.headroom_widget_task);
            views.setInt(R.id.taskCard, "setBackgroundResource", backgroundFor(done, priority));
            views.setTextViewText(R.id.taskStatus, done ? "COMPLETED" : "ACTIVE");
            views.setTextViewText(R.id.taskPriority, priority.toUpperCase(Locale.ROOT) + " PRIORITY");
            views.setTextViewText(R.id.taskTitle, task.optString("text", ""));
            views.setTextViewText(R.id.taskWeek, weekLabel(due));
            views.setTextViewText(R.id.taskDue, dueLabel(due));
            views.setTextViewText(R.id.taskTag, tag.isEmpty() ? "UNTAGGED" : tag.toUpperCase(Locale.ROOT));
            views.setTextViewText(R.id.taskNotes, notes.isEmpty() ? "No notes" : notes);
            views.setTextViewText(R.id.taskAction, done ? "✓  MARK ACTIVE" : "○  MARK COMPLETE");
            views.setInt(
                    R.id.taskTitle,
                    "setPaintFlags",
                    done ? Paint.STRIKE_THRU_TEXT_FLAG : 0
            );

            Intent action = new Intent();
            action.putExtra(HeadRoomWidget.EXTRA_TASK_ID, task.optLong("id", -1L));
            views.setOnClickFillInIntent(R.id.taskAction, action);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
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

        private String weekLabel(String due) {
            if (due.isEmpty()) return "No scheduled week";
            try {
                Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(due);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parsed);

                int dayOffset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                calendar.add(Calendar.DATE, -dayOffset);
                Date start = calendar.getTime();

                calendar.add(Calendar.DATE, 6);
                Date end = calendar.getTime();

                return new SimpleDateFormat("d MMM", Locale.US).format(start)
                        + " – "
                        + new SimpleDateFormat("d MMM yyyy", Locale.US).format(end);
            } catch (Exception ignored) {
                return "";
            }
        }
    }
}
