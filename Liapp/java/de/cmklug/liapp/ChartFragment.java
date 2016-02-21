package de.cmklug.liapp;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChartFragment extends Fragment {

    private String title;
    private int page;
    private  View view;

    public static ChartFragment newInstance(int page, String title) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }


    public ChartFragment() {

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

        view = inflater.inflate(R.layout.fragment_lastscanchart, container, false);

        LineChart cv_LastScan = (LineChart) view.findViewById(R.id.cv_LastScan);

        cv_LastScan.setOnChartGestureListener(new myChartGestureListener(cv_LastScan));

        XAxis xAxis = cv_LastScan.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(getResources().getColor(R.color.colorGlucoseNow));
        xAxis.enableGridDashedLine(5f, 5f, 0f);
        xAxis.setDrawLimitLinesBehindData(true);

        YAxis yAxisLeft = cv_LastScan.getAxisLeft();
        YAxis yAxisRight = cv_LastScan.getAxisRight();

        yAxisRight.setEnabled(false);

        yAxisLeft.setTextSize(18f); // set the textsize
        yAxisLeft.setTextColor(getResources().getColor(R.color.colorGlucoseNow));
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxisLeft.setStartAtZero(true);
        yAxisLeft.setYOffset(-6f);
        yAxisLeft.setAxisMinValue(0.0f);

        Legend legend = cv_LastScan.getLegend();
        legend.setEnabled(false);

        // no description text
        cv_LastScan.setDescription("");
        cv_LastScan.setNoDataText(getResources().getString(R.string.no_data));
        cv_LastScan.setNoDataTextDescription("");

        // enable touch gestures
        cv_LastScan.setTouchEnabled(true);

        // enable scaling and dragging
        cv_LastScan.setDragEnabled(true);
        cv_LastScan.setScaleEnabled(true);
        cv_LastScan.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        cv_LastScan.setPinchZoom(true);


        MyMarkerView mv = new MyMarkerView(view.getContext(), R.layout.custom_marker_view);

        // set the marker to the chart
        cv_LastScan.setMarkerView(mv);

        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                cv_LastScan.setHardwareAccelerationEnabled(false);

            } else {
               cv_LastScan.setHardwareAccelerationEnabled(true);
            }
        } catch (Exception e) {
        }

        refresh();

        return (view);
    }

    public void refresh() {

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(view.getContext().getApplicationContext());

        ImageView iv_unit;
        iv_unit = (ImageView)view.findViewById(R.id.iv_unit);


        if(sharedPrefs.getString("pref_unit", "mg/dl").equals("mg/dl")) {
            iv_unit.setImageResource(R.drawable.unit_mgdl);
        }else{
            iv_unit.setImageResource(R.drawable.unit_mmoll);
        }


        LineChart cv_LastScan = (LineChart) view.findViewById(R.id.cv_LastScan);
        YAxis yAxisLeft = cv_LastScan.getAxisLeft();

        yAxisLeft.removeAllLimitLines();

        LimitLine ll_max = new LimitLine(Float.valueOf(sharedPrefs.getString("pref_zb_max", "-100.0")), getResources().getString(R.string.pref_zb_max));
        ll_max.setLineWidth(4f);
        ll_max.setTextSize(12f);

        LimitLine ll_min = new LimitLine(Float.valueOf(sharedPrefs.getString("pref_zb_min",  "-100.0")), getResources().getString(R.string.pref_zb_min));
        ll_min.setLineWidth(4f);
        ll_min.setTextSize(12f);
        ll_min.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);

        Legend legend = cv_LastScan.getLegend();
        legend.setEnabled(false);

        // set an alternative background color
        if(sharedPrefs.getBoolean("pref_nightmode", false)) {
            cv_LastScan.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
            ll_max.setLineColor(getResources().getColor(R.color.colorZielbereichDark));
            ll_max.setTextColor(getResources().getColor(R.color.colorZielbereichDark));

            ll_min.setLineColor(getResources().getColor(R.color.colorZielbereichDark));
            ll_min.setTextColor(getResources().getColor(R.color.colorZielbereichDark));
        }else{
            cv_LastScan.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));
            ll_max.setLineColor(getResources().getColor(R.color.colorZielbereichLight));
            ll_max.setTextColor(getResources().getColor(R.color.colorZielbereichLight));

            ll_min.setLineColor(getResources().getColor(R.color.colorZielbereichLight));
            ll_min.setTextColor(getResources().getColor(R.color.colorZielbereichLight));
        }

        yAxisLeft.addLimitLine(ll_max);
        yAxisLeft.addLimitLine(ll_min);

       /* if(cv_LastScan.getData() != null) {
            if( cv_LastScan.getData().getDataSets().get(0).getYMax() < Float.valueOf(sharedPrefs.getString("pref_default_range", "0.0"))) {
                yAxisLeft.setAxisMaxValue(Float.valueOf(sharedPrefs.getString("pref_default_range", "0.0")));
            }
        }*/

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        yVals.add(new Entry(Float.valueOf(sharedPrefs.getString("pref_zb_max", "-100.0")), 0));
        yVals.add(new Entry(Float.valueOf(sharedPrefs.getString("pref_zb_max", "-100.0")), cv_LastScan.getXAxis().getValues().size() - 1));

        LineDataSet setarea = new LineDataSet(yVals, "area");
        setarea.setLineWidth(4f);
        setarea.setDrawCircleHole(false);
        setarea.setDrawCircles(false);
        setarea.setDrawValues(false);
        setarea.setDrawCubic(false);
        setarea.setDrawHighlightIndicators(false);
        ArrayList<LineDataSet> areaSets = new ArrayList<LineDataSet>();
        areaSets.add(setarea); // add the datasets

        // create a data object with the datasets
        if(cv_LastScan.getXAxis().getValues().size() > 2) {
            LineData ld_area = new LineData(cv_LastScan.getXAxis().getValues(), areaSets);
            cv_LastScan.setData(ld_area);
        }

    }

}