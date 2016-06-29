package com.dvnor.filmbib;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class AboutActivity extends Activity {

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		final TextView mTitleTextView = (TextView) findViewById(R.id.title_text);
		Typeface tf = Typeface.createFromAsset(getAssets(), "RobotoCondensed-Regular.ttf");
		mTitleTextView.setTypeface(tf, Typeface.BOLD);
		mTitleTextView.setText("Om tjenesten");

		findViewById(R.id.search).setVisibility(View.GONE);
		findViewById(R.id.about).setVisibility(View.GONE);
		webView = (WebView)findViewById(R.id.webView);
	    webView.loadUrl("file:///android_asset/filmbib.html");
	}
}
