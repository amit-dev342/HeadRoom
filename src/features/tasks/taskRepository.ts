import type { AppPreferences, TaskSnapshot } from "./types";

const TASKS_KEY = "headroom.tasks";
const TASKS_UPDATED_AT_KEY = "headroom.tasks.updatedAt";
const PREFERENCES_KEY = "headroom.preferences";

const DEFAULT_PREFERENCES: AppPreferences = {
  groupingMode: "week",
  collapsed: {},
};

export function readTaskSnapshot(): TaskSnapshot {
  if (typeof window === "undefined") return { tasks: [], updatedAt: 0 };

  try {
    const tasks = JSON.parse(localStorage.getItem(TASKS_KEY) ?? "[]");
    const updatedAt = Number(localStorage.getItem(TASKS_UPDATED_AT_KEY) ?? "0");
    return { tasks: Array.isArray(tasks) ? tasks : [], updatedAt };
  } catch {
    return { tasks: [], updatedAt: 0 };
  }
}

export function writeTaskSnapshot(snapshot: TaskSnapshot) {
  localStorage.setItem(TASKS_KEY, JSON.stringify(snapshot.tasks));
  localStorage.setItem(TASKS_UPDATED_AT_KEY, String(snapshot.updatedAt));
}

export function readPreferences(): AppPreferences {
  if (typeof window === "undefined") return DEFAULT_PREFERENCES;

  try {
    const value = JSON.parse(localStorage.getItem(PREFERENCES_KEY) ?? "{}");
    return {
      groupingMode: value.groupingMode === "tag" ? "tag" : "week",
      collapsed: value.collapsed && typeof value.collapsed === "object" ? value.collapsed : {},
    };
  } catch {
    return DEFAULT_PREFERENCES;
  }
}

export function writePreferences(preferences: AppPreferences) {
  localStorage.setItem(PREFERENCES_KEY, JSON.stringify(preferences));
}
