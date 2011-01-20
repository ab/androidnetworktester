/*
 *
 * Copyright (C) 2010 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;

import android.util.Log;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Download10kbTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private TextView textview;
    
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_10kb_download );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_10kb_download );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_10kb_download );
        Log.d( this.toString(), "************foo" );
        progressbar.setProgress( 0 );
    }
    
    public boolean prepareTestAndIsActive() {
        checkbox.setEnabled( false );
        textview.setText( R.string.value_na );
        progressbar.setProgress( 0 );
        return checkbox.isChecked();
    }
    
    public boolean performTest() {
        return DownloadTesterHelper.performTest( "10kb.txt", 10240, mainAct, progressbar, textview );
    }

    public void cleanupTests() {
        checkbox.setEnabled( true );
    }
    
}
