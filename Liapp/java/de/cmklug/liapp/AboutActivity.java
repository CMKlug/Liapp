package de.cmklug.liapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;


public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Intent intent = getIntent();
        nightmode(intent.getBooleanExtra("nightmode", false));

        TextView tv_about_version;
        tv_about_version = (TextView) findViewById(R.id.tv_about_version);

        TextView tv_about_translator;
        tv_about_translator = (TextView) findViewById(R.id.tv_about_translator);
        tv_about_translator.setText(getResources().getString((R.string.translator)));

        TextView tv_about_translator_title;
        tv_about_translator_title = (TextView) findViewById(R.id.tv_about_translator_title);
        tv_about_translator_title.setText("Translator (" + Locale.getDefault().getLanguage()+ "):");

        if(tv_about_translator.length() > 1) {
            tv_about_translator.setVisibility(View.VISIBLE);
            tv_about_translator_title.setVisibility(View.VISIBLE);

        }else{
            tv_about_translator.setText("");
            tv_about_translator.setVisibility(View.GONE);
            tv_about_translator_title.setVisibility(View.GONE);
        }

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tv_about_version.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tv_about_version.setText("1.x.x");
        }

        if(intent.getBooleanExtra("rotation_lock", false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        }
    }

    void nightmode(boolean enabled){

        RelativeLayout rl;
        rl = (RelativeLayout)findViewById(R.id.rl_about);

        if(enabled == false  ) {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));

        }else {
            rl.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        }

    }


}
