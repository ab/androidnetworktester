/*
 *
 * Copyright (C) 2011 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import java.io.InputStream;
import java.net.Socket;

import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;
import org.gc.networktester.util.Log;
import org.gc.networktester.util.Util;

import android.os.SystemClock;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadTesterHelper {
    
    public static boolean performTest( String name, int expsize, final MainActivity mainAct, final ProgressBar pb,
                                       final TextView text ) {

        final int expsize_ = expsize + 190 - 1024;  // + HTTP protocol overhead - 1024 first bytes read not counted 
        mainAct.runOnUiThread( new Thread() { public void run() {
            pb.setMax( 100 );
            pb.setProgress( 1 );
        } } );
        try {
            Socket sock = new Socket( "zarb.org", 80 );
            if ( mainAct.isWantStop() ) {
                sock.close();
                return false;
            }
            sock.getOutputStream().write( ( "GET /~gc/" + name + " HTTP/1.0\r\n" +
                                            "Host: zarb.org\r\n\r\n" ).getBytes( "US-ASCII" ) );

            InputStream is = sock.getInputStream();
            byte[] b = new byte[ 4096 ];
            int read_count, total_read = 0;
            // circumvent a little bit speed progressing of TCP
            is.read( b, 0, 1024 );

            long time_begin = SystemClock.uptimeMillis();
            while ( ( read_count = is.read( b, 0, 4096 ) ) != -1 ) {
                if ( mainAct.isWantStop() ) {
                    is.close();
                    sock.close();
                    return false;
                }
                total_read += read_count;
                final int total_read_ = total_read;
                final String str
                    = mainAct.getString( R.string.dl_speed,
                                         total_read / 1024.0 / ( ( SystemClock.uptimeMillis() - time_begin ) / 1000.0 ) );
                mainAct.runOnUiThread( new Thread() { public void run() {
                    text.setText( str );
                    pb.setProgress( 100 * total_read_ / expsize_ ); }});
            }
            mainAct.runOnUiThread( new Thread() { public void run() { pb.setProgress( 100 ); } } );

            if ( total_read < expsize / 2 ) {
                mainAct.runOnUiThread( new Thread() { public void run() {
                    Toast.makeText( mainAct, R.string.dl_too_small, Toast.LENGTH_LONG ).show(); } } ); 
            }

            is.close();
            sock.close();
            return true;

        } catch ( Exception e ) {
            Log.debug( Util.printException( e ) );
            // special case common error when data is not available
            final String str = Util.typicalHttpclientExceptionToString( mainAct, e );
            mainAct.runOnUiThread( new Thread() { public void run() { text.setText( str ); } } );
            return false;
        }
    }    
}
