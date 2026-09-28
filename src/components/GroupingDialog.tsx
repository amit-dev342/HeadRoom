"use client";

import type { GroupingMode } from "@/features/tasks/types";

interface GroupingDialogProps {
  mode: GroupingMode;
  onChange: (mode: GroupingMode) => void;
  onClose: () => void;
}

export default function GroupingDialog({
  mode,
  onChange,
  onClose,
}: GroupingDialogProps) {
  return (
    <div
      className="overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div className="dialog">
        <h2>Group tasks by</h2>

        <div className="filterChoices">
          <label>
            <input
              type="radio"
              name="grouping"
              checked={mode === "week"}
              onChange={() => onChange("week")}
            />
            <span>Weekly</span>
          </label>

          <label>
            <input
              type="radio"
              name="grouping"
              checked={mode === "tag"}
              onChange={() => onChange("tag")}
            />
            <span>Tag</span>
          </label>
        </div>

        <div className="actions">
          <span />
          <button className="primary" onClick={onClose}>
            APPLY
          </button>
        </div>
      </div>
    </div>
  );
}
