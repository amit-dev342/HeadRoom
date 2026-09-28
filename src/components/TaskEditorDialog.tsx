"use client";

import { useRef, useState } from "react";
import {
  EMPTY_TASK_DRAFT,
  toLocalIsoDate,
} from "@/features/tasks/taskUtils";
import type {
  Priority,
  Task,
  TaskDraft,
} from "@/features/tasks/types";

interface TaskEditorDialogProps {
  task: Task | null;
  onSave: (draft: TaskDraft) => void;
  onDelete?: () => void;
  onClose: () => void;
}

export default function TaskEditorDialog({
  task,
  onSave,
  onDelete,
  onClose,
}: TaskEditorDialogProps) {
  const dateInputRef = useRef<HTMLInputElement>(null);
  const [draft, setDraft] = useState<TaskDraft>(
    task
      ? {
          text: task.text,
          due: task.due,
          tag: task.tag,
          notes: task.notes,
          priority: task.priority,
        }
      : EMPTY_TASK_DRAFT,
  );

  const today = toLocalIsoDate();

  function submit() {
    if (!draft.text.trim()) return;
    onSave({ ...draft, text: draft.text.trim() });
  }

  return (
    <div
      className="overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div className="dialog">
        <h2>{task ? "Edit task" : "New task"}</h2>

        <input
          autoFocus
          placeholder="Task title"
          value={draft.text}
          onChange={(event) => setDraft({ ...draft, text: event.target.value })}
        />

        <div
          className={`dateField ${draft.due ? "hasValue" : ""}`}
          onClick={() => dateInputRef.current?.showPicker?.()}
        >
          <input
            ref={dateInputRef}
            type="date"
            aria-label="Due date"
            min={today}
            value={draft.due}
            onChange={(event) => setDraft({ ...draft, due: event.target.value })}
          />
          {!draft.due && <span className="datePlaceholder">Due date</span>}
        </div>

        <input
          placeholder="Tag (e.g. Personal)"
          value={draft.tag}
          onChange={(event) => setDraft({ ...draft, tag: event.target.value })}
        />

        <textarea
          placeholder="Notes (optional)"
          value={draft.notes}
          onChange={(event) => setDraft({ ...draft, notes: event.target.value })}
        />

        <div className="radios">
          {(["high", "medium", "low"] as Priority[]).map((priority) => (
            <label key={priority}>
              <input
                type="radio"
                name="priority"
                checked={draft.priority === priority}
                onChange={() => setDraft({ ...draft, priority })}
              />
              {priority[0].toUpperCase() + priority.slice(1)}
            </label>
          ))}
        </div>

        <div className="actions">
          {task && onDelete && (
            <button
              className="danger"
              onClick={() => {
                if (window.confirm("Delete this task?")) onDelete();
              }}
            >
              DELETE
            </button>
          )}
          <span />
          <button onClick={onClose}>CANCEL</button>
          <button className="primary" onClick={submit}>
            {task ? "SAVE" : "ADD"}
          </button>
        </div>
      </div>
    </div>
  );
}
