package com.dvnor.filmbib;

import java.io.File;


import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

public class DownloadVideoPlayer extends Activity {

	private final String				TAG = "DownloadVideoPlayer";
	public static final String 			EXTRA_URL = "extra_url";
	private View 						mDecorView ;
	private String 						vidFile = "";
	private MediaController 			controller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloaded_video_player);

		 mDecorView = getWindow().getDecorView();

		Intent intent = getIntent();
		if (intent.hasExtra(EXTRA_URL)) {
			vidFile = intent.getStringExtra(EXTRA_URL);
		}
		Log.d(TAG, "vidFilevidFile: " + vidFile.toString());
		VideoView myVideoView = (VideoView) findViewById(R.id.videoView1);
		controller = new MediaController(this);
		myVideoView.setVideoPath(vidFile);
		myVideoView.setMediaController(controller);
		myVideoView.requestFocus();
		myVideoView.start();
		myVideoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
			}
		});

	}
	
	 private void hideSystemUI() {
		    mDecorView.setSystemUiVisibility(
		            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
		            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
		            | View.SYSTEM_UI_FLAG_IMMERSIVE);
		}
	private void showSystemUI() {
		    mDecorView.setSystemUiVisibility(
		            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}

}
