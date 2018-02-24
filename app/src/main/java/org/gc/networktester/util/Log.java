package org.gc.networktester.util;

public class Log {

    public enum Type {
        DEBUG { public int value() { return 20; } },
        INFO { public int value() { return 30; } },
        WARN { public int value() { return 40; } },
        ERROR { public int value() { return 50; } };
        public abstract int value();
    }; 

    private static Type minLevel = Type.DEBUG;
    
    public static void setMinLevel( Type type ) {
        minLevel = type;
    }
    
    private static void log( Type type, Object what ) {
        if ( type.value() < minLevel.value() ) {
            return;
        }
        StackTraceElement trace = Thread.currentThread().getStackTrace()[ 4 ];
        String me = "/" + trace.getFileName().replace( ".java", "" ) + ":"
                     + trace.getLineNumber() + "(" + trace.getMethodName() + ")";
        String msg = what.toString();
        if ( type == Type.DEBUG ) { 
            android.util.Log.d( me, msg );
        } else if ( type == Type.INFO ) { 
            android.util.Log.i( me, msg );
        } else if ( type == Type.WARN ) { 
            android.util.Log.w( me, msg );
        } else if ( type == Type.ERROR ) { 
            android.util.Log.e( me, msg );
        } else {
            // wtf?
            android.util.Log.e( me, msg );
        }
    }
    
    public static void debug( Object what ) {
        log( Type.DEBUG, what ); 
    }
    public static void info( Object what ) {
        log( Type.INFO, what ); 
    }
    public static void warn( Object what ) {
        log( Type.WARN, what ); 
    }
    public static void error( Object what ) {
        log( Type.ERROR, what ); 
    }
    
}



