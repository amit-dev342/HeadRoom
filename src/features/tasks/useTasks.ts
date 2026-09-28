"use client";

import { useCallback, useEffect, useState } from "react";
import { readTaskSnapshot, writeTaskSnapshot } from "./taskRepository";
import type { Task, TaskDraft, TaskSnapshot } from "./types";
import { pushWidgetSnapshot, readWidgetSnapshot } from "./widgetGateway";

function makeSnapshot(tasks: Task[]): TaskSnapshot {
  return {
    tasks,
    updatedAt: Date.now(),
  };
}

export function useTasks() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [ready, setReady] = useState(false);

  const applySnapshot = useCallback((snapshot: TaskSnapshot) => {
    setTasks(snapshot.tasks);
    writeTaskSnapshot(snapshot);
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function initialize() {
      let local = readTaskSnapshot();

      if (local.tasks.length > 0 && local.updatedAt === 0) {
        local = { ...local, updatedAt: Date.now() };
        writeTaskSnapshot(local);
      }

      const native = await readWidgetSnapshot();
      if (cancelled) return;

      if (native && native.updatedAt > local.updatedAt) {
        applySnapshot(native);
      } else {
        applySnapshot(local);
        await pushWidgetSnapshot(local);
      }

      if (!cancelled) setReady(true);
    }

    void initialize();

    return () => {
      cancelled = true;
    };
  }, [applySnapshot]);

  useEffect(() => {
    async function pullLatestNativeState() {
      if (document.visibilityState !== "visible") return;

      const native = await readWidgetSnapshot();
      const local = readTaskSnapshot();

      if (native && native.updatedAt > local.updatedAt) {
        applySnapshot(native);
      } else if (!native || local.updatedAt > native.updatedAt) {
        await pushWidgetSnapshot(local);
      }
    }

    document.addEventListener("visibilitychange", pullLatestNativeState);
    window.addEventListener("focus", pullLatestNativeState);

    return () => {
      document.removeEventListener("visibilitychange", pullLatestNativeState);
      window.removeEventListener("focus", pullLatestNativeState);
    };
  }, [applySnapshot]);

  const commit = useCallback((nextTasks: Task[]) => {
    const snapshot = makeSnapshot(nextTasks);
    applySnapshot(snapshot);
    void pushWidgetSnapshot(snapshot);
  }, [applySnapshot]);

  const addTask = useCallback((draft: TaskDraft) => {
    commit([
      ...tasks,
      {
        id: Date.now(),
        ...draft,
        text: draft.text.trim(),
        done: false,
      },
    ]);
  }, [commit, tasks]);

  const updateTask = useCallback((taskId: number, draft: TaskDraft) => {
    commit(
      tasks.map((task) =>
        task.id === taskId
          ? { ...task, ...draft, text: draft.text.trim() }
          : task,
      ),
    );
  }, [commit, tasks]);

  const deleteTask = useCallback((taskId: number) => {
    commit(tasks.filter((task) => task.id !== taskId));
  }, [commit, tasks]);

  const toggleTask = useCallback((taskId: number) => {
    commit(
      tasks.map((task) =>
        task.id === taskId ? { ...task, done: !task.done } : task,
      ),
    );
  }, [commit, tasks]);

  return {
    tasks,
    ready,
    addTask,
    updateTask,
    deleteTask,
    toggleTask,
  };
}
