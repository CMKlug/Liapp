package de.cmklug.liapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class DebugActivity extends ActionBarActivity {

    String debug_string = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        TextView tv_debug;
        tv_debug = (TextView)findViewById(R.id.tv_debug);

        Intent intent = getIntent();
        nightmode(intent.getBooleanExtra("nightmode", false));

        if(intent.getBooleanExtra("rotation_lock", false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        debug_string = intent.getStringExtra("tag_data");

        tv_debug.setText(debug_string);
    }

    void nightmode(boolean enabled){

        RelativeLayout rl;
        rl = (RelativeLayout)findViewById(R.id.rl_debug);

        if(enabled == false  ) {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));

        }else {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        }

    }

    public void send_debug_onClick(View arg0){
        boolean error = false;

        if(debug_string.length() > 0) {

                Intent emailClient = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","liapp_debug@marcelklug.de", null));
                emailClient.putExtra(Intent.EXTRA_SUBJECT, "Liapp Debug");
                emailClient.putExtra(Intent.EXTRA_TEXT, debug_string); // + "\n\n" + "Android-Version: " + Build.VERSION.SDK_INT + ";  " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
                //emailClient.setType("text/plain");
                startActivity(Intent.createChooser(emailClient, "eMail Client:"));

        }
    }

}
