export type Priority = "high" | "medium" | "low";

export interface Task {
  id: number;
  text: string;
  priority: Priority;
  done: boolean;
  due: string;
  tag: string;
  notes: string;
}

export interface TaskDraft {
  text: string;
  priority: Priority;
  due: string;
  tag: string;
  notes: string;
}

export interface TaskSnapshot {
  tasks: Task[];
  updatedAt: number;
}

export type GroupingMode = "week" | "tag";

export interface AppPreferences {
  groupingMode: GroupingMode;
  collapsed: Record<string, boolean>;
}
