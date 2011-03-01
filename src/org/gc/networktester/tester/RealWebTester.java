/*
 *
 * Copyright (C) 2011 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import java.io.IOException;

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

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
    private ImageView imageviewInfo;
    private DefaultHttpClient httpclient;
    private AlertDialog dialog = null;
    private int moreInfoMessageId = R.string.real_web_expl;
    
    public void setupViews( MainActivity mainActivity ) {
        this.mainAct = mainActivity;  
        checkbox = (CheckBox) mainActivity.findViewById( R.id.main__checkbox_real_web );
        textview = (TextView) mainActivity.findViewById( R.id.main__text_real_web );
        textview.setOnClickListener( new MoreInfoOnClickListener() );
        progressbar = (ProgressBar) mainActivity.findViewById( R.id.main__progressbar_real_web );
        progressbar.setVisibility( View.GONE );
        imageview = (ImageView) mainActivity.findViewById( R.id.main__image_real_web );
        imageview.setVisibility( View.GONE );
        imageview.setOnClickListener( new MoreInfoOnClickListener() );
        imageviewInfo = (ImageView) mainActivity.findViewById( R.id.main__image_real_web_info );
        imageviewInfo.setOnClickListener( new MoreInfoOnClickListener() );

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
    
    private class MoreInfoOnClickListener implements OnClickListener {
        public void onClick( View v ) {
            dialog = Util.createDialog( mainAct, moreInfoMessageId ); 
        }
    }
    
    public void prepareTest() {
        checkbox.setEnabled( false );
        textview.setVisibility( View.GONE );
        imageview.setVisibility( View.GONE );
        moreInfoMessageId = R.string.real_web_expl;
    }
    
    public boolean isActive() {
        return checkbox.isChecked();
    }

    public void setActive( boolean value ) {
        checkbox.setChecked( value );
    }
    
    public boolean performTest() {
        mainAct.runOnUiThread( new Thread() { public void run() { progressbar.setVisibility( View.VISIBLE ); } } );
        HttpEntity entity = null;
        try {
            HttpResponse response = httpclient.execute(
                                      new HttpGet( "http://androidnetworktester.googlecode.com/files/realweb.txt" ) );
            entity = response.getEntity();
            if ( entity == null
                 || ! EntityUtils.toString( entity ).equals( "androidnetworktester says it works!\n" ) ) {
                mainAct.runOnUiThread( new Thread() { public void run() {
                    textview.setText( R.string.real_web_fail );
                    imageview.setImageResource( R.drawable.failure );
                    moreInfoMessageId = R.string.real_web_fail_expl;
                } } );
                return false;
            } else {
                mainAct.runOnUiThread( new Thread() { public void run() {
                    textview.setText( R.string.real_web_ok );
                    imageview.setImageResource( R.drawable.real_web_ok );
                    moreInfoMessageId = R.string.real_web_ok_expl;
                } } );
                return true;
            }
            
        } catch ( final Exception e ) {
            Log.d( this.toString(), Util.printException( e ) );
            mainAct.runOnUiThread( new Thread() { public void run() {
                textview.setText( Util.typicalHttpclientExceptionToString( mainAct, e ) );
                imageview.setImageResource( R.drawable.failure );
                moreInfoMessageId = R.string.tester_not_tested_expl;
            } } );
            return false;
            
        } finally {
            if ( entity != null ) {
                try {
                    entity.consumeContent();
                } catch ( IOException e ) {}
            }
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
