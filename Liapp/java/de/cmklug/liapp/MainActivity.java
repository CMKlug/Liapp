package de.cmklug.liapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.view.ViewPager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.preference.PreferenceManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.widget.ShareActionProvider;
import android.text.Spannable;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Configuration;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.LineDataProvider;



public class MainActivity extends ActionBarActivity {

    //region ########################## Global ##########################
    private static final int RESULT_SETTINGS = 2;

    // NFC
    private static final int PENDING_INTENT_TECH_DISCOVERED = 1;
    private NfcAdapter mNfcAdapter;

    // Elements
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TextView tvSensorInfo;

    private Date last_scan = null;

    public static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("dd.MM.yyyy;HH:mm") ;

    private SensorData lastsensor = new SensorData();

    private ShareActionProvider myShareActionProvider;

    // Boolean
    private boolean scan = true;
    private boolean wait_read_finished_button = false;
    private boolean show_use_BZM = false;

    // String
    private String tag_data[] = {"",""};
    private String current_sensor_id = "";
    private String Scan_Log = "";

    //endregion ##########################################################

    //region ########################## App Lifetime ##########################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Eula(this,2).show();  //EULA
        new Eula(this,1).show();  //EULA

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        wait_read_finished_button = sharedPrefs.getBoolean("pref_wait_read_finished_button",false);

        if(sharedPrefs.getBoolean("pref_rotation_lock", false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        tvSensorInfo = (TextView)findViewById(R.id.tv_sensorinfo);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {

            Toast.makeText(this, getResources().getString(R.string.error_nfc_device_not_supported), Toast.LENGTH_LONG).show();
            finish();
            return;

        }else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.error_nfc_disabled), Toast.LENGTH_LONG).show();
            resolveIntent(this.getIntent(), false);
        }

        try {
            last_scan = sqlDateFormat.parse("01.01.2000;01:01");
        } catch (ParseException e) {
            e.printStackTrace();
        }


        //getSupportActionBar().setTitle(" " + getResources().getString(R.string.no_sensor));
        tvSensorInfo.setText(getResources().getString(R.string.no_sensor));
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        RelativeLayout rl;
        rl = (RelativeLayout)findViewById(R.id.rl_main);

        if(!sharedPrefs.getBoolean("pref_nightmode", false)) {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));
        }else {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        }

        NfcManager nfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        if (nfcManager != null) {
            mNfcAdapter = nfcManager.getDefaultAdapter();
        }

        if (mNfcAdapter != null) {
            try {
                mNfcAdapter.isEnabled();
            } catch (NullPointerException e) {
                // Drop NullPointerException
            }
            try {
                mNfcAdapter.isEnabled();
            } catch (NullPointerException e) {
                // Drop NullPointerException
            }

            PendingIntent pi = createPendingResult(PENDING_INTENT_TECH_DISCOVERED, new Intent(), 0);
            if (pi != null) {
                try {

                    mNfcAdapter.enableForegroundDispatch(
                            this,
                            pi,
                            new IntentFilter[]{
                                    new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                            },
                            new String[][]{
                                    new String[]{"android.nfc.tech.NfcV"}
                            });
                } catch (NullPointerException e) {
                    // Drop NullPointerException
                }
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            try {
                // Disable foreground dispatch:
                mNfcAdapter.disableForegroundDispatch(this);
            } catch (NullPointerException e) {
                // Drop NullPointerException
            }
        }

        TextView tv_glucoseNow;
        tv_glucoseNow = (TextView) findViewById(R.id.tv_glucoseNow);

        if(tv_glucoseNow != null){ tv_glucoseNow.setTextColor(getResources().getColor(R.color.colorGlucoseLast));}
    }

    @Override
    public void onBackPressed() {

        RelativeLayout rl;
        rl = (RelativeLayout)findViewById(R.id.rl_reading);

        if(rl.getVisibility() == View.VISIBLE){
            rl.setVisibility(View.GONE);
            scan = false;
        }else{
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
    //endregion

    public void finished_reading_onClick(View arg0) {

        RelativeLayout rl;
        rl = (RelativeLayout)findViewById(R.id.rl_reading);
        rl.setVisibility(View.GONE);

        Button bt = (Button)findViewById(R.id.bt_reading);

        if( bt.getText().equals(getResources().getString(R.string.reading_finished))) {
            if (lastsensor.getTimeLeft() <= 0) {
                show_ok_dialog(getResources().getString((R.string.sensor_expired)), "");
            } else if (show_use_BZM) {
                show_ok_dialog(getResources().getString((R.string.critical_value)), getResources().getString(R.string.use_glucose_meter));
            }
        }
    }

    public void current_clucose_onClick(View view) {
        TextView tv = (TextView)view;
        if(copyToClipboard(tv.getText().toString())){
            Toast.makeText(this, getResources().getString(R.string.copytoclipboard), Toast.LENGTH_LONG).show();
        }
    }

    void nightmode(boolean enabled){

        RelativeLayout rl;
        rl = (RelativeLayout)findViewById(R.id.rl_main);

        if(enabled == false  ) {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));

        }else {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        }

    }

    //region ########################## Menu ##########################
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        myShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);

        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        MenuItem item1 = menu.findItem(R.id.action_debug);
        item1.setVisible(sharedPrefs.getBoolean("pref_debug_mode", false));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent i_settings = new Intent(this, SettingsActivity.class);
                startActivityForResult(i_settings, RESULT_SETTINGS);
                break;

            case R.id.action_debug:
                Intent i_debug = new Intent(this, DebugActivity.class);
                i_debug.putExtra("nightmode", sharedPrefs.getBoolean("pref_nightmode", false));
                i_debug.putExtra("rotation_lock", sharedPrefs.getBoolean("pref_rotation_lock", false));
                i_debug.putExtra("tag_data", Scan_Log + "\n" + tag_data[0]);
                this.startActivity(i_debug);
                break;

            case R.id.action_about:
                Intent i_about = new Intent(this, AboutActivity.class);
                i_about.putExtra("nightmode", sharedPrefs.getBoolean("pref_nightmode", false));
                i_about.putExtra("rotation_lock", sharedPrefs.getBoolean("pref_rotation_lock", false));
                this.startActivity(i_about);
                break;

            case R.id.action_close:
                this.finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region ########################## NFC Intent ##########################
    @Override
    protected void onNewIntent(Intent data) {
        resolveIntent(data, true);
    }

    private void resolveIntent(Intent data, boolean foregroundDispatch) {
        this.setIntent(data);
        scan = true;
        String action = data.getAction();
        if ((data.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) { return; }

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)){

            Tag tag = data.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Date now = new Date();

            if (foregroundDispatch && (now.getTime() - last_scan.getTime()) > 10000) {  //10000 = 10sec

                String[] techList = tag.getTechList();
                String searchedTech = NfcV.class.getName();

                // ###################### read Tag ######################
                ProgressBar pb_scan;
                pb_scan = (ProgressBar)findViewById(R.id.pb_reading_spin);
                pb_scan.setVisibility(View.VISIBLE);
                new NfcVReaderTask(this).execute(tag);
                // ######################################################
            }
        }
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PENDING_INTENT_TECH_DISCOVERED:
                // Resolve the foreground dispatch intent:
                resolveIntent(data, true);
                break;

            case RESULT_SETTINGS:
                SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(this);

                nightmode(sharedPrefs.getBoolean("pref_nightmode", false));

                wait_read_finished_button = sharedPrefs.getBoolean("pref_wait_read_finished_button", false);

                if(sharedPrefs.getBoolean("pref_rotation_lock", false)) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }else{
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                }

                SharedPreferences.Editor editor = sharedPrefs.edit();

                if(sharedPrefs.getString("pref_default_range", "0").equals("")){
                    editor.putString("pref_default_range", "0");

                }
                if(sharedPrefs.getString("pref_zb_min", "0").equals("")){
                    editor.putString("pref_zb_min", "0");
                }
                if(sharedPrefs.getString("pref_zb_max", "0").equals("")){
                    editor.putString("pref_zb_max", "0");
                }
                editor.commit();

                updateLogUI();
                updateLastScanChart();
                break;
        }
    }

    public static final double convertmgdlTommoll (int mgdl) {
        double temp;
        temp = Math.round(mgdl * 0.0555 * 10.0);

        return (temp/10);
    }

    private void updateScanUI (List<ScanData> scanneddata, GlucoseData predictedglucosedata, SensorData currentsensor, int error ) throws ParseException {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        getSupportFragmentManager().findFragmentById(R.id.cv_LastScan);

        String unit = sharedPrefs.getString("pref_unit", "mg/dl");

        Date datenow = new Date() ;
        SimpleDateFormat chartdateFormat = new SimpleDateFormat("HH:mm") ;

        LinearLayout ll_chart = (LinearLayout)findViewById(R.id.ll_LastScanchart);
        LinearLayout ll_chart_blindmode = (LinearLayout)findViewById(R.id.ll_LastScanchart_blindmode);

        LineChart cv_LastScan = (LineChart) findViewById(R.id.cv_LastScan);
        LinearLayout ll_LastScan = (LinearLayout) findViewById(R.id.ll_LastScan);

        ImageView iv_prediction_arrow = (ImageView)findViewById(R.id.iv_prediction);
        ImageView iv_unit = (ImageView)findViewById(R.id.iv_unit);

        TextView tv_prediction_arrow = (TextView)findViewById(R.id.tv_prediction);
        TextView tv_unit = (TextView)findViewById(R.id.tv_unit);

        TextView tv_glucoseNow = (TextView) findViewById(R.id.tv_glucoseNow);
        TextView tv_glucoseNow_blindmode = (TextView) findViewById(R.id.tv_glucoseNow_blindmode);

        TextView tv_lastscan = (TextView)findViewById(R.id.tv_last_scan);
        TextView tv_lastscan_blindmode = (TextView)findViewById(R.id.tv_last_scan_blindmode);

        //####################### Chart Data ########################
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        if(error <= 0) {
            long maxx = TimeUnit.MILLISECONDS.toMinutes(sqlDateFormat.parse(predictedglucosedata.getDate()).getTime() - sqlDateFormat.parse(scanneddata.get(0).getDate()).getTime());

            for (int x = 0; x < maxx + 20; x++) {
                String str = chartdateFormat.format(addMinutesToDate(x, sqlDateFormat.parse(scanneddata.get(0).getDate())));
                xVals.add(str);
            }

            //####################### Chart Data ########################

            int c = scanneddata.size();
            ll_LastScan.removeAllViews();

            for (ScanData gd : scanneddata) {
                TextView tv = new TextView(this);

                if (c >= 16 || c == 7 || c == 0) {
                    if (unit.equals("mg/dl")) {
                        tv.setText(gd.getDate().substring(gd.getDate().indexOf(";") + 1) + " " + gd.getGlucoseLevel());
                    } else {
                        tv.setText(gd.getDate().substring(gd.getDate().indexOf(";") + 1) + " " + convertmgdlTommoll(gd.getGlucoseLevel()));
                    }
                    tv.setTextSize(20);
                    tv.setGravity(Gravity.CENTER_HORIZONTAL);
                    ll_LastScan.addView(tv, 0);

                }
                c--;
                if (gd.getType().equals("h")) {

                    if (unit.equals("mg/dl")) {
                        yVals.add(new Entry(gd.getGlucoseLevel(), yVals.size() * 15));
                    } else {
                        yVals.add(new Entry((float) convertmgdlTommoll(gd.getGlucoseLevel()), yVals.size() * 15));
                    }
                }
            }

            if (unit.equals("mg/dl")) {
                yVals.add(new Entry(predictedglucosedata.getGlucoseLevel(), (int) maxx));
            } else {
                yVals.add(new Entry((float) convertmgdlTommoll(predictedglucosedata.getGlucoseLevel()), (int) maxx));
            }
            //####################### Chart Data ########################

            // create a dataset and give it a type (0)
            LineDataSet setCurve = new LineDataSet(yVals, "DataSet");

            setCurve.setLineWidth(4f);
            setCurve.setCircleSize(2f);
            setCurve.setColor(getResources().getColor(R.color.colorValue));
            setCurve.setCircleColor(getResources().getColor(R.color.colorGlucoseNow));
            setCurve.setDrawCircleHole(false);
            setCurve.setDrawCircles(true);
            setCurve.setDrawValues(false);
            setCurve.setCubicIntensity(0.1f);
            setCurve.setDrawCubic(true);
            setCurve.setDrawHighlightIndicators(false);
            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(setCurve); // add the datasets

            // create a data object with the datasets
            LineData ld_data = new LineData(xVals, dataSets);

            cv_LastScan.setDescription("");
            cv_LastScan.setBackgroundColor(ll_chart.getDrawingCacheBackgroundColor());
            cv_LastScan.setData(ld_data);
            cv_LastScan.highlightValue((int) maxx, 0);
            updateLastScanChart();
            cv_LastScan.fitScreen();

            //####################### Chart Data ########################

            if (currentsensor.getLastScanSensorTime() > 60 && currentsensor.getTimeLeft() > 0) {
                iv_prediction_arrow.setVisibility(View.VISIBLE);

                if (predictedglucosedata.getPrediction() == GlucoseData.Prediction.FALLING.ordinal()) {  //50
                    iv_prediction_arrow.setImageResource(R.drawable.arrow_falling);
                    tv_prediction_arrow.setText(getResources().getText(R.string.prediction_falling));
                } else if (predictedglucosedata.getPrediction() == GlucoseData.Prediction.FALLING_SLOW.ordinal()) {   //15
                    iv_prediction_arrow.setImageResource(R.drawable.arrow_falling_slow);
                    tv_prediction_arrow.setText(getResources().getText(R.string.prediction_falling_slow));
                } else if (predictedglucosedata.getPrediction() == GlucoseData.Prediction.RISING.ordinal()) {   //50
                    iv_prediction_arrow.setImageResource(R.drawable.arrow_rising);
                    tv_prediction_arrow.setText(getResources().getText(R.string.prediction_rising));
                } else if (predictedglucosedata.getPrediction() == GlucoseData.Prediction.RISING_SLOW.ordinal()) {   //15
                    iv_prediction_arrow.setImageResource(R.drawable.arrow_rising_slow);
                    tv_prediction_arrow.setText(getResources().getText(R.string.prediction_rising_slow));
                } else {
                    iv_prediction_arrow.setImageResource(R.drawable.arrow_constant);
                    tv_prediction_arrow.setText(getResources().getText(R.string.prediction_constant));
                }

                tv_glucoseNow.setTextColor(getResources().getColor(R.color.colorGlucoseNow));
                tv_glucoseNow.setShadowLayer(3, 1, 1, getResources().getColor(R.color.colorBackgroundLight));

                if (unit.equals("mg/dl") && predictedglucosedata.getGlucoseLevel() <= 500) {
                    if (predictedglucosedata.getGlucoseLevel() <= 40) {
                        tv_glucoseNow.setText("<40!");
                    } else {
                        tv_glucoseNow.setText(String.valueOf(predictedglucosedata.getGlucoseLevel()));
                    }
                } else if (unit.equals("mg/dl") && predictedglucosedata.getGlucoseLevel() > 500) {
                    tv_glucoseNow.setText(">500!");
                } else if (unit.equals("mmol/l") && convertmgdlTommoll(predictedglucosedata.getGlucoseLevel()) <= 28.0) {
                    if (convertmgdlTommoll(predictedglucosedata.getGlucoseLevel()) <= 2.0) {
                        tv_glucoseNow.setText("<2.0!");
                    } else {
                        tv_glucoseNow.setText(String.valueOf(convertmgdlTommoll(predictedglucosedata.getGlucoseLevel())));
                    }
                } else if (unit.equals("mmol/l") && convertmgdlTommoll(predictedglucosedata.getGlucoseLevel()) > 28.0) {
                    tv_glucoseNow.setText(">28.0!");
                }
                tv_glucoseNow_blindmode.setText(tv_glucoseNow.getText());
                ll_LastScan.setVisibility(View.VISIBLE);

            } else {
                cv_LastScan.clearValues();
                ll_LastScan.setVisibility(View.GONE);
                tv_glucoseNow.setText("---");
                iv_prediction_arrow.setVisibility(View.INVISIBLE);

            }

            if (unit.equals("mg/dl")) {
                iv_unit.setImageResource(R.drawable.unit_mgdl);
                tv_unit.setText("Milligramm pro Deziliter");
            } else {
                iv_unit.setImageResource(R.drawable.unit_mmoll);
                tv_unit.setText("Millimol pro Liter");
            }


            // ToDo: Batterie ist leer, bevor die Zeit abgelaufen ist erkennen.
            if (currentsensor.getLastScanSensorTime() == 0) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.sensor_not_activated) );
            } else if (currentsensor.getLastScanSensorTime() == 59) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.sensor_ready_in) + " " + String.valueOf(60 - currentsensor.getLastScanSensorTime()) + " " + getResources().getText(R.string.minute));
            } else if (currentsensor.getLastScanSensorTime() < 60) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.sensor_ready_in) + " " + String.valueOf(60 - currentsensor.getLastScanSensorTime()) + " " + getResources().getText(R.string.minutes));
            } else if (currentsensor.getTimeLeft() <= 0) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.sensor_expired));
            } else if (currentsensor.getTimeLeft() > 2880) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.ends_in) + " " + (int) (Math.ceil(currentsensor.getTimeLeft() / 60 / 24)) + " " + getResources().getText(R.string.days));
            } else if (currentsensor.getTimeLeft() > 1440) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.ends_in) + " " + 2 + " " + getResources().getText(R.string.days));
            } else if (currentsensor.getTimeLeft() == 1440) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.ends_in) + " " + 1 + " " + getResources().getText(R.string.day));
            } else if (currentsensor.getTimeLeft() > 60) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.ends_in) + " " + (int) (Math.ceil(currentsensor.getTimeLeft() / 60)) + " " + getResources().getText(R.string.hours));
            } else if (currentsensor.getTimeLeft() == 60) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.ends_in) + " " + 1 + " " + getResources().getText(R.string.hour));
            } else if (currentsensor.getTimeLeft() > 1) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.ends_in) + " " + currentsensor.getTimeLeft() + " " + getResources().getText(R.string.minutes));
            } else if (currentsensor.getTimeLeft() == 1) {
                tvSensorInfo.setText(" " + getResources().getText(R.string.ends_in) + " " + currentsensor.getTimeLeft() + " " + getResources().getText(R.string.minute));
            } else {
                tvSensorInfo.setText(" " + getResources().getText(R.string.sensor_expired));
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MMM HH:mm");

            tv_lastscan.setText(getResources().getString(R.string.last_scan) + " " + dateFormat.format(datenow));
            tv_lastscan_blindmode.setText(tv_lastscan.getText());

            if (sharedPrefs.getBoolean("pref_blindmode", false)) {
                //Blind mode
                ll_chart.setVisibility(View.GONE);
                ll_chart_blindmode.setVisibility(View.VISIBLE);

            } else {
                //normal mode
                ll_chart.setVisibility(View.VISIBLE);
                ll_chart_blindmode.setVisibility(View.GONE);

            }
        }else {
            //error
            if(cv_LastScan.getData() != null) {
                cv_LastScan.clearValues();
            }
            tv_glucoseNow.setText("---");
        }

        mViewPager.setCurrentItem(0);

    }

    private void updateLogUI () {
        LogFragment lf = LogFragment.getInstance(this);

        lf.refresh();

    }

    private void checkSaveToSql(final List<ScanData> scanneddata, final GlucoseData predictedglucosedata, final SensorData lastsensor) {
        boolean sensorfound = false;

        DBHelper db = DBHelper.getInstance(this);

        for (SensorData sd :  db.getCompleteSensors()) {
            if(sd.getSensorID().equals(lastsensor.getSensorID())) {
                sensorfound = true;
                new SaveTask(this,scanneddata, predictedglucosedata, lastsensor).execute();

            }
        }

        if(sensorfound == false) {
            final Context mcon = this;

            final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getResources().getText(R.string.new_sensor))
                            //.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.your_icon)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new SaveTask(mcon,scanneddata, predictedglucosedata, lastsensor).execute();
                            dialogInterface.dismiss();

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                        }
                    });

            builder.setMessage(getResources().getText(R.string.save_data_from_this_sensor));
            builder.create().show();
        }
    }

    private void updateShareIntent(List<GlucoseData> intentlistGD) {
        ShareFormat sf = new ShareFormat();
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        LineChart cv_LastScan = (LineChart) findViewById(R.id.cv_LastScan);
        String unit = sharedPrefs.getString("pref_unit", "mg/dl");

        Intent myShareIntent = new Intent();
        myShareIntent.setAction(Intent.ACTION_SEND_MULTIPLE); //ACTION_SEND_MULTIPLE

        myShareIntent.setType("*/*"); //"text/plain|image/*"
        ArrayList<Uri> imageUris = new ArrayList<Uri>();

        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String dir = "/liapp";
        String name_csv = "liapp_export";
        String name_chart = "liapp_chart";

        OutputStream stream = null;


        try {
            String string = sf.CSV_sidiary(intentlistGD,unit);

            File f_csv = new File(Environment.getExternalStorageDirectory().getPath() + dir + "/" + name_csv + ".csv");
            File f_chart = new File(Environment.getExternalStorageDirectory().getPath() + dir + "/" + name_chart + ".png");

            if(!f_csv.getParentFile().exists()) { f_csv.getParentFile().mkdirs();}

            stream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + dir + "/" + name_csv + ".csv");

            stream.write(string.getBytes());
            stream.close();

            imageUris.add(Uri.fromFile(f_csv)); // Add your image URIs here

            if(cv_LastScan.saveToPath(name_chart, dir)){
                imageUris.add(Uri.fromFile(f_chart));
                //myShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(baseDir + dir + "/" + name_chart + ".png"));
            }

            //myShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            myShareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            //myShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(baseDir + dir + "/" + name_csv + ".csv"));

            if(myShareActionProvider != null) { myShareActionProvider.setShareIntent(myShareIntent);}

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class SaveTask extends AsyncTask<String, Void, String> {
        private Context mCon;
        private List<ScanData> mScanneddata;
        private GlucoseData mPredictedglucosedata;
        private SensorData mLastsensor ;

        public SaveTask(Context con, List<ScanData> scanneddata, GlucoseData predictedglucosedata, SensorData lastsensor)
        {
            mCon = con;
            mScanneddata = scanneddata;
            mPredictedglucosedata = predictedglucosedata;
            mLastsensor = lastsensor;
        }

        @Override
        protected String doInBackground(String... params) {

            DBHelper db = DBHelper.getInstance(mCon);

            final List<GlucoseData> intentlistGD = new ArrayList<>();

            db.addorUpdateSensorData(mLastsensor);

            db.addScanData(mPredictedglucosedata);

            for (ScanData sd : mScanneddata) {

                if (sd.getType().equals("h")) {
                    GlucoseData gd = new GlucoseData();

                    gd.setDate(sd.getDate());
                    gd.setGlucoseLevel(sd.getGlucoseLevel());

                    db.addorUpdateHistoryGlucoseData(gd);
                    intentlistGD.add(gd);
                }
            }
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    updateLogUI();
                    updateShareIntent(intentlistGD);


                }
            });

            return null;
        }
    }


    //region ########################## convert to Hex ##########################
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteToHex(byte byte_) {
        char[] hexChars = new char[2];

        int v = byte_ & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];

        return new String(hexChars);
    }
    //endregion

    private class NfcVReaderTask extends AsyncTask<Tag, Void, String> {

        private Context mCon;
        private Date mLastScan;
        private int error = 0;
        private List<ScanData> scanneddata = new ArrayList<>();
        private GlucoseData predictedglucosedata = new GlucoseData();
        private SensorData detectedsensor = new SensorData();

        // Byte
        private byte[] tag_data_raw = new byte[360];

        public NfcVReaderTask(Context con)
        {
            mCon = con;
        }

        @Override
        protected void onPostExecute(String result) {
            long[] pattern = {0, 200, 100, 200};
            Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

            if (error > 0) {
                vibrator.vibrate(200);
            } else{
                vibrator.vibrate(pattern,-1);

                last_scan = mLastScan;
                lastsensor = detectedsensor;

                if(detectedsensor.getTimeLeft() > 0) {
                    checkSaveToSql(scanneddata, predictedglucosedata, detectedsensor);
                }
                log();

            }

            try {
                updateScanUI(scanneddata, predictedglucosedata, detectedsensor, error);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(!wait_read_finished_button){
                RelativeLayout rl;
                rl = (RelativeLayout)findViewById(R.id.rl_reading);
                rl.setVisibility(View.GONE);

                if (error <= 0) {

                    if (detectedsensor.getTimeLeft() <= 0) {
                        show_ok_dialog(getResources().getString((R.string.sensor_expired)), "");
                    } else if (show_use_BZM) {
                        show_ok_dialog(getResources().getString((R.string.critical_value)), getResources().getString(R.string.use_glucose_meter));
                    }
                }
            }else{
                ProgressBar pb;
                pb = (ProgressBar)findViewById(R.id.pb_reading_spin);
                pb.setVisibility(View.INVISIBLE);

                Button bt;
                bt = (Button)findViewById(R.id.bt_reading);
                bt.setVisibility(View.VISIBLE);

                if(error == 0){
                    bt.setText(getResources().getString(R.string.reading_finished));
                }else if(error == 1){
                    bt.setText(getResources().getString(R.string.error_read_timeout));
                }else if(error == 2){
                    bt.setText(getResources().getString(R.string.error_reading_tag));
                }else{
                    bt.setText("Error!");
                }
            }

        }

        private void log (){
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(mCon);

            String unit = sharedPrefs.getString("pref_unit", "mg/dl");

            String temp_log = "";

            temp_log = sqlDateFormat.format(mLastScan).replaceAll(";", " ") + "\n";
            temp_log = temp_log + "aktueller Wert: " + predictedglucosedata.getGlucoseLevel()  +  "\n";

           

            temp_log = temp_log + "\n" + "Trent:" + "\n";

            String tempstr = new String();

            for (ScanData gd : scanneddata) {

                if(gd.getType().equals("t")) {
                    if (unit.equals("mmol/l")) {
                        tempstr = gd.getSensorTime() + " | " + gd.getFirsthb() + " | " + convertmgdlTommoll(gd.getGlucoseLevel()) + " | " + gd.getThirdb() + " | " + gd.getFourthb() + " | " + gd.getFifthb() + " | " + gd.getError() + "\n" + tempstr;
                    } else {
                        tempstr = gd.getSensorTime() + " | " + gd.getFirsthb() + " | " + gd.getGlucoseLevel() + " | " + gd.getThirdb() + " | " + gd.getFourthb() + " | " + gd.getFifthb() + " | " + gd.getError() + "\n" + tempstr;
                    }
                }
            }
            temp_log += tempstr.replaceAll("\\.",",");

            temp_log = temp_log + "\n" + "History:" + "\n";

            tempstr = new String();
            for (ScanData gd : scanneddata) {
                if(gd.getType().equals("h")) {

                    if (unit.equals("mmol/l")) {
                        tempstr =  gd.getSensorTime() + " | " + gd.getFirsthb() + " | " + convertmgdlTommoll(gd.getGlucoseLevel()) + " | " + gd.getThirdb() + " | " + gd.getFourthb() + " | " + gd.getFifthb() + " | " + gd.getError() + "\n" + tempstr;
                    } else {
                        tempstr = gd.getSensorTime() + " | " + gd.getFirsthb() + " | " + gd.getGlucoseLevel() + " | " + gd.getThirdb() + " | " + gd.getFourthb() + " | " + gd.getFifthb() + " | " + gd.getError() + "\n" + tempstr;
                    }
                }
            }
            temp_log += tempstr.replaceAll("\\.",",");

            Scan_Log = temp_log; // + "\n\n" + Scan_Log;
        }

        private void tag_raw_to_value (int index_trent, int index_history, int sensortime){

				/* removed the magic */
				
                show_use_BZM = true;


        }

        private void show_reading_layout() {

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    RelativeLayout rl;
                    rl = (RelativeLayout) findViewById(R.id.rl_reading);
                    rl.setVisibility(View.VISIBLE);

                    ProgressBar pb;
                    pb = (ProgressBar) findViewById(R.id.pb_reading_spin);
                    pb.setVisibility(View.VISIBLE);

                    ProgressBar pb_reading;
                    pb_reading = (ProgressBar) findViewById(R.id.pb_reading);
                    pb_reading.setProgress(0);

                    Button bt;
                    bt = (Button) findViewById(R.id.bt_reading);
                    bt.setVisibility(View.GONE);
                }
            });
        }

        @Override
        protected String doInBackground(Tag... params) {
            Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(200);

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    TextView tv_glucoseNow;
                    tv_glucoseNow = (TextView) findViewById(R.id.tv_glucoseNow);

                    tv_glucoseNow.setTextColor(getResources().getColor(R.color.colorGlucoseLast));
                }
            });

            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this);

            show_reading_layout();
            Long timestamp_beginread = System.currentTimeMillis();

            Tag tag = params[0];

            NfcV nfcvTag = NfcV.get(tag);

            boolean faster_nfc_scan = sharedPrefs.getBoolean("pref_nfc_fastscan", false);

            show_use_BZM = false;

            try {
                nfcvTag.connect();

                tag_data[0] = "";

                final byte[] uid = tag.getId();
                current_sensor_id = new String(uid);

                for(int i=0; i <= 40; i++) {

                    byte[] cmd;
                    int offset = 1;

                    if(faster_nfc_scan) {

                        cmd = new byte[]{
                                (byte) 0x00, // Flags
                                (byte) 0x20, // Command: Read
                                (byte) (i & 0x0ff) // block (offset)
                        };

                        offset = 1;

                    }else {

                        cmd = new byte[]{
                                (byte)0x60,                  // flags: addressed (= UID field present)
                                (byte)0x20,
                                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  // placeholder for tag UID
                                (byte)(i & 0x0ff),
                                (byte)((1 - 1) & 0x0ff)
                        };

                        offset = 2;

                        System.arraycopy(uid, 0, cmd, 2, 8);
                    }

                    byte[] oneBlock = new byte[9];

                    while(true) {
                        try {
                            oneBlock = nfcvTag.transceive(cmd);
                            break;
                        } catch (IOException e) {
                            if((System.currentTimeMillis() > (timestamp_beginread + 5000)) || !scan){
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        if(!scan){
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_read_cancelled), Toast.LENGTH_SHORT).show();
                                            tag_data[0] = tag_data[0] + getResources().getString(R.string.error_read_cancelled);
                                        }else {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_read_timeout), Toast.LENGTH_SHORT).show();
                                            tag_data[0] = tag_data[0] + getResources().getString(R.string.error_read_timeout);
                                        }
                                    }
                                });
                                tag_data[0] = tag_data[0] + "\n" + e.toString() + "\n";
                                error = 1;
                                return null;
                            }
                        }
                    }

                    oneBlock = Arrays.copyOfRange(oneBlock, offset, oneBlock.length);


                    tag_data_raw[i*8+0] = oneBlock[0];
                    tag_data_raw[i*8+1] = oneBlock[1];
                    tag_data_raw[i*8+2] = oneBlock[2];
                    tag_data_raw[i*8+3] = oneBlock[3];
                    tag_data_raw[i*8+4] = oneBlock[4];
                    tag_data_raw[i*8+5] = oneBlock[5];
                    tag_data_raw[i*8+6] = oneBlock[6];
                    tag_data_raw[i*8+7] = oneBlock[7];

                  

                    final int pg = i;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {

                            ProgressBar pb_reading;
                            pb_reading = (ProgressBar)findViewById(R.id.pb_reading);
                            pb_reading.setMax(40);
                            pb_reading.setProgress(pg);

                        }
                    });
                }

            } catch (Exception e) {
                error = 2;
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_reading_tag), Toast.LENGTH_SHORT).show();
                        tag_data[0] = tag_data[0] + getResources().getString(R.string.error_reading_tag);
                    }
                });
                tag_data[0] = tag_data[0] + "\n" + e.toString() + "\n";
                return null;
            } finally {
                try {
                    nfcvTag.close();
                } catch (Exception e) {

                }
            } // Lesen beendet

            mLastScan = new Date() ;

            tag_data[0] = tag_data[0] + "Fast NFC-Scan:" + sharedPrefs.getBoolean("pref_nfc_fastscan", false);

            String temp;

            temp = byteToHex(tag_data_raw[26]);
            int index_trent = Integer.parseInt(temp,16); // aktuelle Daten an pos index_trent

            temp = byteToHex(tag_data_raw[27]);
            int index_history = Integer.parseInt(temp,16);

            temp = bytesToHex(new byte[]{tag_data_raw[317], tag_data_raw[316]});
            final int sensor_time = Integer.parseInt(temp, 16);

            final int tag_lifetime_min_left = 14 * 24 * 60 - sensor_time;


            detectedsensor.setExpireDate(sqlDateFormat.format(addMinutesToDate(tag_lifetime_min_left, mLastScan)));
            detectedsensor.setStartDate(sqlDateFormat.format(addMinutesToDate(-sensor_time, mLastScan)));
            detectedsensor.setTimeLeft(tag_lifetime_min_left);
            detectedsensor.setSensorID(current_sensor_id);
            detectedsensor.setLastScanSensorTime(sensor_time);

            DBHelper db = DBHelper.getInstance(mCon);
            for (SensorData sd : db.getCompleteSensors()) {
                if(sd.getSensorID().equals(current_sensor_id)) {
                    detectedsensor.setExpireDate(sd.getExpireDate());
                    detectedsensor.setStartDate(sd.getStartDate());
                }
            }

            tag_raw_to_value(index_trent, index_history, sensor_time);

            return null;
        }
    }

    private boolean copyToClipboard(String text){

            try {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) this
                            .getSystemService(this.CLIPBOARD_SERVICE);
                    clipboard.setText(text);
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this
                            .getSystemService(this.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData
                            .newPlainText(
                                    "BZ", text);
                    clipboard.setPrimaryClip(clip);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
    }

    public void show_ok_dialog(String title, String msg ) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                //.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.your_icon)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Mark this version as read.
                        dialogInterface.dismiss();

                    }
                });

        if(msg.length() >=1) {builder.setMessage(msg);}
        builder.create().show();

    }


    public void exportTO(FileOutputStream fileOut){
        String datastring = null;
        OutputStreamWriter writer = new OutputStreamWriter(fileOut);
        ShareFormat format = new ShareFormat();
        DBHelper db = DBHelper.getInstance(this);
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        String unit = sharedPrefs.getString("pref_unit", "mg/dl");

        try {
            datastring = format.CSV_sidiary(db.getCompleteHistory(),unit);
            writer.write(datastring);		//in datei Schreiben

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Saving data!", Toast.LENGTH_LONG).show();
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void updateLastScanChart() {

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());

        ImageView iv_unit;
        iv_unit = (ImageView)findViewById(R.id.iv_unit);


        if(sharedPrefs.getString("pref_unit", "mg/dl").equals("mg/dl")) {
            iv_unit.setImageResource(R.drawable.unit_mgdl);
        }else{
            iv_unit.setImageResource(R.drawable.unit_mmoll);
        }

        LineChart cv_LastScan = (LineChart) findViewById(R.id.cv_LastScan);

        if(sharedPrefs.getBoolean("pref_nightmode", false)) {
            cv_LastScan.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        }else{
            cv_LastScan.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));
        }

        LineData getData = cv_LastScan.getData();
        if(getData != null) {
            YAxis yAxisLeft = cv_LastScan.getAxisLeft();

            yAxisLeft.removeAllLimitLines();
            yAxisLeft.setDrawLimitLinesBehindData(true);
            yAxisLeft.resetAxisMaxValue();

            if (cv_LastScan.getData() != null && !sharedPrefs.getString("pref_default_range", "0.0").equals("")) {
                if (cv_LastScan.getData().getDataSets().get(0).getYMax() < Float.valueOf(sharedPrefs.getString("pref_default_range", "0.0"))) { //Todo: platz für highlight
                    yAxisLeft.setAxisMaxValue(Float.valueOf(sharedPrefs.getString("pref_default_range", "0.0")));
                }
            }

            LimitLine ll_max = new LimitLine(Float.valueOf(sharedPrefs.getString("pref_zb_max", "-100.0")), getResources().getString(R.string.pref_zb_max));
            ll_max.setLineWidth(4f);
            ll_max.setTextSize(12f);

            LimitLine ll_min = new LimitLine(Float.valueOf(sharedPrefs.getString("pref_zb_min", "-100.0")), getResources().getString(R.string.pref_zb_min));
            ll_min.setLineWidth(4f);
            ll_min.setTextSize(12f);

            ll_min.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);

            Legend legend = cv_LastScan.getLegend();
            legend.setEnabled(false);

            // set an alternative background color
            if (sharedPrefs.getBoolean("pref_nightmode", false)) {
                ll_max.setLineColor(getResources().getColor(R.color.colorZielbereichDark));
                ll_max.setTextColor(getResources().getColor(R.color.colorZielbereichDark));

                ll_min.setLineColor(getResources().getColor(R.color.colorZielbereichDark));
                ll_min.setTextColor(getResources().getColor(R.color.colorZielbereichDark));
            } else {
                ll_max.setLineColor(getResources().getColor(R.color.colorZielbereichLight));
                ll_max.setTextColor(getResources().getColor(R.color.colorZielbereichLight));

                ll_min.setLineColor(getResources().getColor(R.color.colorZielbereichLight));
                ll_min.setTextColor(getResources().getColor(R.color.colorZielbereichLight));
            }

            yAxisLeft.addLimitLine(ll_max);
            yAxisLeft.addLimitLine(ll_min);

            cv_LastScan.notifyDataSetChanged();
            cv_LastScan.invalidate();
        }

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
