package de.cmklug.liapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Eula {

    private String EULA_PREFIX = "eula_";
    private Activity mActivity;
    int text_show = 0;

    public Eula(Activity context, int text) {
        mActivity = context;
        text_show = text;
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    public void show() {
        PackageInfo versionInfo = getPackageInfo();

        // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
        final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
        if(hasBeenShown == false){

            // Show the Eula
            String title = mActivity.getString(R.string.app_name) + " v" + versionInfo.versionName;

            //Includes the updates as well so users know what changed.
            if(text_show == 1) {
                String message = mActivity.getString(R.string.updates) + "\n\n" + mActivity.getString(R.string.eula);

                final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)

                        .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Mark this version as read.
                                dialogInterface.dismiss();

                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Close the activity as they have declined the EULA
                                mActivity.finish();
                            }

                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                            // Close the activity as they have declined the EULA
                            mActivity.finish();
                            }
                        });

                builder.create().show();

            }else if(text_show == 2){
                String message = mActivity.getString(R.string.eula2);

                final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)

                        .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Mark this version as read.

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean(eulaKey, true);
                                editor.commit();
                                dialogInterface.dismiss();

                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Close the activity as they have declined the EULA
                                mActivity.finish();
                            }

                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                            // Close the activity as they have declined the EULA
                            mActivity.finish();
                            }
                        });

                builder.create().show();
            }

        }
    }


}