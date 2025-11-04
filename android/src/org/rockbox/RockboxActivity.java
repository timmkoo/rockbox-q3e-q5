/***************************************************************************
 *             __________               __   ___.
 *   Open      \______   \ ____   ____ |  | _\_ |__   _______  ___
 *   Source     |       _//  _ \_/ ___\|  |/ /| __ \ /  _ \  \/  /
 *   Jukebox    |    |   (  <_> )  \___|    < | \_\ (  <_> > <  <
 *   Firmware   |____|_  /\____/ \___  >__|_ \|___  /\____/__/\_ \
 *                     \/            \/     \/    \/            \/
 * $Id$
 *
 * Copyright (C) 2010 Thomas Martitz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY
 * KIND, either express or implied.
 *
 ****************************************************************************/

package org.rockbox;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.rockbox.widgets.ClickwheelView;

public class RockboxActivity extends Activity 
{
    private static final String PREFS_NAME = "RockboxPrefs";
    private static final String PREF_CLICKWHEEL_MODE = "clickwheel_mode";
    
    private RockboxFramebuffer framebuffer;
    private ClickwheelView clickwheel;
    private Button toggleButton;
    private boolean clickwheelMode = true;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Load saved control mode preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        clickwheelMode = prefs.getBoolean(PREF_CLICKWHEEL_MODE, true);
        
        Intent intent = new Intent(this, RockboxService.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.putExtra("callback", new ResultReceiver(new Handler(getMainLooper())) {
            private boolean unzip = false;
            private ProgressDialog loadingdialog;
            private void createProgressDialog()
            {
                loadingdialog = new ProgressDialog(RockboxActivity.this);
                loadingdialog.setMessage(getString(R.string.rockbox_extracting));
                loadingdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                loadingdialog.setIndeterminate(true);
                loadingdialog.setCancelable(false);
                loadingdialog.show();
            }

            @Override
            protected void onReceiveResult(final int resultCode, final Bundle resultData)
            {
                switch (resultCode) {
                    case RockboxService.RESULT_INVOKING_MAIN:
                        if (loadingdialog != null)
                            loadingdialog.dismiss();
                        setupLayout();
                        break;
                    case RockboxService.RESULT_LIB_LOAD_PROGRESS:
                        if (loadingdialog == null)
                            createProgressDialog();
                        loadingdialog.setIndeterminate(false);
                        loadingdialog.setMax(resultData.getInt("max", 100));
                        loadingdialog.setProgress(resultData.getInt("value", 0));
                        break;
                    case RockboxService.RESULT_LIB_LOADED:
                        unzip = resultData.getBoolean("unzip");
                        break;
                    case RockboxService.RESULT_SERVICE_RUNNING:
                        if (!unzip) /* defer to RESULT_INVOKING_MAIN */
                        {
                            setupLayout();
                        }
                        setServiceActivity(true);
                        break;
                    case RockboxService.RESULT_ERROR_OCCURED:
                        Toast.makeText(RockboxActivity.this, resultData.getString("error"), Toast.LENGTH_LONG);
                        break;
                    case RockboxService.RESULT_ROCKBOX_EXIT:
                        finish();
                        break;
                }
            }
        });
        startService(intent);
    }
    
    private void setupLayout() {
        setContentView(R.layout.main);
        
        framebuffer = (RockboxFramebuffer) findViewById(R.id.rockbox_framebuffer);
        clickwheel = (ClickwheelView) findViewById(R.id.clickwheel);
        toggleButton = (Button) findViewById(R.id.toggle_control_mode);
        
        if (framebuffer != null) {
            framebuffer.requestFocus();
        }
        
        // Set up toggle button
        if (toggleButton != null) {
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleControlMode();
                }
            });
        }
        
        // Apply saved control mode
        updateControlMode();
    }
    
    private void toggleControlMode() {
        clickwheelMode = !clickwheelMode;
        
        // Save preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_CLICKWHEEL_MODE, clickwheelMode);
        editor.commit();
        
        updateControlMode();
        
        // Show toast to inform user
        String mode = clickwheelMode ? "Clickwheel Mode" : "Standard Touch Mode";
        Toast.makeText(this, mode, Toast.LENGTH_SHORT).show();
    }
    
    private void updateControlMode() {
        if (framebuffer != null) {
            framebuffer.setClickwheelMode(clickwheelMode);
        }
        
        if (clickwheel != null) {
            clickwheel.setVisibility(clickwheelMode ? View.VISIBLE : View.GONE);
        }
    }

    private void setServiceActivity(boolean set)
    {
        RockboxService s = RockboxService.getInstance();
        if (s != null)
            s.setActivity(set ? this : null);
    }

    public void onResume()
    {
        super.onResume();
        if (framebuffer == null) {
            setupLayout();
        }
        if (framebuffer != null) {
            framebuffer.requestFocus();
        }
        setVisible(true);
        setServiceActivity(true);
    }
    
    /* this is also called when the backlight goes off,
     * which is nice 
     */
    @Override
    protected void onPause() 
    {
        super.onPause();
        /* this will cause the framebuffer's Surface to be destroyed, enabling
         * us to disable drawing */
        setVisible(false);
    }
    
    @Override
    protected void onStop() 
    {
        super.onStop();
        setServiceActivity(false);
    }
    
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        setServiceActivity(false);
    }
}
