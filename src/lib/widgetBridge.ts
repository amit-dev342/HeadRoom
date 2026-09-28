import type {Task} from "./tasks";
/**
 * Browser/PWA builds intentionally no-op. The Capacitor Android shell can expose
 * a native HeadRoomWidget bridge that stores this JSON in SharedPreferences
 * named HeadRoomWidget and calls notifyAppWidgetViewDataChanged.
 */
export async function syncWidgetTasks(tasks:Task[]){if(typeof window==="undefined")return;const cap=(window as Window & {Capacitor?:{Plugins?:Record<string,{syncTasks?:(input:{tasks:string})=>Promise<void>}>}}).Capacitor;const bridge=cap?.Plugins?.HeadRoomWidget;if(bridge?.syncTasks)await bridge.syncTasks({tasks:JSON.stringify(tasks)});}
