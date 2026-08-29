package com.shiva.originlauncher;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
  GridLayout grid; EditText search; ArrayList<AppInfo> apps = new ArrayList<>(); ArrayList<AppInfo> shown = new ArrayList<>(); Handler h=new Handler(Looper.getMainLooper());
  Runnable clock = new Runnable(){public void run(){updateClock();h.postDelayed(this,1000);}};
  static class AppInfo { String label; Drawable icon; Intent intent; AppInfo(String l, Drawable i, Intent x){label=l;icon=i;intent=x;} }
  @Override public void onCreate(Bundle b){super.onCreate(b); setContentView(com.shiva.originlauncher.R.layout.activity_main);
    grid=findViewById(R.id.grid); search=findViewById(R.id.search); loadApps(); updateClock(); h.post(clock);
    search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){} public void onTextChanged(CharSequence s,int a,int b,int c){filter(s.toString());} public void afterTextChanged(android.text.Editable e){}});
  }
  void updateClock(){TextView t=findViewById(R.id.time), d=findViewById(R.id.date); Date now=new Date(); t.setText(new SimpleDateFormat("HH:mm",Locale.getDefault()).format(now)); d.setText(new SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(now));}
  void loadApps(){PackageManager pm=getPackageManager(); Intent main=new Intent(Intent.ACTION_MAIN,null); main.addCategory(Intent.CATEGORY_LAUNCHER); List<ResolveInfo> list=pm.queryIntentActivities(main,0); Collections.sort(list,(a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString())); for(ResolveInfo r:list){String l=r.loadLabel(pm).toString(); Intent x= new Intent(main); x.setClassName(r.activityInfo.packageName,r.activityInfo.name); apps.add(new AppInfo(l,r.loadIcon(pm),x));} shown.clear(); shown.addAll(apps); render();}
  void filter(String q){shown.clear(); q=q.toLowerCase(Locale.getDefault()); for(AppInfo a:apps) if(a.label.toLowerCase(Locale.getDefault()).contains(q)) shown.add(a); render();}
  TextView tv(String s){TextView v=new TextView(this); v.setText(s); v.setTextColor(Color.rgb(22,22,26)); v.setTextSize(12); v.setGravity(Gravity.CENTER); v.setMaxLines(2); v.setEllipsize(android.text.TextUtils.TruncateAt.END); return v;}
  void render(){grid.removeAllViews(); int dp=(int)(getResources().getDisplayMetrics().density+.5f); for(AppInfo a:shown){LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(4*dp,10*dp,4*dp,6*dp); ImageView iv=new ImageView(this); iv.setImageDrawable(a.icon); box.addView(iv,new LinearLayout.LayoutParams(52*dp,52*dp)); TextView label=tv(a.label); box.addView(label,new LinearLayout.LayoutParams(76*dp,38*dp)); box.setOnClickListener(v->{try{startActivity(a.intent);}catch(Exception e){}}); box.setOnLongClickListener(v->{Toast.makeText(this,a.label,Toast.LENGTH_SHORT).show();return true;}); GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=0; p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); p.setMargins(2*dp,2*dp,2*dp,2*dp); grid.addView(box,p);}}
  @Override protected void onDestroy(){h.removeCallbacks(clock);super.onDestroy();}
}
