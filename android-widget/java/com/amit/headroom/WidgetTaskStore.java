package com.amit.headroom;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

final class WidgetTaskStore {
    private static final String PREFS_NAME = "HeadRoomWidget";
    private static final String TASKS_KEY = "tasks";
    private static final String UPDATED_AT_KEY = "updatedAt";

    private WidgetTaskStore() {}

    static JSONArray readTasks(Context context) {
        try {
            return new JSONArray(readTasksJson(context));
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    static String readTasksJson(Context context) {
        return preferences(context).getString(TASKS_KEY, "[]");
    }

    static long readUpdatedAt(Context context) {
        return preferences(context).getLong(UPDATED_AT_KEY, 0L);
    }

    static int activeCount(Context context) {
        JSONArray tasks = readTasks(context);
        int count = 0;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && !task.optBoolean("done", false)) count++;
        }
        return count;
    }

    static int completedCount(Context context) {
        JSONArray tasks = readTasks(context);
        int count = 0;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && task.optBoolean("done", false)) count++;
        }
        return count;
    }

    static void save(Context context, String tasksJson, long updatedAt) throws Exception {
        JSONArray validated = new JSONArray(tasksJson);
        preferences(context)
                .edit()
                .putString(TASKS_KEY, validated.toString())
                .putLong(UPDATED_AT_KEY, updatedAt)
                .commit();
    }

    static boolean toggle(Context context, long taskId) {
        JSONArray tasks = readTasks(context);
        boolean changed = false;

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task != null && task.optLong("id", -1L) == taskId) {
                try {
                    task.put("done", !task.optBoolean("done", false));
                    changed = true;
                } catch (Exception ignored) {
                    return false;
                }
                break;
            }
        }

        if (!changed) return false;

        try {
            save(context, tasks.toString(), System.currentTimeMillis());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
