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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Download10mbTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private TextView textview;
    private boolean warningDataDone = false;
    
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_10mb_download );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_10mb_download );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_10mb_download );
        progressbar.setProgress( 0 );
        checkbox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                NetworkInfo netinfo
                    = ( (ConnectivityManager) mainAct.getSystemService( Context.CONNECTIVITY_SERVICE ) ).getActiveNetworkInfo();
                if ( ! warningDataDone ) {
                    warningDataDone = true;
                    if ( netinfo != null && netinfo.getType() == ConnectivityManager.TYPE_MOBILE ) {
                        Toast.makeText( mainAct, R.string.warning_data_mobile, Toast.LENGTH_LONG ).show();
                    } else {
                        Toast.makeText( mainAct, R.string.warning_data_generic, Toast.LENGTH_LONG ).show();
                    }
                }
            } } );
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
        return DownloadTesterHelper.performTest( "10mb.txt", 10485760, mainAct, progressbar, textview );
    }

    public void cleanupTests() {
        checkbox.setEnabled( true );
    }

    public void onPause() {}
    
}
