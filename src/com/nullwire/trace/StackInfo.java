package com.nullwire.trace;

import java.util.List;

/**
 * Encapsulates a stacktrace and relevant information about the device and OS when the crash occured.
 * @author pretz
 *
 */
public class StackInfo {
	
	private final String mPackageVersion;
	private final String mPhoneModel;
	private final String mAndroidVersion;
	private final String mExceptionType;
	private final String mThreadName;
	private final String mMessage;
	private final List<StackTraceElement> mStacktrace;
	private StackInfo mCause;
	
	public StackInfo(String packageVersion, String phoneModel,
			String androidVersion, String exceptionType, String threadName, String message, List<StackTraceElement> stacktrace) {
		super();
		mPackageVersion = packageVersion;
		mPhoneModel = phoneModel;
		mAndroidVersion = androidVersion;
		mThreadName = threadName;
		mMessage = message;
		mStacktrace = stacktrace;
		mExceptionType = exceptionType;
	}
	
	/**
	 * The version string of the application at the time of the crash.
	 */
	public String getPackageVersion() {
		return mPackageVersion;
	}
	
	/**
	 * The phone model as set in <code>android.os.Build.MODEL</code>.
	 */
	public String getPhoneModel() {
		return mPhoneModel;
	}
	
	/**
	 * The version of android as stored in <code>android.os.Build.VERSION.RELEASE</code> at the time of crash.
	 */
	public String getAndroidVersion() {
		return mAndroidVersion;
	}
	
	/**
	 * The separate lines of the stacktrace as a list of strings.
	 */
	public List<StackTraceElement> getStacktrace() {
		return mStacktrace;
	}

	/**
	 * Gets the name of the thread that this StackInfo is for.
	 * @return a String name of the thread, as retrieved by {@link Thread#getName()}
	 */
	public String getThreadName() {
		return mThreadName;
	}

	/**
	 * The type of the topmost exception as a fully qualified name, such as <code>java.lang.RuntimeException</code>.
	 */
	public String getExceptionType() {
		return mExceptionType;
	}

	/**
	 * Returns the Message associated with this stack, usually from {@link Throwable#getMessage()}
	 * @return a string message summarizing the overall fault
	 */
	public String getMessage() {
		return mMessage;
	}

	public StackInfo getCause() {
		return mCause;
	}

	void addCause(StackInfo info) {
		mCause = info;
	}
}
