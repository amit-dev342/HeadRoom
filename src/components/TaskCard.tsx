"use client";

import { displayDate } from "@/features/tasks/taskUtils";
import type { Task } from "@/features/tasks/types";

interface TaskCardProps {
  task: Task;
  onToggle: (taskId: number) => void;
  onEdit: (task: Task) => void;
}

export default function TaskCard({ task, onToggle, onEdit }: TaskCardProps) {
  return (
    <article className={`task ${task.priority} ${task.done ? "done" : ""} expanded card`}>
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
