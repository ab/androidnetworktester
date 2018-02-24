/*
 *
 * Copyright (C) 2011 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;
import org.gc.networktester.util.Log;
import org.gc.networktester.util.Util;

import android.app.AlertDialog;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TcpConnectionTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private ImageView imageview;
    private TextView textview;
    private ImageView imageviewInfo;
    private AlertDialog dialog = null;
    private int moreInfoMessageId = R.string.tcp_connection_expl;
    
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_tcp_connection );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_tcp_connection );
        textview.setOnClickListener( new MoreInfoOnClickListener() );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_tcp_connection );
        progressbar.setVisibility( View.GONE );
        imageview = (ImageView) mainActivity.findViewById( R.id.main__image_tcp_connection );
        imageview.setVisibility( View.GONE );
        imageview.setOnClickListener( new MoreInfoOnClickListener() );
        imageviewInfo = (ImageView) mainActivity.findViewById( R.id.main__image_tcp_connection_info );
        imageviewInfo.setOnClickListener( new MoreInfoOnClickListener() );
    }

    private class MoreInfoOnClickListener implements OnClickListener {
        public void onClick( View v ) {
            dialog = Util.createDialog( mainAct, moreInfoMessageId ); 
        }
    }
    
    public void prepareTest() {
        checkbox.setEnabled( false );
        textview.setVisibility( View.GONE );
        imageview.setVisibility( View.GONE );
        moreInfoMessageId = R.string.tcp_connection_expl;
    }
    
    public boolean isActive() {
        return checkbox.isChecked();
    }
    
    public void setActive( boolean value ) {
        checkbox.setChecked( value );
    }
    
    private Long tcpConnectionReal() {
        InetAddress address;
        try {
            address = InetAddress.getByName( "www.google.com" );
        } catch ( UnknownHostException e ) {
            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setText( R.string.typical_error_unknownhost );
                imageview.setImageResource( R.drawable.failure );
                moreInfoMessageId = R.string.tester_not_tested_expl;
            } } );
            return null;
        }
        try {   
            Socket sock = new Socket();
            SocketAddress localaddr = new InetSocketAddress( (InetAddress)null, 0 );
            SocketAddress remoteaddr = new InetSocketAddress( address, 80 );
            sock.bind( localaddr );
            long now = SystemClock.uptimeMillis();
            sock.connect( remoteaddr, 10 * 1000 );
            long time = SystemClock.uptimeMillis() - now;
            sock.close();
            return time;

        } catch ( final IOException ioe ) {
            Log.debug( Util.printException( ioe ) );
            mainAct.runOnUiThread( new Thread() { public void run() {
                if ( ioe instanceof SocketTimeoutException ) {
                    textview.setText( mainAct.getString( R.string.typical_error_connectiontimeout ) );
                } else if ( ioe instanceof ConnectException ) {
                    textview.setText( mainAct.getString( R.string.typical_error_connectionrefused ) );
                } else {
                    textview.setText( Util.exceptionMessageOrClass( ioe ) );
                }
                imageview.setImageResource( R.drawable.failure );
                moreInfoMessageId = R.string.tester_not_tested_expl;
            } } );
            return null;
        }
    }

    
    public boolean performTest() {
        mainAct.runOnUiThread( new Thread() { public void run() { progressbar.setVisibility( View.VISIBLE ); } } );

        Long time_one = tcpConnectionReal();
        if ( mainAct.isWantStop() ) {
            mainAct.runOnUiThread( new Thread() { public void run() { progressbar.setVisibility( View.GONE ); } } );
            return false;
        }

        final Long time_two = tcpConnectionReal();

        mainAct.runOnUiThread( new Thread() { public void run() {
            textview.setVisibility( View.VISIBLE );
            imageview.setVisibility( View.VISIBLE );
            progressbar.setVisibility( View.GONE );
        } } );
        
        if ( time_one != null && time_two != null ) {
            final String str = mainAct.getString( R.string.tcp_connected, time_one, time_two );
            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setText( str );
                int timing_resource = Util.getTimingResource( mainAct, time_two );
                imageview.setImageResource( timing_resource );
                if ( timing_resource == R.drawable.timing_good ) {
                    moreInfoMessageId = R.string.timing_test_good;
                } else if ( timing_resource == R.drawable.timing_medium ) {
                    moreInfoMessageId = R.string.timing_test_medium;
                } else {
                    moreInfoMessageId = R.string.timing_test_bad;
                }
            } } );
            return true;
        } else {
            return false;
        }
    }

    public void cleanupTests() {
        checkbox.setEnabled( true );
    }
    
    public void onPause() {
        // need to detach existing popup windows before pausing/destroying activity
        // (screen orientation change, for example)
        if ( dialog != null ) {
            dialog.dismiss();
        }
    }    

}
