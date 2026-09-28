import type { Task, TaskDraft } from "./types";

export const EMPTY_TASK_DRAFT: TaskDraft = {
  text: "",
  due: "",
  tag: "",
  notes: "",
  priority: "medium",
};

export function toLocalIsoDate(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function displayDate(value: string) {
  if (!value) return "";
  return new Date(`${value}T00:00:00`).toLocaleDateString(undefined, {
    weekday: "short",
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

export function weekStart(task: Task) {
  const date = task.due ? new Date(`${task.due}T00:00:00`) : new Date(task.id);
  const day = date.getDay();
  date.setDate(date.getDate() - (day === 0 ? 6 : day - 1));
  date.setHours(0, 0, 0, 0);
  return date.getTime();
}

export function weekLabel(start: number) {
  const first = new Date(start);
  const last = new Date(start);
  last.setDate(first.getDate() + 6);

  const short = (date: Date) =>
    date.toLocaleDateString(undefined, { day: "numeric", month: "short" });

  return first.getFullYear() === last.getFullYear()
    ? `${short(first)} – ${short(last)} ${last.getFullYear()}`
    : `${short(first)} ${first.getFullYear()} – ${short(last)} ${last.getFullYear()}`;
}

export function sortTasks(tasks: Task[]) {
  return [...tasks].sort(
    (a, b) => weekStart(a) - weekStart(b) || Number(a.done) - Number(b.done),
  );
}

export function groupTasksByWeek(tasks: Task[]) {
  const groups = new Map<number, Task[]>();

  sortTasks(tasks).forEach((task) => {
    const key = weekStart(task);
    groups.set(key, [...(groups.get(key) ?? []), task]);
  });

  return [...groups.entries()];
}

export function groupTasksByTag(tasks: Task[]) {
  const groups = new Map<string, { label: string; items: Task[] }>();

  sortTasks(tasks).forEach((task) => {
    const label = task.tag.trim() || "Untagged";
    const key = label.toLocaleLowerCase();
    const existing = groups.get(key);

    if (existing) {
      existing.items.push(task);
    } else {
      groups.set(key, { label, items: [task] });
    }
  });

  return [...groups.values()].sort((a, b) => a.label.localeCompare(b.label));
}
