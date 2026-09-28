import { Capacitor, registerPlugin } from "@capacitor/core";
import type { Task } from "./tasks";

interface HeadRoomWidgetPlugin {
  syncTasks(input: { tasks: string }): Promise<void>;
}

const HeadRoomWidget = registerPlugin<HeadRoomWidgetPlugin>("HeadRoomWidget");

export async function syncWidgetTasks(tasks: Task[]) {
  if (!Capacitor.isNativePlatform()) return;
  try {
    await HeadRoomWidget.syncTasks({ tasks: JSON.stringify(tasks) });
  } catch (error) {
    console.error("Unable to sync HeadRoom widget tasks", error);
  }
}
