/*
 *
 * Copyright (C) 2010 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.tester;

import org.gc.networktester.activity.MainActivity;

public interface Tester {
    
    /** Called by UI thread. */
    public void setupViews( MainActivity mainActivity );
    
    /** Called by UI thread. */
    public boolean prepareTestAndIsActive();
    
    /** Called by background thread. */
    public boolean performTest();
    
    /** Called by UI thread. */
    public void cleanupTests();

}
