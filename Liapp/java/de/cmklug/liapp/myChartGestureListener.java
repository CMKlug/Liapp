package de.cmklug.liapp;

import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

/**
 * Created by CM on 29.10.2015.
 */

public class myChartGestureListener implements OnChartGestureListener {
    LineChart lc;

    public myChartGestureListener(LineChart linechart){
        lc = linechart;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

        if(lc.isFullyZoomedOut()) {
            lc.zoomIn();
        }else{
            lc.fitScreen();
        }
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        float dx = 0;
        int dc = 0;


        Highlight h = lc.getHighlightByTouchPoint(me.getX(), me.getY());

        while (h == null && dx < 200){
            dc++;
            if(dc%2 == 0) {// gerade
                dx++;
                h = lc.getHighlightByTouchPoint(me.getX()+dx, me.getY());
            }else{
                h = lc.getHighlightByTouchPoint(me.getX()-dx, me.getY());
            }
        }

        if (h == null ) {
            lc.highlightTouch(null);
        } else {
            lc.highlightTouch(h);
        }
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }
}
