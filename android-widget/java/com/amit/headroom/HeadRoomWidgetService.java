package com.amit.headroom;
import android.content.*;import android.widget.*;import org.json.*;import java.text.*;import java.util.*;
public class HeadRoomWidgetService extends RemoteViewsService {
 public RemoteViewsFactory onGetViewFactory(Intent i){return new Factory(getApplicationContext());}
 static class Factory implements RemoteViewsFactory {
  final Context c; JSONArray tasks=new JSONArray();
  Factory(Context c){this.c=c;}
  public void onCreate(){reload();}
  private void reload(){try{tasks=new JSONArray(c.getSharedPreferences("HeadRoomWidget",Context.MODE_PRIVATE).getString("tasks","[]"));}catch(Exception e){tasks=new JSONArray();}}
  public void onDataSetChanged(){reload();}
  public void onDestroy(){}
  public int getCount(){return tasks.length()==0?1:tasks.length();}
  public RemoteViews getViewAt(int p){
   try{
    RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.headroom_widget_task);
    if(tasks.length()==0){v.setTextViewText(R.id.taskStatus,"DAILY NOTE");v.setTextViewText(R.id.taskPriority,"HEADROOM");v.setTextViewText(R.id.taskTitle,dailyQuote());v.setTextViewText(R.id.taskWeek,"Make space. Move forward.");v.setTextViewText(R.id.taskDue,"");v.setTextViewText(R.id.taskTag,"");v.setTextViewText(R.id.taskNotes,"");v.setTextViewText(R.id.taskAction,"ADD A TASK IN HEADROOM");return v;}
    JSONObject t=tasks.getJSONObject(p);boolean done=t.optBoolean("done");String priority=t.optString("priority","medium");
    v.setInt(R.id.taskTitle,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskStatus,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskAction,"setBackgroundResource",R.drawable.widget_action);
    v.setInt(R.id.taskTitle,"setTextColor",android.graphics.Color.WHITE);
    int bg=done?R.drawable.widget_card_done:priority.equals("high")?R.drawable.widget_card_high:priority.equals("low")?R.drawable.widget_card_low:R.drawable.widget_card;
    v.setInt(R.id.taskAction,"setTextColor",android.graphics.Color.WHITE);v.setInt(R.id.taskTitle,"setTextColor",android.graphics.Color.WHITE);
    v.setInt(R.id.taskStatus,"setTextColor",android.graphics.Color.WHITE);
    v.setTextViewText(R.id.taskStatus,done?"COMPLETED":"ACTIVE");v.setTextViewText(R.id.taskPriority,priority.toUpperCase(Locale.ROOT)+" PRIORITY");v.setTextViewText(R.id.taskTitle,t.optString("text"));
    String due=t.optString("due");v.setTextViewText(R.id.taskWeek,weekLabel(due));v.setTextViewText(R.id.taskDue,dueLabel(due));String tag=t.optString("tag").trim();v.setTextViewText(R.id.taskTag,tag.isEmpty()?"UNTAGGED":tag.toUpperCase(Locale.ROOT));String notes=t.optString("notes").trim();v.setTextViewText(R.id.taskNotes,notes.isEmpty()?"No notes":notes);v.setTextViewText(R.id.taskAction,done?"✓  MARK ACTIVE":"○  MARK COMPLETE");
    v.setInt(R.id.taskAction,"setBackgroundResource",R.drawable.widget_action);v.setInt(R.id.taskTitle,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskNotes,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskWeek,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskDue,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskTag,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskPriority,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskStatus,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskAction,"setBackgroundResource",R.drawable.widget_action);
    v.setInt(R.id.taskTitle,"setTextColor",android.graphics.Color.WHITE);
    v.setInt(R.id.taskStatus,"setTextColor",android.graphics.Color.WHITE);
    v.setInt(R.id.taskPriority,"setTextColor",android.graphics.Color.WHITE);
    v.setInt(R.id.taskDue,"setTextColor",android.graphics.Color.WHITE);
    v.setInt(R.id.taskTag,"setTextColor",android.graphics.Color.WHITE);
    v.setInt(R.id.taskAction,"setTextColor",android.graphics.Color.WHITE);
    v.setInt(R.id.taskNotes,"setTextColor",0xFFF8F4E8);v.setInt(R.id.taskWeek,"setTextColor",0xFFF8F4E8);
    v.setInt(R.id.taskAction,"setBackgroundResource",R.drawable.widget_action);
    v.setInt(R.id.taskTitle,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskStatus,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskPriority,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskWeek,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskDue,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskTag,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskNotes,"setBackgroundResource",android.R.color.transparent);
    v.setInt(R.id.taskAction,"setBackgroundResource",R.drawable.widget_action);
    return applyCardBackground(v,bg);
   }catch(Exception e){return null;}
  }
  private RemoteViews applyCardBackground(RemoteViews v,int bg){v.setInt(R.id.taskAction,"setBackgroundResource",R.drawable.widget_action);return v;}
  private String dueLabel(String due){if(due.isEmpty())return "NO DUE DATE";try{Date d=new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(due);return new SimpleDateFormat("EEE, d MMM",Locale.US).format(d);}catch(Exception e){return due;}}
  private String weekLabel(String due){if(due.isEmpty())return "No scheduled week";try{SimpleDateFormat iso=new SimpleDateFormat("yyyy-MM-dd",Locale.US);Date d=iso.parse(due);Calendar cal=Calendar.getInstance();cal.setTime(d);int day=(cal.get(Calendar.DAY_OF_WEEK)+5)%7;cal.add(Calendar.DATE,-day);Date start=cal.getTime();cal.add(Calendar.DATE,6);Date end=cal.getTime();return new SimpleDateFormat("d MMM",Locale.US).format(start)+" – "+new SimpleDateFormat("d MMM yyyy",Locale.US).format(end);}catch(Exception e){return "";}}
  private String dailyQuote(){try{BufferedReader r=new BufferedReader(new java.io.InputStreamReader(c.getResources().openRawResource(R.raw.motivational_quotes)));ArrayList<String> q=new ArrayList<>();String line;while((line=r.readLine())!=null)if(!line.trim().isEmpty())q.add(line);int day=Calendar.getInstance().get(Calendar.DAY_OF_YEAR);return q.get((day-1)%q.size());}catch(Exception e){return "Make room for what matters.";}}
  public RemoteViews getLoadingView(){return null;}public int getViewTypeCount(){return 1;}public long getItemId(int p){return p;}public boolean hasStableIds(){return true;}
 }
}