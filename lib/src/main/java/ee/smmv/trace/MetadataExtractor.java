package ee.smmv.trace;

import android.content.Context;

import java.util.Map;

/**
 * Metadata extractor is used by exception handler
 * to extract app-specific metadata and provide
 * them to be included in {@link StackInfo}.
 *
 * @author Josef Petrak, josef.petrak@somemove.ee
 */
public abstract class MetadataExtractor {

	private Context context;

	/**
	 * Returns context given to exception handler
	 * @return Android context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Sets context for metadata extractor to use
	 * @param context Android context
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Extract app-specific metadata and returns as key-value map.
	 * @return Key-value map
	 */
	abstract Map<String, String> extract();

}
