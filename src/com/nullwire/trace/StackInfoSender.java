package com.nullwire.trace;

import java.util.Collection;

/**
 * A StackInfoSender receives a list of StackInfos for previous crashes at
 * application startup time. A typical implementation will send them to the developer
 * over http.
 * @author pretz
 *
 */
public interface StackInfoSender {

	public void submitStackInfos(Collection<StackInfo> stackInfos, String packageName);
	
}
