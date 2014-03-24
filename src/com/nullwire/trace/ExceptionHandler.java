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
Alex Pretzlav, alex@turnlav.net
*/

package com.nullwire.trace;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

/**
 * An exception handler that records the exception stacktrace to a file
 * for sending to Analytics.
 *
 * Largely based on the <a href="http://code.google.com/p/android-remote-stacktrace/">android-remote-stacktrace</a> project.
 * <p>
 * Improvements over the original android-remote-stacktrace:
 * <ul>
 * <li>Stacktraces are written to their own "stacktraces" folder
 * instead of the root documents folder of the application.
 * <li>The method of sending stacktraces is customizable. Instead of
 * always POSTing to a server, a StackInfoSender can be provided
 * to perform custom handling of stack traces.
 * <li>Stack filenames will never collide, unlike android-remote-stacktrace
 * which generated random numbers in the hope they wouldn't collide.
 * <li>This version provides more fine-grained customization of logging and debug parameters.
 * </ul>
 * <p>
 * Contributors:<ul> 
 * <li>Mads Kristiansen, mads.kristiansen@nullwire.com
 * <li>Glen Humphrey
 * <li>Evan Charlton
 * <li>Peter Hewitt
 * <li>Alex Pretzlav, alex@turnlav.net
 * </ul>
 */
public class ExceptionHandler implements UncaughtExceptionHandler {

	private static final String CURRENT_VERSION = "VERSION1";
	private static final String NESTED_SET = "===CAUSED_BY===";
	private final UncaughtExceptionHandler mDefaultExceptionHandler;
	private final String mFilePath;
	private final String mAppVersion;
	private final boolean mDebug;

	private static final String TAG = "CollectingExceptionHandler";

	private static String[] stackTraceFileList = null;
	
	/**
	 * Call this from each thread that should submit stack traces when it crashes.
	 * This method creates an <code>HttpPostStackInfoSender</info> for the supplied URL,
	 * and disables debug.
	 * @param context Android Context to use to resolve information about the application
	 * @param url The url to POST stack traces to.
	 * @return
	 */
	public static boolean register(Context context, String url) {
		return register(context, new HttpPostStackInfoSender(url), false);
	}

	/**
	 * Register handler for unhandled exceptions.
	 * <p>
	 * This method has been slightly modified from the android-remote-stacktrace
	 * version to not rely on global static state.
	 * 
	 * @param context Android Context to use to resolve information about the application
	 * @param stackInfoSender A sender to handle any stack traces recorded on the device.
	 * @param debug If true, extra debug information will be logged and the application
	 * version sent with stacktraces will have 'DEBUG-' prepended to it.
	 *
	 *
	 * @author pretz/android-remote-stacktrace
	 */
	public static boolean register(Context context, final StackInfoSender stackInfoSender, final boolean debug) {
		Log.i(TAG, "Registering default exceptions handler");
		// Files dir for storing the stack trace
		final String filePath = context.getDir("stacktraces", 0).getAbsolutePath();
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		final String appVersion = packageInfo.versionName;

		boolean stackTracesFound = false;
		// We'll return true if any stack traces were found
		if (searchForStackTraces(filePath).length > 0) {
			stackTracesFound = true;
		}

		// First of all transmit any stack traces that may be lying around
		// This must be called from the UI thread as it may trigger an analytics flush
		submitStackTraces(filePath, stackInfoSender, debug, packageInfo.packageName);

		new Thread() {
			@Override
			public void run() {
				UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
				if (debug) {
					if (currentHandler != null) {
						Log.d(TAG, "current handler class=" + currentHandler.getClass().getName());
					}
				}
				// don't register again if already registered
				if (!(currentHandler instanceof ExceptionHandler)) {
					// Register default exceptions handler
					Thread.setDefaultUncaughtExceptionHandler(
							new ExceptionHandler(currentHandler, appVersion, filePath, debug));
				}
			}
		}.start();

		return stackTracesFound;
	}

	/**
	 * Search for stack trace files.  This method is unchanged
	 * from android-remote-stacktrace
	 * @return
	 */
	private static String[] searchForStackTraces(String filePath) {
		if (stackTraceFileList != null) {
			return stackTraceFileList;
		}
		File dir = new File(filePath + "/");
		// Try to create the files folder if it doesn't exist
		dir.mkdirs();
		stackTraceFileList = dir.list();
		if (stackTraceFileList == null) {
			// In the cases where reading the dir fails
			// assume there are no stack traces
			stackTraceFileList = new String[0];
		}
		return stackTraceFileList;
	}

	/**
	 * Look into the files folder to see if there are any "*.stacktrace" files.
	 * If any are present, submit them to the stackInfoSender
	 *
	 * This method has been modified from the original android-remote-stacktrace
	 * version to use a StackInfoSender and not to rely on global state.
	 *
	 * @param filesPath The path to search for stack traces
	 * @param stackInfoSender The StackInfoSender to use for StackInfos collected from filesPath.  
	 *
	 * @author pretz/android-remote-stacktrace
	 */
	private static void submitStackTraces(String filesPath, StackInfoSender stackInfoSender,
			boolean debug, String packageName) {
		try {
			if (debug) {
				Log.d(TAG, "Looking for exceptions in: " + filesPath);
			}
			String[] list = searchForStackTraces(filesPath);
			if ( list != null && list.length > 0 ) {
				if (debug) {
					Log.d(TAG, "Found " + list.length + " stacktrace(s)");
				}
				ArrayList<StackInfo> stackInfos = new ArrayList<StackInfo>(list.length);
				for (int i=0; i < list.length; i++) {
					String filePath = filesPath + "/" + list[i];
					// Extract the version from the filename: "packagename-version-...."
					String version = list[i].substring(0, list[i].lastIndexOf('-')); 
					if (debug) {
						Log.d(TAG, "Stacktrace in file '" + filePath + "' belongs to version " + version);
					}
					BufferedReader input =  new BufferedReader(new FileReader(filePath));
					String phoneModel = null;
					String buildVersion = null;
					String exceptionType = null;
					String thread = null;
					String message = null;
					boolean currentVersion = false;
					boolean hasCause = false;
					StackInfo rootInfo = null;
					StackInfo currentInfo = null;
					try {
						String line = null;
						while ((line = input.readLine()) != null) {
							if (!currentVersion) {
								currentVersion = CURRENT_VERSION.equals(line);
								continue;
							}
							if (!currentVersion) {
								Log.i(TAG, "file did not contain valid version" + line);
								return;
							}
							if (phoneModel == null) {
								phoneModel = line;
								continue;
							} else if (buildVersion == null) {
								buildVersion = line;
								continue;
							} else if (exceptionType == null) {
								exceptionType = line;
								continue;
							} else if (thread == null) {
								thread = line;
								continue;
							} else if (message == null) {
								message = line;
								continue;
							}
							if (TextUtils.equals(NESTED_SET, line)) {
								hasCause = true;
								message = exceptionType = null;
								continue;
							}
							if (currentInfo == null) {
								rootInfo = currentInfo = new StackInfo(version, phoneModel, buildVersion, exceptionType, thread, message, new ArrayList<StackTraceElement>());
							}
							if (hasCause) {
								StackInfo cause = new StackInfo(version, phoneModel, buildVersion, exceptionType, thread, message, new ArrayList<StackTraceElement>());
								currentInfo.addCause(cause);
								currentInfo = cause;
								hasCause = false;
							}
							String[] parts = line.split(",");
							StackTraceElement element = new StackTraceElement(parts[0], parts[1], parts[2], Integer.valueOf(parts[3]));
							currentInfo.getStacktrace().add(element);
						}
					} finally {
						input.close();
					}
					if (debug) {
						Log.d(TAG, "Transmitting stack trace: " + TextUtils.join("\n", rootInfo.getStacktrace()));
					}
					stackInfos.add(rootInfo);
				}
				stackInfoSender.submitStackInfos(stackInfos, packageName);
			}
		} catch (FileNotFoundException e) {
			if (debug) {
				Log.i(TAG, "didn't find any stack traces", e);
			}
		} catch (IOException e) {
			Log.w(TAG, "Problem closing files", e);
		} catch (Exception e) {
			Log.e(TAG, "Something really bad just happened that we weren't expecting", e);
		} finally {
			try {
				for (File stack : new File(filesPath + "/").listFiles()) {
					if (debug) {
						Log.v(TAG, "Deleting stack at: " + stack.getAbsolutePath());
					}
					stack.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param defaultExceptionHandler The original exception handler that this handler should forward through after recording a crash.
	 * @param appVersion The version of the application to log with any crashes
	 * @param filePath The path to write stack information to
	 * @param debug Whether to perform extra debug logging and submit the application version with DEBUG- prepended.
	 */
	public ExceptionHandler(UncaughtExceptionHandler defaultExceptionHandler, 
			String appVersion, String filePath, boolean debug) {
		mDefaultExceptionHandler = defaultExceptionHandler;
		mFilePath = filePath;
		mDebug = debug;
		if (mDebug) {
			mAppVersion = "DEBUG-" + appVersion;
		} else {
			mAppVersion = appVersion;
		}
	}

	// Default exception handler
	@Override
	public void uncaughtException(final Thread thread, final Throwable exception) {
		try {
			File dir = new File(mFilePath);
			if (dir.list().length > 20) {
				// Too many stacks, skip
				if (mDefaultExceptionHandler != null) {
					mDefaultExceptionHandler.uncaughtException(thread, exception);
				}
				return;
			}

			// Walk through potential filenames until we find an unused one (should rarely loop)
			File file = null;
			int count = 0;
			do {
				String filename = mAppVersion + "-" + Integer.toString(count);
				file = new File(mFilePath + "/" + filename + ".stacktrace");
				count++;
			} while (file.exists());

			if (mDebug) {
				Log.d(TAG, "Writing unhandled exception to: " + file.getAbsolutePath());
			}
			// Write the stacktrace to disk
			FileOutputStream stream = new FileOutputStream(file);
			final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(stream));
			printWriter.println(CURRENT_VERSION);
			printWriter.print(android.os.Build.MODEL);
			printWriter.print("\n");
			printWriter.print(android.os.Build.VERSION.RELEASE);
			printWriter.print("\n");
			printWriter.print(exception.getClass().getCanonicalName());
			printWriter.print("\n");
			printWriter.println(thread.getName());
			Throwable throwable = exception;
			do {
				printWriter.println(throwable.getClass().getName() + " : " + throwable.getMessage());
				for (StackTraceElement element : throwable.getStackTrace()) {
					printWriter.println(TextUtils.join(",", new String[] {
							element.getClassName(),
							element.getMethodName(),
							element.getFileName(),
							String.valueOf(element.getLineNumber())}));
				}
				throwable = throwable.getCause();
				if (throwable != null) {
					printWriter.println(NESTED_SET);
					printWriter.println(throwable.getClass().getCanonicalName());
				}
				printWriter.flush();
			} while(throwable != null);
			stream.getFD().sync();
			// Close up everything
			printWriter.close();
			if (mDebug) {
				Log.d(TAG, "saved stacktrace to file", exception);
			}
		} catch (Exception ebos) {
			// Nothing much we can do about this - the game is over
			Log.e(TAG, "Exception thrown while logging stack trace", ebos);
		}
		// Call original handler
		if (mDefaultExceptionHandler != null) {
			mDefaultExceptionHandler.uncaughtException(thread, exception);
		}
	}
}
