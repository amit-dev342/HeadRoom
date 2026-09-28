"use client";

import { useRef } from "react";
import type { PointerEvent as ReactPointerEvent } from "react";
import { displayDate } from "@/features/tasks/taskUtils";
import type { Task } from "@/features/tasks/types";

interface TaskCardProps {
  task: Task;
  onToggle: (taskId: number) => void;
  onEdit: (task: Task) => void;
  onDelete: (taskId: number) => void;
}

const LONG_PRESS_MS = 650;
const MOVE_CANCEL_DISTANCE = 12;

export default function TaskCard({
  task,
  onToggle,
  onEdit,
  onDelete,
}: TaskCardProps) {
  const holdTimer = useRef<number | null>(null);
  const holdStart = useRef<{ x: number; y: number } | null>(null);

  function cancelHold() {
    if (holdTimer.current !== null) {
      window.clearTimeout(holdTimer.current);
      holdTimer.current = null;
    }
    holdStart.current = null;
  }

  function startHold(event: ReactPointerEvent<HTMLElement>) {
    const target = event.target as HTMLElement;

    if (target.closest("button, a, input, textarea, select")) return;

    cancelHold();
    holdStart.current = { x: event.clientX, y: event.clientY };
    holdTimer.current = window.setTimeout(() => {
      holdTimer.current = null;
      holdStart.current = null;

      if ("vibrate" in navigator) {
        navigator.vibrate?.(35);
      }

      onDelete(task.id);
    }, LONG_PRESS_MS);
  }

  function trackHold(event: ReactPointerEvent<HTMLElement>) {
    if (!holdStart.current || holdTimer.current === null) return;

    const distance = Math.hypot(
      event.clientX - holdStart.current.x,
      event.clientY - holdStart.current.y,
    );

    if (distance > MOVE_CANCEL_DISTANCE) {
      cancelHold();
    }
  }

  return (
    <article
      className={"task " + task.priority + (task.done ? " done" : "") + " expanded card"}
      onPointerDown={startHold}
      onPointerMove={trackHold}
      onPointerUp={cancelHold}
      onPointerCancel={cancelHold}
      onPointerLeave={cancelHold}
      onContextMenu={(event) => event.preventDefault()}
    >
      <button
        className="check"
        onClick={() => onToggle(task.id)}
        aria-label={task.done ? "Mark active" : "Mark complete"}
      >
        {task.done ? "✓" : ""}
      </button>

      <div className="taskBody">
        <strong className="taskTitle">{task.text}</strong>

        <div className="chips">
          {task.due && <span>{displayDate(task.due)}</span>}
          {task.tag && <span>{task.tag}</span>}
          <span>{task.priority.toUpperCase()}</span>
        </div>

        {task.notes && <p>{task.notes}</p>}
      </div>

      <button className="more" onClick={() => onEdit(task)} aria-label="Edit task">
        ⋮
      </button>
    </article>
  );
}
