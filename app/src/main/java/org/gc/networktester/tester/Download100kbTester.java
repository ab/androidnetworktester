/*
 *
 * Copyright (C) 2011 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;

import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Download100kbTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private TextView textview;
    
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_100kb_download );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_100kb_download );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_100kb_download );
        progressbar.setProgress( 0 );
    }
    
    public void prepareTest() {
        checkbox.setEnabled( false );
        textview.setText( R.string.value_na );
        progressbar.setProgress( 0 );
    }
    
    public boolean isActive() {
        return checkbox.isChecked();
    }

    public void setActive( boolean value ) {
        checkbox.setChecked( value );
    }
    
    public boolean performTest() {
        return DownloadTesterHelper.performTest( "100kb.txt", 102400, mainAct, progressbar, textview );
    }

    public void cleanupTests() {
        checkbox.setEnabled( true );
    }
    
    public void onPause() {}
    
}
