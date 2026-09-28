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
    static final String EXTRA_DISPLAY_MODE = "displayMode";
    static final String EXTRA_COMPACT = "compact";
    static final int MODE_COMPACT_TITLE = 0;
    static final int MODE_TITLE_ONLY = 1;
    static final int MODE_SUMMARY = 2;
    static final int MODE_DETAILED = 3;

    private static final int MAX_TASKS_BEFORE_ADD_CARD = 3;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int displayMode = intent.getIntExtra(EXTRA_DISPLAY_MODE, MODE_DETAILED);
        boolean compact = intent.getBooleanExtra(EXTRA_COMPACT, false);
        return new Factory(getApplicationContext(), displayMode, compact);
    }

    private static final class Factory implements RemoteViewsFactory {
        private final Context context;
        private final int displayMode;
        private final boolean compact;
        private JSONArray tasks = new JSONArray();

        Factory(Context context, int displayMode, boolean compact) {
            this.context = context;
            this.displayMode = displayMode;
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
            return tasks.length() + 2;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == addCardPosition()) {
                return buildAddTaskCard();
            }
            if (position == missedCardPosition()) {
                return buildMissedCard();
            }

            int taskIndex = taskIndexForPosition(position);
            if (taskIndex < 0 || taskIndex >= tasks.length()) return null;

            JSONObject task = tasks.optJSONObject(taskIndex);
            if (task == null) return null;

            return buildTaskCard(task);
        }

        private int taskIndexForPosition(int position) {
            int addPosition = addCardPosition();
            int missedPosition = missedCardPosition();

            if (position < addPosition) {
                return position;
            }
            if (position > missedPosition) {
                return position - 2;
            }
            return -1;
        }

        private RemoteViews buildTaskCard(JSONObject task) {
            boolean done = task.optBoolean("done", false);
            String priority = task.optString("priority", "medium");
            long taskId = task.optLong("id", -1L);

            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    taskLayout()
            );

            views.setInt(R.id.taskCard, "setBackgroundResource", backgroundFor(done, priority));

            if (displayMode == MODE_COMPACT_TITLE || displayMode == MODE_TITLE_ONLY) {
                setTaskTitle(views, task, done);
            } else if (displayMode == MODE_SUMMARY) {
                views.setTextViewText(R.id.taskStatus, done ? "COMPLETED" : "ACTIVE");
                views.setTextViewText(
                        R.id.taskPriority,
                        priority.toUpperCase(Locale.ROOT) + " PRIORITY"
                );
                setTaskTitle(views, task, done);
            } else {
                views.setTextViewText(R.id.taskStatus, done ? "COMPLETED" : "ACTIVE");
                views.setTextViewText(
                        R.id.taskPriority,
                        priority.toUpperCase(Locale.ROOT) + " PRIORITY"
                );
                setTaskTitle(views, task, done);

                String due = task.optString("due", "");
                String tag = task.optString("tag", "").trim();

                views.setTextViewText(R.id.taskDue, dueLabel(due));
                views.setTextViewText(
                        R.id.taskTag,
                        tag.isEmpty() ? "UNTAGGED" : tag.toUpperCase(Locale.ROOT)
                );
            }

            Intent cardTap = new Intent();
            cardTap.putExtra(HeadRoomWidget.EXTRA_TASK_ID, taskId);
            cardTap.putExtra(
                    HeadRoomWidget.EXTRA_INTERACTION,
                    HeadRoomWidget.INTERACTION_CARD_TAP
            );
            views.setOnClickFillInIntent(R.id.taskCard, cardTap);

            return views;
        }

        private void setTaskTitle(RemoteViews views, JSONObject task, boolean done) {
            views.setTextViewText(R.id.taskTitle, task.optString("text", ""));
            views.setInt(
                    R.id.taskTitle,
                    "setPaintFlags",
                    done ? Paint.STRIKE_THRU_TEXT_FLAG : 0
            );
        }

        private int taskLayout() {
            if (displayMode == MODE_COMPACT_TITLE) {
                return R.layout.headroom_widget_task_compact;
            }
            if (displayMode == MODE_TITLE_ONLY) {
                return R.layout.headroom_widget_task_title_only;
            }
            if (displayMode == MODE_SUMMARY) {
                return R.layout.headroom_widget_task_summary;
            }
            return R.layout.headroom_widget_task;
        }

        private RemoteViews buildAddTaskCard() {
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    compact
                            ? R.layout.headroom_widget_add_task_compact
                            : R.layout.headroom_widget_add_task
            );

            Intent addTask = new Intent();
            addTask.putExtra(
                    HeadRoomWidget.EXTRA_INTERACTION,
                    HeadRoomWidget.INTERACTION_ADD_TASK
            );
            views.setOnClickFillInIntent(R.id.addTaskCard, addTask);
            return views;
        }

        private RemoteViews buildMissedCard() {
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    compact
                            ? R.layout.headroom_widget_missed_compact
                            : R.layout.headroom_widget_missed
            );
            views.setTextViewText(
                    R.id.missedCount,
                    "Missed: " + WidgetTaskStore.missedCount(context)
            );
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public long getItemId(int position) {
            if (position == addCardPosition()) {
                return Long.MAX_VALUE;
            }
            if (position == missedCardPosition()) {
                return Long.MAX_VALUE - 1;
            }

            int taskIndex = taskIndexForPosition(position);
            JSONObject task = tasks.optJSONObject(taskIndex);
            return task == null ? position : task.optLong("id", position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void reload() {
            tasks = WidgetTaskStore.readTasks(context);
        }

        private int addCardPosition() {
            return Math.min(MAX_TASKS_BEFORE_ADD_CARD, tasks.length());
        }

        private int missedCardPosition() {
            return addCardPosition() + 1;
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
