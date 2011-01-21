/*
 *
 * Copyright (C) 2010 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;
import org.gc.networktester.util.Util;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RealWebTester implements Tester {
    
    private MainActivity mainAct;
    private CheckBox checkbox;
    private ProgressBar progressbar;
    private ImageView imageview;
    private TextView textview;
    private DefaultHttpClient httpclient;
    
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_real_web );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_real_web );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_real_web );
        progressbar.setVisibility( View.GONE );
        imageview = (ImageView) mainActivity.findViewById( R.id.main__image_real_web );
        imageview.setVisibility( View.GONE );

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );
        HttpProtocolParams.setContentCharset( params, "UTF-8" );
        HttpConnectionParams.setStaleCheckingEnabled( params, false );
        HttpConnectionParams.setConnectionTimeout( params, 10 * 1000 );
        HttpConnectionParams.setSoTimeout( params, 15 * 1000 );
        HttpConnectionParams.setSocketBufferSize( params, 8192 );
        HttpClientParams.setRedirecting( params, false );
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );
        ClientConnectionManager manager = new ThreadSafeClientConnManager( params, schemeRegistry );
        httpclient = new DefaultHttpClient( manager, params );
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
            HttpResponse response = httpclient.execute(
                                      new HttpGet( "http://androidnetworktester.googlecode.com/files/realweb.txt" ) );
            HttpEntity entity = response.getEntity();
            if ( entity == null
                 || ! EntityUtils.toString( entity ).equals( "androidnetworktester says it works!\n" ) ) {
                mainAct.runOnUiThread( new Thread() { public void run() {
                    textview.setText( R.string.real_web_fail );
                    imageview.setImageResource( R.drawable.failure );
                } } );
                return false;
            } else {
                mainAct.runOnUiThread( new Thread() { public void run() {
                    textview.setText( R.string.real_web_ok );
                    imageview.setImageResource( R.drawable.real_web_ok );
                } } );
                return true;
            }
            
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
