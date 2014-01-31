package com.att.api.consentactivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.oauth.OAuthService;
import com.att.api.oauth.OAuthToken;
import com.example.iamsdk.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class UserConsentActivity extends Activity implements ATTIAMListener{

	private String fqdn;
	private String clientId;
	private String clientSecret;
	OAuthService osrvc;
	WebView webView ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_consent);
		
		 webView = (WebView) findViewById(R.id.userConsentView);
			
		 Intent i = getIntent();
		 fqdn = i.getStringExtra("fqdn");
		 clientId = i.getStringExtra("clientId");
		 clientSecret =  i.getStringExtra("clientSecret");
		
		 osrvc = new OAuthService(fqdn, clientId, clientSecret);

		
		webView.clearFormData();
		webView.clearCache(true);
		webView.clearHistory();
		webView.clearView();
		webView.clearSslPreferences();
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAppCacheEnabled(false);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.loadUrl("https://api.att.com/oauth/authorize?client_id=hahcoflonje5cxctdbpwtjg966imi6v1&scope=DC,IMMN,MIM,TL&redirect_uri=https://developer.att.com");
		//webView.loadUrl("http://auth-api.att.com/oauth/authorize?client_id=hnpm6f1bsgr4unmqabsrdn46zjukl9n7&response_type=token&scope=immn&redirect_uri=https://www.google.com");		
		webView.setWebViewClient(new myWebViewClient()); 	
	}
	private class myWebViewClient extends WebViewClient {

    	@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			Log.i("onPageStarted", "Start : " + url);

			super.onPageStarted(view, url, favicon);
			if(url.contains("code")) {
				
				/*webView.setVisibility(View.GONE);
				TextView auThorize = (TextView)findViewById(R.id.authorizing);
				auThorize.setVisibility(View.VISIBLE);
				auThorize.setText("AUTHORIZING....");
*/				
				String encodedURL;
				OAuthToken accessToken ;
				try {
					encodedURL = URLEncoder.encode(url, "UTF-8");
					Log.i("onPageStarted", "encodedURL: " + encodedURL);

					//URL urll = new URL(encodedURL);
					String encodedURLSplits[] = encodedURL.split("code%3D");
					String oAuthCode = encodedURLSplits[1];

					Log.i("onPageStarted", "oAuthCode: " + oAuthCode);
					
					Intent returnIntent = new Intent();
					returnIntent.putExtra("oAuthCode", oAuthCode);
					setResult(RESULT_OK,returnIntent);
					finish();

					
					/*GetTokenUsingCodeTask getTokenUsingCodetask  = new GetTokenUsingCodeTask();
					getTokenUsingCodetask.execute(oAuthCode);
					*/
											
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    	
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_consent, menu);
		return true;
	}

	@Override
	public void onSuccess(Object adViewResponse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(Object error) {
		// TODO Auto-generated method stub
		
	}

}