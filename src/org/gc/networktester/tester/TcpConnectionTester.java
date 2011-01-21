/*
 *
 * Copyright (C) 2010 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

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

public class TcpConnectionTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private ImageView imageview;
    private TextView textview;
    
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_tcp_connection );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_tcp_connection );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_tcp_connection );
        progressbar.setVisibility( View.GONE );
        imageview = (ImageView) mainActivity.findViewById( R.id.main__image_tcp_connection );
        imageview.setVisibility( View.GONE );
    }
    
    public boolean prepareTestAndIsActive() {
        checkbox.setEnabled( false );
        textview.setVisibility( View.GONE );
        imageview.setVisibility( View.GONE );
        return checkbox.isChecked();
    }
    
    private Long tcpConnectionReal() {
        try {
            InetAddress address = InetAddress.getByName( "www.google.com" );
            Socket sock = new Socket();
            SocketAddress localaddr = new InetSocketAddress( (InetAddress)null, 0 );
            SocketAddress remoteaddr = new InetSocketAddress( address, 80 );
            sock.bind( localaddr );
            long now = SystemClock.uptimeMillis();
            sock.connect( remoteaddr, 10000 );
            long time = SystemClock.uptimeMillis() - now;
            sock.close();
            return time;

        } catch ( Exception e ) {
            Log.d( this.toString(), Util.printException( e ) );
            // special case common error when data is not available
            final String str = e.getClass().equals( UnknownHostException.class )
                                   ? mainAct.getString( R.string.host_unknownhost )
                                   : mainAct.getString( R.string.failed, e.getMessage() );
            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setText( str );
                imageview.setImageResource( R.drawable.failure );
            } } );
            return null;
        }
    }

    
    public boolean performTest() {
        mainAct.runOnUiThread( new Thread() { public void run() { progressbar.setVisibility( View.VISIBLE ); } } );

        Long time_one = tcpConnectionReal();
        if ( mainAct.isWantStop() ) {
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
                imageview.setImageResource( Util.getTimingResource( mainAct, time_two ) );
            } } );
            return true;
        } else {
            return false;
        }
    }

    public void cleanupTests() {
        checkbox.setEnabled( true );
    }
    
}
