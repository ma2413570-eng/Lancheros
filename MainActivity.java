package com.shiva.originlauncher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private GridLayout grid;
    private EditText search;
    private ArrayList<AppInfo> apps = new ArrayList<>();
    private ArrayList<AppInfo> shown = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView timeView;
    private TextView dateView;
    private TextView weatherView;
    
    private Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 1000);
        }
    };

    static class AppInfo {
        String label;
        Drawable icon;
        Intent intent;

        AppInfo(String label, Drawable icon, Intent intent) {
            this.label = label;
            this.icon = icon;
            this.intent = intent;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        grid = findViewById(R.id.grid);
        search = findViewById(R.id.search);
        timeView = findViewById(R.id.time);
        dateView = findViewById(R.id.date);
        weatherView = findViewById(R.id.weather);

        // Load apps
        loadApps();
        updateClock();
        handler.post(clockRunnable);

        // Setup search listener
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup long press to remove apps
        grid.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    private void updateClock() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        
        timeView.setText(timeFormat.format(now));
        dateView.setText(dateFormat.format(now));
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        
        // Clear previous apps
        apps.clear();
        
        for (ResolveInfo resolveInfo : resolveInfos) {
            String label = resolveInfo.loadLabel(pm).toString();
            Drawable icon = resolveInfo.loadIcon(pm);
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.setClassName(resolveInfo.activityInfo.packageName, 
                                      resolveInfo.activityInfo.name);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            
            apps.add(new AppInfo(label, icon, launchIntent));
        }
        
        // Sort apps alphabetically
        Collections.sort(apps, (a, b) -> a.label.compareToIgnoreCase(b.label));
        
        // Show all apps initially
        shown.addAll(apps);
        render();
    }

    private void filter(String query) {
        shown.clear();
        String lowerQuery = query.toLowerCase(Locale.getDefault());
        
        for (AppInfo app : apps) {
            if (app.label.toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                shown.add(app);
            }
        }
        
        render();
    }

    private void render() {
        grid.removeAllViews();
        
        int dpValue = (int) (getResources().getDisplayMetrics().density + 0.5f);
        int itemSize = 80 * dpValue;
        int padding = 12 * dpValue;
        
        for (final AppInfo app : shown) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.CENTER);
            box.setPadding(padding, padding, padding, padding);
            
            // Create grid layout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = itemSize;
            params.height = itemSize;
            params.setMargins(4 * dpValue, 4 * dpValue, 4 * dpValue, 4 * dpValue);
            box.setLayoutParams(params);
            
            // Set background
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(28 * dpValue);
            background.setColor(Color.WHITE);
            box.setBackground(background);
            
            // Create icon view
            LinearLayout iconContainer = new LinearLayout(this);
            iconContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    56 * dpValue, 56 * dpValue));
            
            // Add icon
            TextView iconView = new TextView(this);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(
                    56 * dpValue, 56 * dpValue));
            iconView.setGravity(Gravity.CENTER);
            
            if (app.icon != null) {
                iconView.setCompoundDrawablesWithIntrinsicBounds(null, app.icon, null, null);
            }
            iconContainer.addView(iconView);
            
            // Add label
            TextView labelView = createLabel(app.label);
            
            box.addView(iconContainer);
            box.addView(labelView);
            
            // Set click listener
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchApp(app);
                }
            });
            
            // Set long press listener for removal
            box.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Optional: add to favorites or remove from view
                    return true;
                }
            });
            
            grid.addView(box);
        }
    }

    private TextView createLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(Color.rgb(22, 22, 26));
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return label;
    }

    private void launchApp(AppInfo app) {
        try {
            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
            }
            
            // Launch app
            startActivity(app.intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(clockRunnable);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from closing the launcher
        moveTaskToBack(true);
    }
}
