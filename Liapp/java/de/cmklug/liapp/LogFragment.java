package de.cmklug.liapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class LogFragment extends Fragment {
    private static LogFragment sInstance;
    private static  View view;

    private int logEntries;

    private String title;
    private int page;

    public static synchronized LogFragment getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new LogFragment();
        }
        return sInstance;
    }

    public static LogFragment newInstance(int page, String title) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }


    public LogFragment() {
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_log, container, false);

        refresh();

        return (view);
    }

    public void refresh()  {

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(view.getContext().getApplicationContext());

        final String unit = sharedPrefs.getString("pref_unit", "mg/dl");

        TextView tv_unit;
        tv_unit = (TextView)view.findViewById(R.id.logView_tv_unit);
        tv_unit.setText(unit);

        LinearLayout  my_linear_layout = (LinearLayout) view.findViewById(R.id.logView_ll_log);

        my_linear_layout.removeAllViews();
        logEntries = 0;

        ImageButton bt = new ImageButton(view.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(133, 83);

        bt.setBackgroundResource(R.drawable.button_arrowdown);

        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(0,20,0,50);

        bt.setLayoutParams(params);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper db = DBHelper.getInstance(view.getContext());

                try {
                    addEntry(unit);
                    if(db.getScanCount() - logEntries <= 0){v.setVisibility(View.GONE);}
                } catch (ParseException e) {
                    e.printStackTrace();
                }            }
        });

        my_linear_layout.addView(bt);

        try {
            addEntry(unit);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addEntry(String unit)throws ParseException{
        SimpleDateFormat logDateFormat = new SimpleDateFormat("dd MMM") ;
        SimpleDateFormat logTimeFormat = new SimpleDateFormat("HH:mm") ;

        DBHelper db = DBHelper.getInstance(view.getContext());

        LinearLayout  my_linear_layout = (LinearLayout) view.findViewById(R.id.logView_ll_log);

        int x = 0;
        int scanCount = db.getScanCount();

        while ((scanCount - (x+logEntries)) > 0 && x < 10) {
            View newview = LayoutInflater.from(view.getContext()).inflate(R.layout.layoutpart_logentry, null);
            GlucoseData gd = db.getScanData(scanCount - (x+logEntries));
            Date date = MainActivity.sqlDateFormat.parse(gd.getDate());

            TextView tv_date = (TextView) newview.findViewById(R.id.logEntry_tv_date);
            TextView tv_time = (TextView) newview.findViewById(R.id.logEntry_tv_time);
            TextView tv_gl = (TextView) newview.findViewById(R.id.logEntry_tv_glucoselevel);
            ImageView iv_arrow = (ImageView) newview.findViewById(R.id.logEntry_tv_arrow);

            tv_date.setText(logDateFormat.format(date));
            tv_time.setText(logTimeFormat.format(date));

            if(unit.equals("mg/dl")) { tv_gl.setText(String.valueOf(gd.getGlucoseLevel())); } else {tv_gl.setText(String.valueOf(MainActivity.convertmgdlTommoll(gd.getGlucoseLevel())));}

            if (gd.getPrediction() == GlucoseData.Prediction.FALLING.ordinal()) {
                iv_arrow.setImageResource(R.drawable.arrow_falling);
            } else if (gd.getPrediction() == GlucoseData.Prediction.FALLING_SLOW.ordinal()) {
                iv_arrow.setImageResource(R.drawable.arrow_falling_slow);
            } else if (gd.getPrediction() == GlucoseData.Prediction.RISING.ordinal()) {
                iv_arrow.setImageResource(R.drawable.arrow_rising);
            } else if (gd.getPrediction() == GlucoseData.Prediction.RISING_SLOW.ordinal()) {
                iv_arrow.setImageResource(R.drawable.arrow_rising_slow);
            } else {
                iv_arrow.setImageResource(R.drawable.arrow_constant);
            }

            my_linear_layout.addView(newview,my_linear_layout.getChildCount()-1);
            x++;
        }

        logEntries += 10;

    }

}