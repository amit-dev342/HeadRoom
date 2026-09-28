"use client";

import { useEffect, useMemo, useState } from "react";
import GroupingDialog from "./GroupingDialog";
import TaskCard from "./TaskCard";
import TaskEditorDialog from "./TaskEditorDialog";
import TaskSection from "./TaskSection";
import {
  readPreferences,
  writePreferences,
} from "@/features/tasks/taskRepository";
import {
  groupTasksByTag,
  groupTasksByWeek,
  weekLabel,
} from "@/features/tasks/taskUtils";
import type {
  GroupingMode,
  Task,
  TaskDraft,
} from "@/features/tasks/types";
import { useTasks } from "@/features/tasks/useTasks";
import { consumeWidgetAddTaskRequest } from "@/features/tasks/widgetGateway";

export default function HeadRoom() {
  const {
    tasks,
    ready,
    addTask,
    updateTask,
    deleteTask,
    toggleTask,
  } = useTasks();

  const [groupingMode, setGroupingMode] = useState<GroupingMode>("week");
  const [collapsed, setCollapsed] = useState<Record<string, boolean>>({});
  const [filterOpen, setFilterOpen] = useState(false);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  useEffect(() => {
    const preferences = readPreferences();
    setGroupingMode(preferences.groupingMode);
    setCollapsed(preferences.collapsed);
  }, []);

  useEffect(() => {
    if (!ready) return;
    writePreferences({ groupingMode, collapsed });
  }, [collapsed, groupingMode, ready]);

  useEffect(() => {
    let cancelled = false;

    async function openRequestedTaskEditor() {
      const requested = await consumeWidgetAddTaskRequest();
      if (!cancelled && requested) {
        setEditingTask(null);
        setEditorOpen(true);
      }
    }

    void openRequestedTaskEditor();
    window.addEventListener("focus", openRequestedTaskEditor);

    return () => {
      cancelled = true;
      window.removeEventListener("focus", openRequestedTaskEditor);
    };
  }, []);


  const weekGroups = useMemo(() => groupTasksByWeek(tasks), [tasks]);
  const tagGroups = useMemo(() => groupTasksByTag(tasks), [tasks]);
  const activeCount = tasks.filter((task) => !task.done).length;

  function openNewTask() {
    setEditingTask(null);
    setEditorOpen(true);
  }

  function openEditTask(task: Task) {
    setEditingTask(task);
    setEditorOpen(true);
  }

  function closeEditor() {
    setEditorOpen(false);
    setEditingTask(null);
  }

  function saveTask(draft: TaskDraft) {
    if (editingTask) {
      updateTask(editingTask.id, draft);
    } else {
      addTask(draft);
    }
    closeEditor();
  }

  function removeEditingTask() {
    if (!editingTask) return;
    deleteTask(editingTask.id);
    closeEditor();
  }

  function toggleSection(key: string) {
    setCollapsed((current) => ({
      ...current,
      [key]: !current[key],
    }));
  }

  if (!ready) return null;

  return (
    <main>
      <header>
        <div>
          <div className="brand">HEADROOM</div>
          <p>MAKE SPACE. MOVE FORWARD.</p>
        </div>
        <button className="view" onClick={() => setFilterOpen(true)}>
          FILTER
        </button>
      </header>

      <div className="sectionTitle">
        <h1>MY TASKS</h1>
        <span>{activeCount} ACTIVE</span>
      </div>

      {tasks.length === 0 ? (
        <div className="empty">
          <b>✓</b>
          <h2>Nothing pending</h2>
          <p>Your space is clear. Add something when it matters.</p>
        </div>
      ) : (
        <div className="weeks">
          {groupingMode === "week"
            ? weekGroups.map(([start, items]) => {
                const key = `week-${start}`;
                return (
                  <TaskSection
                    key={key}
                    label={weekLabel(start)}
                    count={items.length}
                    collapsed={Boolean(collapsed[key])}
                    onToggle={() => toggleSection(key)}
                  >
                    {items.map((task) => (
                      <TaskCard
                        key={task.id}
                        task={task}
                        onToggle={toggleTask}
                        onEdit={openEditTask}
                      />
                    ))}
                  </TaskSection>
                );
              })
            : tagGroups.map((group) => {
                const key = `tag-${group.label.toLocaleLowerCase()}`;
                return (
                  <TaskSection
                    key={key}
                    label={group.label}
                    count={group.items.length}
                    collapsed={Boolean(collapsed[key])}
                    onToggle={() => toggleSection(key)}
                  >
                    {group.items.map((task) => (
                      <TaskCard
                        key={task.id}
                        task={task}
                        onToggle={toggleTask}
                        onEdit={openEditTask}
                      />
                    ))}
                  </TaskSection>
                );
              })}
        </div>
      )}

      <button className="fab" onClick={openNewTask} aria-label="Add task">
        +
      </button>

      {editorOpen && (
        <TaskEditorDialog
          key={editingTask?.id ?? "new-task"}
          task={editingTask}
          onSave={saveTask}
          onDelete={editingTask ? removeEditingTask : undefined}
          onClose={closeEditor}
        />
      )}

      {filterOpen && (
        <GroupingDialog
          mode={groupingMode}
          onChange={setGroupingMode}
          onClose={() => setFilterOpen(false)}
        />
      )}
    </main>
  );
}
