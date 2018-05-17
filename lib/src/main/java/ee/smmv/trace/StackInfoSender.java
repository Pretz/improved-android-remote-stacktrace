package ee.smmv.trace;

import java.util.Collection;

/**
 * A StackInfoSender receives a list of StackInfos for previous crashes at
 * application startup time. A typical implementation will send them to the developer
 * over http.
 * @author pretz
 *
 */
public interface StackInfoSender {

	/**
	 * This method will be called during application startup if there are crash reports
	 * saved to phone memory from a previous application run. It is called on
	 * the same thread that {@link ExceptionHandler.register} is called from.
	 * <p>
	 * android-remote-stacktrace clears the crash reports from the device once
	 * it calls this method, so it won't be called multiple times for the same
	 * crash.
	 *  
	 * @param stackInfos Each {@link StackInfo} represents a single crash event that has been previously saved.
	 * @param packageName The application package name, if your application wants to save it with
	 * the stacktraces.
	 */
	public void submitStackInfos(Collection<StackInfo> stackInfos, String packageName);

}
