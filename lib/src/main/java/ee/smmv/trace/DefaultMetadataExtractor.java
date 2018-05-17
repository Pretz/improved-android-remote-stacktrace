package ee.smmv.trace;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Default app-specific metadata extractor that returns an
 * empty key-value {@link Map}.
 *
 * @author Josef Petrak, josef.petrak@somemove.ee
 */
public class DefaultMetadataExtractor extends MetadataExtractor {

	private static final String TAG = DefaultMetadataExtractor.class.getSimpleName();

	@Override
	public Map<String, String> extract() {
		Log.d(TAG, "Extractor returns an empty key-value map");
		return new HashMap<>();
	}

}
