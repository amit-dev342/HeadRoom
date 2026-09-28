import { Capacitor, registerPlugin } from "@capacitor/core";
import type { TaskSnapshot } from "./types";

interface NativeTaskSnapshot {
  tasks: string;
  updatedAt: string;
}

interface HeadRoomWidgetPlugin {
  syncTasks(input: { tasks: string; updatedAt: string }): Promise<{ count: number; updatedAt: string }>;
  getTaskSnapshot(): Promise<NativeTaskSnapshot>;
  consumeAddTaskRequest(): Promise<{ requested: boolean }>;
}

const HeadRoomWidget = registerPlugin<HeadRoomWidgetPlugin>("HeadRoomWidget");

export async function pushWidgetSnapshot(snapshot: TaskSnapshot) {
  if (!Capacitor.isNativePlatform()) return false;

  for (let attempt = 0; attempt < 3; attempt += 1) {
    try {
      await HeadRoomWidget.syncTasks({
        tasks: JSON.stringify(snapshot.tasks),
        updatedAt: String(snapshot.updatedAt),
      });
      return true;
    } catch {
      if (attempt < 2) {
        await new Promise((resolve) => setTimeout(resolve, 250 * (attempt + 1)));
      }
    }
  }

  return false;
}

export async function readWidgetSnapshot(): Promise<TaskSnapshot | null> {
  if (!Capacitor.isNativePlatform()) return null;

  try {
    const snapshot = await HeadRoomWidget.getTaskSnapshot();
    const tasks = JSON.parse(snapshot.tasks);
    return {
      tasks: Array.isArray(tasks) ? tasks : [],
      updatedAt: Number(snapshot.updatedAt || "0"),
    };
  } catch {
    return null;
  }
}


export async function consumeWidgetAddTaskRequest() {
  if (!Capacitor.isNativePlatform()) return false;

  try {
    const result = await HeadRoomWidget.consumeAddTaskRequest();
    return Boolean(result.requested);
  } catch {
    return false;
  }
}
