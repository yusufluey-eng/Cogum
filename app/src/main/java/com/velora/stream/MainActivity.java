package com.velora.stream;

import android.app.*;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;

public class MainActivity extends Activity {
 final int bg=Color.rgb(11,12,22),panel=Color.rgb(24,25,42),purple=Color.rgb(157,105,255),white=Color.WHITE,muted=Color.rgb(170,169,190);
 LinearLayout root,body; String apiKey=""; boolean searching=false;
 String[][] featured={
 {"jNQXAC9IVRw","Me at the zoo","YouTube classics"},
 {"aqz-KE-bpKQ","Big Buck Bunny","Animation"},
 {"M7lc1UVf-VE","YouTube player demo","Technology"},
 {"ScMzIvxBSi4","Creative video showcase","Explore"}
 };
 int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);return t;}
 LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 void pad(View v,int a,int b,int c,int d){v.setPadding(dp(a),dp(b),dp(c),dp(d));}
 void margin(LinearLayout parent,View child,int top){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(top);parent.addView(child,lp);}
 @Override public void onCreate(Bundle state){super.onCreate(state);apiKey=getPreferences(0).getString("api","");home();}
 void shell(String title){
  root=vertical();root.setBackgroundColor(bg);setContentView(root);
  LinearLayout top=horizontal();pad(top,20,20,18,14);
  TextView logo=txt("▶  "+title,25,white,true);top.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
  TextView settings=txt("⚙",25,purple,true);pad(settings,12,0,0,0);top.addView(settings);settings.setOnClickListener(v->settings());
  root.addView(top);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  body=vertical();pad(body,18,4,18,20);scroll.addView(body);
  LinearLayout nav=horizontal();nav.setBackgroundColor(panel);pad(nav,12,12,12,12);
  addNav(nav,"⌂  Home",()->home());addNav(nav,"⌕  Search",()->searchDialog());addNav(nav,"♡  Saved",()->saved());
  root.addView(nav);
 }
 void addNav(LinearLayout bar,String s,Runnable action){TextView t=txt(s,14,white,true);t.setGravity(Gravity.CENTER);bar.addView(t,new LinearLayout.LayoutParams(0,dp(35),1));t.setOnClickListener(v->action.run());}
 void home(){shell("VELORA");
  TextView subtitle=txt("Discover something amazing",16,muted,false);margin(body,subtitle,5);
  TextView title=txt("Your next favorite video ✨",23,white,true);margin(body,title,22);
  LinearLayout chips=horizontal();String[] names={"For You","Gaming","Music","Technology"};for(String s:names){TextView chip=txt(s,12,white,true);chip.setBackground(shape(s.equals("For You")?purple:panel,22));pad(chip,13,10,13,10);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.rightMargin=dp(6);chips.addView(chip,lp);chip.setOnClickListener(v->search(s));}margin(body,chips,15);
  TextView h=txt("Trending picks",19,white,true);margin(body,h,26);
  for(String[] item:featured)card(item[0],item[1],item[2]);
  TextView note=txt("Videos play using YouTube's embedded player. Live search requires your own YouTube Data API key.",12,muted,false);margin(body,note,15);
 }
 void card(String id,String title,String channel){
  LinearLayout card=vertical();card.setBackground(shape(panel,18));card.setClipToOutline(true);
  ImageView image=new ImageView(this);image.setBackgroundColor(Color.rgb(40,37,67));image.setScaleType(ImageView.ScaleType.CENTER_CROP);card.addView(image,new LinearLayout.LayoutParams(-1,dp(195)));
  loadImage(image,"https://i.ytimg.com/vi/"+id+"/hqdefault.jpg");
  LinearLayout row=horizontal();pad(row,14,13,14,15);
  LinearLayout texts=vertical();TextView t=txt(title,16,white,true);texts.addView(t);TextView c=txt(channel+" • YouTube",12,muted,false);margin(texts,c,5);row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
  TextView play=txt("▶",22,purple,true);row.addView(play);card.addView(row);
  margin(body,card,13);card.setOnClickListener(v->player(id,title));card.setOnLongClickListener(v->{save(id,title);return true;});
 }
 void loadImage(ImageView image,String url){new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);Bitmap bmp=BitmapFactory.decodeStream(c.getInputStream());c.disconnect();if(bmp!=null)runOnUiThread(()->image.setImageBitmap(bmp));}catch(Exception ignored){}}).start();}
 void player(String id,String title){
  shell("Now Playing");TextView heading=txt(title,20,white,true);margin(body,heading,10);
  WebView web=new WebView(this);web.setBackgroundColor(Color.BLACK);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.setWebChromeClient(new WebChromeClient());web.setWebViewClient(new WebViewClient());
  String html="<html><head><meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'></head><body style='margin:0;background:#000'><iframe width='100%' height='100%' src='https://www.youtube-nocookie.com/embed/"+id+"?playsinline=1' title='YouTube video player' frameborder='0' allow='accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture; web-share' allowfullscreen></iframe></body></html>";
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(230));lp.topMargin=dp(20);body.addView(web,lp);web.loadDataWithBaseURL("https://www.youtube.com/",html,"text/html","UTF-8",null);
  TextView source=txt("Video source: YouTube • Official embedded player",13,muted,false);margin(body,source,18);
  action("♡ Save video",()->{save(id,title);Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();});
  action("↗ Open on YouTube",()->{Intent i=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.youtube.com/watch?v="+id));startActivity(i);});
 }
 void action(String label,Runnable r){TextView b=txt(label,16,white,true);b.setBackground(shape(panel,15));pad(b,17,15,17,15);margin(body,b,13);b.setOnClickListener(v->r.run());}
 void save(String id,String title){getPreferences(0).edit().putString("saved_"+id,title).apply();getPreferences(0).edit().putString("ids",getPreferences(0).getString("ids","").replace(id+",","")+id+",").apply();}
 void saved(){shell("Saved videos");String[] ids=getPreferences(0).getString("ids","").split(",");for(String id:ids){if(id.length()>0)card(id,getPreferences(0).getString("saved_"+id,"Video"),"Saved");}if(ids.length==1&&ids[0].isEmpty())margin(body,txt("Long-press a video to save it.",16,muted,false),25);}
 void settings(){EditText input=new EditText(this);input.setSingleLine(true);input.setHint("YouTube Data API v3 key");input.setText(apiKey);new AlertDialog.Builder(this).setTitle("YouTube API settings").setMessage("Optional: enter your Google Cloud API key for live YouTube search. This demo stores the key on your device.").setView(input).setPositiveButton("Save",(d,w)->{apiKey=input.getText().toString().trim();getPreferences(0).edit().putString("api",apiKey).apply();}).setNegativeButton("Cancel",null).show();}
 void searchDialog(){EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Search YouTube videos");new AlertDialog.Builder(this).setTitle("Search videos").setView(input).setPositiveButton("Search",(d,w)->search(input.getText().toString())).setNegativeButton("Cancel",null).show();}
 void search(String q){if(q.trim().isEmpty())return;shell("Search");margin(body,txt("Results for: "+q,19,white,true),12);
  if(apiKey.isEmpty()){margin(body,txt("Live YouTube search needs an API key. Add it in settings ⚙.",15,muted,false),25);action("Set API key",this::settings);action("Search on YouTube",()->startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.youtube.com/results?search_query="+android.net.Uri.encode(q)))));return;}
  margin(body,txt("Loading YouTube results…",14,muted,false),20);
  new Thread(()->{try{String u="https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=15&q="+URLEncoder.encode(q,"UTF-8")+"&key="+URLEncoder.encode(apiKey,"UTF-8");HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(12000);int code=c.getResponseCode();InputStream in=code==200?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);c.disconnect();JSONObject json=new JSONObject(out.toString("UTF-8"));if(code!=200)throw new Exception(json.optJSONObject("error")!=null?json.getJSONObject("error").optString("message"):"API error "+code);JSONArray arr=json.getJSONArray("items");runOnUiThread(()->{shell("Search");margin(body,txt("Results for: "+q,19,white,true),10);for(int i=0;i<arr.length();i++){JSONObject obj=arr.optJSONObject(i);if(obj==null)continue;JSONObject sn=obj.optJSONObject("snippet");JSONObject id=obj.optJSONObject("id");if(sn!=null&&id!=null)card(id.optString("videoId"),android.text.Html.fromHtml(sn.optString("title"),0).toString(),sn.optString("channelTitle"));}});}catch(Exception e){runOnUiThread(()->{shell("Search");margin(body,txt("Search unavailable: "+e.getMessage(),15,muted,false),20);});}}).start();
 }
 @Override public void onBackPressed(){home();}
}
