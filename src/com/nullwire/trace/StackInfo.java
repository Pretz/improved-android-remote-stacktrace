package com.nullwire.trace;

import java.util.List;

/**
 * Encapsulates a stacktrace and relevant information about the device and OS when the crash occured.
 * @author pretz
 *
 */
public class StackInfo {
	
	private String mPackageVersion;
	private String mPhoneModel;
	private String mAndroidVersion;
	private String mExceptionType;
	private List<String> mStacktrace;
	
	public StackInfo(String packageVersion, String phoneModel,
			String androidVersion, String exceptionType, List<String> stacktrace) {
		super();
		mPackageVersion = packageVersion;
		mPhoneModel = phoneModel;
		mAndroidVersion = androidVersion;
		mStacktrace = stacktrace;
		mExceptionType = exceptionType;
	}
	
	/**
	 * @return The version string of the application at the time of the crash.
	 */
	public String getPackageVersion() {
		return mPackageVersion;
	}
	
	/**
	 * @return The phone model as set in <code>android.os.Build.MODEL</code>.
	 */
	public String getPhoneModel() {
		return mPhoneModel;
	}
	
	/**
	 * @return The version of android as stored in <code>android.os.Build.VERSION.RELEASE</code> at the time of crash.
	 */
	public String getAndroidVersion() {
		return mAndroidVersion;
	}
	
	/**
	 * @return The separate lines of the stacktrace as a list of strings.
	 */
	public List<String> getStacktrace() {
		return mStacktrace;
	}
	
	/**
	 * @return The type of the topmost exception as a fully qualified name, such as <code>java.lang.RuntimeException</code>.
	 */
	public String getExceptionType() {
		return mExceptionType;
	}
}
