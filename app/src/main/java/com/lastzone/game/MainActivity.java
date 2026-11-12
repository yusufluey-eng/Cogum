package com.lastzone.game;
import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.graphics.*;
import java.util.*;
public class MainActivity extends Activity {
 Game game;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);game=new Game();setContentView(game);}
 @Override public void onBackPressed(){if(game!=null)game.mode=0;}
 class Enemy {float x,y;int hp=100;Enemy(float a,float b){x=a;y=b;}}
 class Game extends View {
  Paint p=new Paint(3); Random rand=new Random(23); ArrayList<Enemy> enemies=new ArrayList<>();
  final String[] map={"1111111111111111","1000000000000001","1000010000000001","1000010001100001","1000000000100001","1000000000000001","1001100000000001","1000000000010001","1000000000010001","1000000000000001","1000011000000001","1000000000000001","1000000000000001","1111111111111111"};
  float x=3.3f,y=4.5f,ang=0.3f,pitch=0f,joyX,joyY,joyDX,joyDY,lookLastX,lookLastY;int joyId=-1,lookId=-1,mode=0,hp=100,ammo=30,reserve=120,kills=0,ticks=0;long last=System.nanoTime(),lastShot=0;boolean firing=false;float screenW,screenH;int mission=0;String toast="";long toastUntil=0;
  Game(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);p.setStrokeWidth(1);}
  void rect(Canvas c,float a,float b,float d,float e,int col){color(col);c.drawRect(a,b,d,e,p);}
  void txt(Canvas c,String s,float a,float b,int size,int col){color(col);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(size);c.drawText(s,a,b,p);}
  void circle(Canvas c,float a,float b,float r,int col){color(col);c.drawCircle(a,b,r,p);}
  boolean wall(float a,float b){int ix=(int)a,iy=(int)b;return ix<0||iy<0||iy>=map.length||ix>=map[0].length()||map[iy].charAt(ix)=='1';}
  void start(){mode=1;x=3.3f;y=4.5f;ang=.3f;pitch=0;hp=100;ammo=30;reserve=120;kills=0;ticks=0;enemies.clear();enemies.add(new Enemy(8,5));enemies.add(new Enemy(10,8));enemies.add(new Enemy(4,10));enemies.add(new Enemy(12,3));enemies.add(new Enemy(7,11));last=System.nanoTime();note("GOREV 01: BLACK SIGNAL - dusmanlari temizle");}
  void note(String s){toast=s;toastUntil=System.currentTimeMillis()+3200;}
  void shoot(){long now=System.currentTimeMillis();if(now-lastShot<220||ammo<=0||mode!=1)return;lastShot=now;ammo--;Enemy hit=null;double best=99;for(Enemy e:enemies){if(e.hp<=0)continue;double dist=Math.hypot(e.x-x,e.y-y);double a=Math.atan2(e.y-y,e.x-x)-ang;while(a>Math.PI)a-=2*Math.PI;while(a< -Math.PI)a+=2*Math.PI;if(Math.abs(a)<.12+0.12/dist&&dist<best){boolean blocked=false;for(double t=.15;t<dist;t+=.1)if(wall(x+(float)Math.cos(ang+a)* (float)t,y+(float)Math.sin(ang+a)*(float)t)){blocked=true;break;}if(!blocked){hit=e;best=dist;}}}if(hit!=null){hit.hp-=55;if(hit.hp<=0){kills++;note("HEDEF ETKISIZ: "+kills+"/5");if(kills==5){mode=2;mission=1;}}}}
  void reload(){int n=Math.min(30-ammo,reserve);ammo+=n;reserve-=n;note("SARJOR DEGISTIRILDI");}
  void update(){long now=System.nanoTime();float dt=Math.min(.045f,(now-last)/1e9f);last=now;if(mode!=1)return;ticks++;float move=(float)Math.hypot(joyDX,joyDY);if(move>.12f){float fwd=-joyDY,side=joyDX;float dx=(float)(Math.cos(ang)*fwd-Math.sin(ang)*side)*dt*2.7f,dy=(float)(Math.sin(ang)*fwd+Math.cos(ang)*side)*dt*2.7f;if(!wall(x+dx,y))x+=dx;if(!wall(x,y+dy))y+=dy;}if(firing)shoot();if(ticks%45==0){for(Enemy e:enemies){if(e.hp<=0)continue;double d=Math.hypot(e.x-x,e.y-y);if(d<6){if(d>1.2){float dx=(x-e.x)/(float)d*.075f,dy=(y-e.y)/(float)d*.075f;if(!wall(e.x+dx,e.y))e.x+=dx;if(!wall(e.x,e.y+dy))e.y+=dy;}else hp=Math.max(0,hp-9);}}if(hp==0){mode=3;note("GOREV BASARISIZ");}}}
  void scene(Canvas c,float w,float h){float horizon=h*(.52f+pitch);rect(c,0,0,w,horizon,Color.rgb(82,116,141));rect(c,0,horizon,w,h,Color.rgb(69,74,60));for(int i=0;i<32;i++){float a=ang+(i/31f-.5f)*1.23f;float dx=(float)Math.cos(a),dy=(float)Math.sin(a),d=.03f;while(d<18&&!wall(x+dx*d,y+dy*d))d+=.045f;float corrected=d*(float)Math.cos(a-ang);float wallH=Math.min(h*1.6f,h/(corrected*.76f));float sx=i*w/32f,ex=(i+1)*w/32f+1;int shade=Math.max(48,Math.min(160,(int)(165-corrected*12)));rect(c,sx,horizon-wallH/2,ex,horizon+wallH/2,Color.rgb(shade,shade+4,shade+1));rect(c,sx,horizon-wallH/2,ex,horizon-wallH/2+3,Color.rgb(shade+12,shade+12,shade+12));}
   ArrayList<Enemy> sorted=new ArrayList<>(enemies);Collections.sort(sorted,(a,b)->Double.compare(Math.hypot(b.x-x,b.y-y),Math.hypot(a.x-x,a.y-y)));
   for(Enemy e:sorted){if(e.hp<=0)continue;double d=Math.hypot(e.x-x,e.y-y),a=Math.atan2(e.y-y,e.x-x)-ang;while(a>Math.PI)a-=2*Math.PI;while(a< -Math.PI)a+=2*Math.PI;if(Math.abs(a)>0.68||d<.3)continue;float sx=(float)(w*.5+Math.tan(a)*w*.75),sz=(float)Math.min(h*.72,h/(d*.85));float feet=horizon+sz*.45f;float head=feet-sz;float visible=0;float tx=x+(e.x-x)*.9f,ty=y+(e.y-y)*.9f;if(!wall(tx,ty))visible=1;if(visible==0)continue;rect(c,sx-sz*.18f,head+sz*.32f,sx+sz*.18f,feet,Color.rgb(42,54,44));circle(c,sx,head+sz*.17f,sz*.13f,Color.rgb(175,153,127));rect(c,sx-sz*.24f,head+sz*.39f,sx+sz*.24f,head+sz*.48f,Color.rgb(36,44,35));rect(c,sx-sz*.23f,head-sz*.1f,sx+sz*.23f,head-sz*.04f,Color.RED);}
   // weapon silhouette
   rect(c,w*.46f,h*.74f,w*.59f,h,Color.rgb(32,35,35));rect(c,w*.505f,h*.63f,w*.53f,h*.9f,Color.rgb(23,25,27));rect(c,w*.497f,h*.65f,w*.54f,h*.68f,Color.rgb(58,61,58));
   color(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.drawCircle(w/2,horizon,8,p);c.drawLine(w/2-20,horizon,w/2-9,horizon,p);c.drawLine(w/2+9,horizon,w/2+20,horizon,p);p.setStyle(Paint.Style.FILL);
  }
  @Override protected void onDraw(Canvas c){super.onDraw(c);screenW=getWidth();screenH=getHeight();float w=screenW,h=screenH;update();if(mode==0){rect(c,0,0,w,h,Color.rgb(10,18,27));txt(c,"LAST ZONE",w*.10f,h*.31f,(int)(h*.12f),Color.WHITE);txt(c,"BLACK SIGNAL",w*.11f,h*.42f,(int)(h*.07f),Color.rgb(226,174,71));txt(c,"GOREV 01  /  KARA INIS",w*.11f,h*.55f,23,Color.LTGRAY);rect(c,w*.11f,h*.64f,w*.53f,h*.82f,Color.rgb(208,151,43));txt(c,"HIZLI MAC  >  BASLA",w*.14f,h*.76f,25,Color.BLACK);txt(c,"ONLINE MOD: GELISTIRILIYOR",w*.6f,h*.75f,18,Color.LTGRAY);}
  else {scene(c,w,h);rect(c,0,0,w,h*.14f,0xCC0B1723);rect(c,0,h*.14f,w,h*.145f,0xFFBA8B42);
   txt(c,"LAST ZONE",22,34,24,Color.WHITE);txt(c,"BLACK SIGNAL  /  KARA INIS",23,53,12,0xFFC3D4DD);
   txt(c,"CAN "+hp+"   |   HEDEF "+kills+"/5",w*.43f,35,20,Color.WHITE);
   rect(c,w*.43f,h*.102f,w*.69f,h*.117f,0xFF334A50);rect(c,w*.43f,h*.102f,w*(.43f+.26f*hp/100f),h*.117f,0xFF56D1A0);
   txt(c,ammo+" / "+reserve,w*.85f,35,23,Color.rgb(247,196,67));circle(c,w*.13f,h*.77f,h*.17f,0x55444444);circle(c,w*.13f+joyDX*h*.12f,h*.77f+joyDY*h*.12f,h*.073f,0xAAFFFFFF);circle(c,w*.88f,h*.72f,h*.125f,0xBB9D3C27);circle(c,w*.88f,h*.72f,h*.102f,0xBBCC5834);
   // Bullet icon: brass cartridge with rounded tip
   float bx=w*.88f,by=h*.72f,unit=h*.055f;
   color(0xFFFFD57A);c.drawRoundRect(bx-unit*.28f,by-unit*.80f,bx+unit*.28f,by+unit*.63f,unit*.10f,unit*.10f,p);
   color(0xFFFFE4A1);c.drawOval(bx-unit*.28f,by-unit*1.08f,bx+unit*.28f,by-unit*.38f,p);
   rect(c,bx-unit*.35f,by+unit*.50f,bx+unit*.35f,by+unit*.75f,0xFF9A692F);
   circle(c,w*.77f,h*.86f,h*.071f,0xB9455B69);
   // Magazine reload icon: curved magazine and two motion arrows
   float mx=w*.77f,my=h*.86f,mu=h*.045f;
   color(0xFFE7F1F6);Path mag=new Path();mag.moveTo(mx-mu*.45f,my-mu*.66f);mag.lineTo(mx+mu*.40f,my-mu*.66f);mag.lineTo(mx+mu*.32f,my+mu*.60f);mag.quadTo(mx-mu*.20f,my+mu*.95f,mx-mu*.60f,my+mu*.53f);mag.close();c.drawPath(mag,p);
   rect(c,mx-mu*.57f,my-mu*.81f,mx+mu*.51f,my-mu*.57f,0xFFADC7D3);
   color(0xFF233946);p.setStrokeWidth(2);c.drawLine(mx-mu*.2f,my-mu*.28f,mx+mu*.18f,my-mu*.28f,p);c.drawLine(mx-mu*.2f,my,mx+mu*.18f,my,p);
   txt(c,"SOL: HAREKET     SAG: BAKIS",w*.31f,h*.94f,13,0xFFD5E4EC);if(System.currentTimeMillis()<toastUntil)txt(c,toast,w*.25f,h*.19f,18,Color.rgb(255,217,110));if(mode==2||mode==3){rect(c,w*.19f,h*.22f,w*.82f,h*.7f,0xE6101B26);txt(c,mode==2?"GOREV TAMAMLANDI":"GOREV BASARISIZ",w*.25f,h*.4f,34,Color.WHITE);txt(c,"YENIDEN OYNAMAK ICIN DOKUN",w*.25f,h*.56f,23,Color.rgb(245,184,67));}}
   postInvalidateDelayed(16);
  }
  @Override public boolean onTouchEvent(android.view.MotionEvent ev){int act=ev.getActionMasked(),id=ev.getActionIndex(),pid=ev.getPointerId(id);float w=getWidth(),h=getHeight();if(act==MotionEvent.ACTION_DOWN||act==MotionEvent.ACTION_POINTER_DOWN){float px=ev.getX(id),py=ev.getY(id);if(mode==0||mode==2||mode==3){start();return true;}if(px>w*.72f&&py>h*.59f&&px<w*.82f&&py>h*.76f){reload();return true;}if(px>w*.78f&&py>h*.56f){firing=true;shoot();return true;}if(px<w*.36f&&py>h*.43f&&joyId==-1){joyId=pid;joyX=px;joyY=py;joyDX=joyDY=0;}else if(lookId==-1){lookId=pid;lookLastX=px;lookLastY=py;}}
   else if(act==MotionEvent.ACTION_MOVE){for(int i=0;i<ev.getPointerCount();i++){int z=ev.getPointerId(i);float px=ev.getX(i),py=ev.getY(i);if(z==joyId){joyDX=Math.max(-1,Math.min(1,(px-joyX)/(h*.13f)));joyDY=Math.max(-1,Math.min(1,(py-joyY)/(h*.13f)));}else if(z==lookId){ang+=(px-lookLastX)/w*2.7f;pitch=Math.max(-.30f,Math.min(.30f,pitch+(py-lookLastY)/h*.65f));lookLastX=px;lookLastY=py;}}}
   else if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_POINTER_UP||act==MotionEvent.ACTION_CANCEL){if(pid==joyId){joyId=-1;joyDX=joyDY=0;}if(pid==lookId)lookId=-1;if(ev.getPointerCount()<=2)firing=false;}return true;
  }
 }
}