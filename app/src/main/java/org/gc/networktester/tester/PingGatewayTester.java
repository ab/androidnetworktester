/*
 *
 * Copyright (C) 2011 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import android.app.AlertDialog;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;
import org.gc.networktester.util.Log;
import org.gc.networktester.util.Util;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.collections4.ArrayUtils;

public class PingGatewayTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private ImageView imageview;
    private TextView textview;
    private ImageView imageviewInfo;
    private AlertDialog dialog = null;
    private int moreInfoMessageId = R.string.ping_internet_expl;

    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_ping_internet );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_ping_internet );
        textview.setOnClickListener( new MoreInfoOnClickListener() );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_ping_internet );
        progressbar.setVisibility( View.GONE );
        imageview = (ImageView) mainActivity.findViewById( R.id.main__image_ping_internet );
        imageview.setVisibility( View.GONE );
        imageview.setOnClickListener( new MoreInfoOnClickListener() );
        imageviewInfo = (ImageView) mainActivity.findViewById( R.id.main__image_ping_internet_info );
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
        moreInfoMessageId = R.string.ping_internet_expl;
    }
    
    public boolean isActive() {
        return checkbox.isChecked();
    }

    public void setActive( boolean value ) {
        checkbox.setChecked( value );
    }

    private String formatIP(int ip) {
        byte[] myIPAddress = BigInteger.valueOf(ip).toByteArray();
        java.util.Collections.reverse(myIPAddress);
        InetAddress myInetIP = null;
        try {
            myInetIP = InetAddress.getByAddress(myIPAddress);
        } catch (UnknownHostException e) {
            Log.error("Unexpected unknown host when trying to look up " + ip);
            return null;
        }
        return myInetIP.getHostAddress();
    }

    public String getRouterIPAddress() {
        WifiManager wifiManager = (WifiManager) mainAct.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();
        int ip = info.gateway;
        return formatIP(ip);
    }

    /*
    public String getRouterTake2() {
        ConnectivityManager cm = (ConnectivityManager)
                mainAct.getSystemService(Context.CONNECTIVITY_SERVICE);

        WifiManager wifiManager = (WifiManager) mainAct.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();

        Network net = wifiManager.getCurrentNetwork();
        LinkProperties prop = cm.getLinkProperties(net);
        for (RouteInfo route : prop.getRoutes()) {
                if (route.isDefaultRoute()) {
                    return route.getGateway().getHostAddress());
                }
            }

    }
    */


    public boolean performTest() {
        mainAct.runOnUiThread( new Thread() { public void run() { progressbar.setVisibility( View.VISIBLE ); } } );
        try {



            InetAddress address = InetAddress.getByName(dnsServerAddress);
            long now = SystemClock.uptimeMillis();

            Log.debug("Testing reachability of " + address.getHostAddress());

            final boolean result = address.isReachable(2000);
            long time_one = SystemClock.uptimeMillis() - now;

            final String str = mainAct.getString( R.string.ping_time, time_one);

            if (result) {
                Log.debug(address.getHostAddress() + " is reachable");
            } else {
                Log.debug(address.getHostAddress() + " is not reachable");
            }

            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setText( str );
                if (result) {
                    imageview.setImageResource(R.drawable.real_web_ok);
                    moreInfoMessageId = R.string.ping_internet_success;
                } else {
                    imageview.setImageResource(R.drawable.failure);
                    moreInfoMessageId = R.string.ping_internet_failure;
                }
            } } );
            return true;
            
        } catch ( UnknownHostException e ) {
            mainAct.runOnUiThread(new Thread() {
                public void run() {
                    textview.setText(R.string.typical_error_unknownhost);
                    imageview.setImageResource(R.drawable.failure);
                    moreInfoMessageId = R.string.tester_not_tested_expl;
                }
            });
            return false;

        } catch ( final IOException ioe ) {
            Log.debug( Util.printException( ioe ) );
            mainAct.runOnUiThread( new Thread() { public void run() {
                if ( ioe instanceof SocketTimeoutException) {
                    textview.setText( mainAct.getString( R.string.typical_error_connectiontimeout ) );
                } else if ( ioe instanceof ConnectException) {
                    textview.setText( mainAct.getString( R.string.typical_error_connectionrefused ) );
                } else {
                    textview.setText( Util.exceptionMessageOrClass( ioe ) );
                }
                imageview.setImageResource( R.drawable.failure );
                moreInfoMessageId = R.string.tester_not_tested_expl;
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

    public void onPause() {
        // need to detach existing popup windows before pausing/destroying activity
        // (screen orientation change, for example)
        if ( dialog != null ) {
            dialog.dismiss();
        }
    }    
    
}
