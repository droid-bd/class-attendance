package com.myattendance.checkattendance;

import com.netcse.myattendance.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class DetailsPage extends Activity {

	ProgressDialog progress;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_details);
		
		setTitle("About Us!");
		
		Bundle localBundle = getIntent().getExtras();
	    String str = localBundle.getString("url");
	    setTitle(localBundle.getString("title"));
		
		this.progress = new ProgressDialog(this);
	    this.progress.setTitle("Please wait!");
	    this.progress.setMessage("Data is loading.");
	    
	    WebView localWebView = (WebView)findViewById(R.id.web_view);
	    localWebView.getSettings().setJavaScriptEnabled(true);
	    localWebView.getSettings().setBuiltInZoomControls(true);
	    localWebView.loadUrl(str);
	    
	    localWebView.setWebChromeClient(new WebChromeClient()
	    {
	      public void onProgressChanged(WebView paramAnonymousWebView, int paramAnonymousInt)
	      {
	        super.onProgressChanged(paramAnonymousWebView, paramAnonymousInt);
	        if (paramAnonymousInt < 95)
	        {
	          DetailsPage.this.progress.show();
	          return;
	        }
	        DetailsPage.this.progress.hide();
	      }
	    });
	    localWebView.setWebViewClient(new WebViewClient()
	    {
	      public void onPageFinished(WebView paramAnonymousWebView, String paramAnonymousString)
	      {
	        super.onPageFinished(paramAnonymousWebView, paramAnonymousString);
	        DetailsPage.this.progress.hide();
	      }
	    });
	}
}
