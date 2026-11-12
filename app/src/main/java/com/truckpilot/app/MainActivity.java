package com.truckpilot.app;
import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.*;
import org.json.*;
public class MainActivity extends Activity {
 LinearLayout root,content; JSONArray jobs; String role="manager";
 int navy=Color.rgb(15,23,42);
 @Override public void onCreate(Bundle b){super.onCreate(b);try{jobs=new JSONArray(getPreferences(0).getString("jobs","[]"));}catch(Exception e){jobs=new JSONArray();}home();}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(navy);t.setPadding(10,14,10,14);return t;}
 void button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setOnClickListener(v->r.run());content.addView(b);}
 void page(String title){root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(246,248,251));setContentView(root);TextView h=text("🚛 TruckPilot • "+title,22);h.setTextColor(Color.WHITE);h.setBackgroundColor(navy);h.setPadding(20,30,20,30);root.addView(h);ScrollView sc=new ScrollView(this);root.addView(sc);content=new LinearLayout(this);content.setPadding(18,18,18,18);content.setOrientation(1);sc.addView(content);}
 void home(){page("Giriş");content.addView(text("Lojistik operasyonlarını tek yerden yönet",19));button("🏢 Firma yöneticisi",()->{role="manager";dashboard();});button("🚚 Şoför",()->{role="driver";dashboard();});content.addView(text("Demo: Veriler yalnızca bu cihazda saklanır.",13));}
 void dashboard(){page(role.equals("manager")?"Yönetici":"Şoför");content.addView(text("Toplam sefer: "+jobs.length(),20));if(role.equals("manager"))button("+ Yeni sefer",this::newJob);button("Seferleri görüntüle",this::list);button("Rol değiştir",this::home);}
 EditText field(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);content.addView(e);return e;}
 void newJob(){page("Yeni sefer");EditText plate=field("Plaka");EditText driver=field("Şoför");EditText from=field("Yükleme adresi");EditText to=field("Teslimat adresi");button("Kaydet",()->{if(plate.getText().toString().trim().isEmpty()||to.getText().toString().trim().isEmpty()){Toast.makeText(this,"Plaka ve teslimat adresi zorunlu",Toast.LENGTH_SHORT).show();return;}try{JSONObject j=new JSONObject();j.put("plate",plate.getText().toString());j.put("driver",driver.getText().toString());j.put("from",from.getText().toString());j.put("to",to.getText().toString());j.put("status","Atandı");jobs.put(j);save();list();}catch(Exception e){Toast.makeText(this,"Hata",Toast.LENGTH_SHORT).show();}});button("Geri",this::dashboard);}
 void save(){getPreferences(0).edit().putString("jobs",jobs.toString()).apply();}
 void list(){page("Seferler");if(jobs.length()==0)content.addView(text("Henüz sefer yok.",18));for(int i=0;i<jobs.length();i++){int index=i;JSONObject j=jobs.optJSONObject(i);if(j==null)continue;content.addView(text("🚚 "+j.optString("plate")+"\nŞoför: "+j.optString("driver")+"\n"+j.optString("from")+" → "+j.optString("to")+"\nDurum: "+j.optString("status"),17));button("Durumu güncelle",()->update(index));}button("Panele dön",this::dashboard);}
 void update(int i){String[] s={"Atandı","Yüklemeye gidiyor","Yüklendi","Yolda","Teslim edildi"};new AlertDialog.Builder(this).setTitle("Sefer durumu").setItems(s,(d,w)->{try{jobs.getJSONObject(i).put("status",s[w]);save();list();}catch(Exception e){}}).setNegativeButton("İptal",null).show();}
 @Override public void onBackPressed(){dashboard();}
}
