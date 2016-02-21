package de.cmklug.liapp;


import android.content.Context;

import java.util.List;

public class Extrapolation {
    private Context mCon;

    public Extrapolation(Context con)
    {
        mCon = con;
    }

    public int calculateCurrentGlucoseLevel(List<GlucoseData> trent, List<GlucoseData> history){

        int extpol = 0;
		
        /* removed extrapolation */ 
		
        return extpol;
    }

}
