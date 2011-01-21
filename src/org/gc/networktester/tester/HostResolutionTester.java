/*
 *
 * Copyright (C) 2010 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;
import org.gc.networktester.util.Util;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HostResolutionTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private ImageView imageview;
    private TextView textview;
        
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_host_resolution );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_host_resolution );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_host_resolution );
        progressbar.setVisibility( View.GONE );
        imageview = (ImageView) mainActivity.findViewById( R.id.main__image_host_resolution );
        imageview.setVisibility( View.GONE );
    }
    
    public boolean prepareTestAndIsActive() {
        checkbox.setEnabled( false );
        textview.setVisibility( View.GONE );
        imageview.setVisibility( View.GONE );
        return checkbox.isChecked();
    }
    
    public boolean performTest() {
        mainAct.runOnUiThread( new Thread() { public void run() { progressbar.setVisibility( View.VISIBLE ); } } );
        try {
            // perform first resolution
            long now = SystemClock.uptimeMillis();
            InetAddress address = InetAddress.getByName( "google.com" );
            long time_one = SystemClock.uptimeMillis() - now;
            if ( mainAct.isWantStop() ) {
                return false;
            }

            // perform second resolution
            now = SystemClock.uptimeMillis();
            address = InetAddress.getByName( "www.google.com" );
            final long time_two = SystemClock.uptimeMillis() - now;

            // display result
            List<String> ip = new ArrayList<String>();
            for ( byte part : address.getAddress() ) {
                ip.add( String.valueOf( Util.unsignedByteToInt( part ) ) );
            }
            Log.d( this.toString(), "Resolved as " + Util.join( ip, "." ) );
            final String str = mainAct.getString( R.string.host_resolved, time_one, time_two );
            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setText( str );
                imageview.setImageResource( Util.getTimingResource( mainAct, time_two ) );
            } } );
            return true;
            
        } catch ( UnknownHostException e ) {
            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setText( R.string.host_unknownhost );
                imageview.setImageResource( R.drawable.failure );
            } } );
            return false;
            
        } finally {
            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setVisibility( View.VISIBLE );
                imageview.setVisibility( View.VISIBLE );
                progressbar.setVisibility( View.GONE ); } } );
        }
    }
    
    public void cleanupTests() {
        checkbox.setEnabled( true );
    }
    
}
