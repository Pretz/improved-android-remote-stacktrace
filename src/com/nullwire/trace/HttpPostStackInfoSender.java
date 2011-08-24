package com.nullwire.trace;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A StackInfoSender that performs an individual http POST to a URL for each
 * stack info provided. The http requests will be performed inside of a single
 * AsyncTask, so submitStackInfos must be called from the main thread.
 *  
 * The data sent is identical to the data sent in
 * the original android-remote-stacktrace:
 * * package_name
 * * package_version
 * * phone_model
 * * android_version
 * * stacktrace
 * 
 * @author pretz
 *
 */
public class HttpPostStackInfoSender implements StackInfoSender {
	
	private static final String TAG = "HttpPostStackInfoSender";
	
	private final String mPostUrl;
	
	/**
	 * Construct a new HttpPostStackInfoSender that will submit
	 * stack traces by POSTing them to the specified URL.
	 * @param postUrl
	 */
	public HttpPostStackInfoSender(String postUrl) {
		mPostUrl = postUrl;
	}
	
	public void submitStackInfos(Collection<StackInfo> stackInfos, final String packageName) {
		new AsyncTask<StackInfo, Void, Void>() {
			
			@Override
			protected Void doInBackground(StackInfo... infos) {
				final DefaultHttpClient httpClient = new DefaultHttpClient(); 
				for (final StackInfo info : infos) {
					HttpPost httpPost = new HttpPost(mPostUrl);
					final List <NameValuePair> nvps = new ArrayList <NameValuePair>(); 
					nvps.add(new BasicNameValuePair("package_name", packageName));
					nvps.add(new BasicNameValuePair("package_version", info.getPackageVersion()));
                    nvps.add(new BasicNameValuePair("phone_model", info.getPhoneModel()));
                    nvps.add(new BasicNameValuePair("android_version", info.getAndroidVersion()));
                    nvps.add(new BasicNameValuePair("stacktrace", TextUtils.join("\n", info.getStacktrace())));
					try {
						httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
						// We don't care about the response, so we just hope it went well and on with it
						httpClient.execute(httpPost);	
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, "Error sending stack traces", e);
					} catch (ClientProtocolException e) {
						Log.e(TAG, "Error sending stack traces", e);
					} catch (IOException e) {
						Log.e(TAG, "Error sending stack traces", e);
					} 
				}
				return null;
			}
		}.execute(stackInfos.toArray(new StackInfo[0]));
	}

}
