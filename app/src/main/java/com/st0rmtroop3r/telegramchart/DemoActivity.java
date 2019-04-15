package com.st0rmtroop3r.telegramchart;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.st0rmtroop3r.telegramchart.enitity.ChartData;
import com.st0rmtroop3r.telegramchart.views.ChartView;

import java.util.List;

public class DemoActivity extends Activity {

    private final static String TAG = DemoActivity.class.getSimpleName();

    private List<ChartData> charts;
    private ChartData chart;
    private static final String PREF_THEME_DARK = "PREF_THEME_DARK";
    private boolean dark;
    private ChartView chartView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dark = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(PREF_THEME_DARK, false);
        setTheme(dark ? R.style.AppThemeDark : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        Log.w(TAG, "onCreate: ");

        ChartData chart = DataProvider.readChartData(getApplicationContext(), R.raw.overview3);
        if (chart == null) {
            throw new NullPointerException("chart == null");
        }

        chartView1 = findViewById(R.id.chartV);
        chartView1.setCharData(chart, 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_v2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dark_mode:
                switchTheme();
                return true;
            default: return false;
        }
    }

    private void switchTheme() {
        dark = !dark;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putBoolean(PREF_THEME_DARK, dark)
                .apply();
//        recreate();

        int style = dark ? R.style.AppThemeDark : R.style.AppTheme;
        Resources.Theme theme = getTheme();
        theme.applyStyle(style, true);

        TypedValue typedValue = new TypedValue();
//        theme.resolveAttribute(R.attr.label_background_color, typedValue, true);
//        int test = typedValue.data;
//        Log.w(TAG, "switchTheme: test = " + test);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            int visibility;
//            visibility = dark ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE :
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
////                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//            getWindow().getDecorView().setSystemUiVisibility(visibility);
//
//        }
//        Resources resources = getResources();
//        int color = resources.getColor(dark ? R.color.colorPrimaryDark_dark : R.color.colorPrimaryDark);
//        getWindow().setStatusBarColor(color);
//
//        ActionBar actionBar = getActionBar();
//        if (actionBar != null) {
//            color = resources.getColor(dark ? R.color.colorPrimary_dark : R.color.colorPrimary);
//            actionBar.setBackgroundDrawable(new ColorDrawable(color));
//        }
    }

}
