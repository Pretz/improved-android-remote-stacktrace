/*
Copyright (c) 2009 nullwire aps

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Contributors: 
Mads Kristiansen, mads.kristiansen@nullwire.com
Glen Humphrey
Evan Charlton
Peter Hewitt
*/

package com.nullwire.trace;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

import android.util.Log;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultExceptionHandler;
	
	private static final String TAG = "UNHANDLED_EXCEPTION";

	// constructor
	public DefaultExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler)
	{
		defaultExceptionHandler = pDefaultExceptionHandler;
	}
	 
	// Default exception handler
	public void uncaughtException(Thread t, Throwable e) {
		// Here you should have a more robust, permanent record of problems
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    e.printStackTrace(printWriter);
	    try {
	    	// Random number to avoid duplicate files
	    	Random generator = new Random();
	    	int random = generator.nextInt(99999);    	
	    	// Embed version in stacktrace filename
	    	String filename = G.APP_VERSION+"-"+Integer.toString(random);
	    	Log.d(TAG, "Writing unhandled exception to: " + G.FILES_PATH+"/"+filename+".stacktrace");
		    // Write the stacktrace to disk
	    	BufferedWriter bos = new BufferedWriter(new FileWriter(G.FILES_PATH+"/"+filename+".stacktrace"));
            bos.write(G.ANDROID_VERSION + "\n");
            bos.write(G.PHONE_MODEL + "\n");
            bos.write(result.toString());
		    bos.flush();
		    // Close up everything
		    bos.close();
	    } catch (Exception ebos) {
	    	// Nothing much we can do about this - the game is over
	    	ebos.printStackTrace();
	    }
		Log.d(TAG, result.toString());	    
		//call original handler  
    	defaultExceptionHandler.uncaughtException(t, e);        
	}
}