"use client";

import type { ReactNode } from "react";

interface TaskSectionProps {
  label: string;
  count: number;
  collapsed: boolean;
  onToggle: () => void;
  children: ReactNode;
}

export default function TaskSection({
  label,
  count,
  collapsed,
  onToggle,
  children,
}: TaskSectionProps) {
  return (
    <section className="week">
      <button className="weekHead" onClick={onToggle}>
        <span className="weekLabel">
          <svg
            className={`chevron ${collapsed ? "" : "open"}`}
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="M9 19L16 12L9 5" />
          </svg>
          <span>{label}</span>
        </span>
        <span>{count}</span>
      </button>

      {!collapsed && <div className="weekBody">{children}</div>}
    </section>
  );
}
