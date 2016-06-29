package com.dvnor.filmbib;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dvnor.Asynctask.DownloadService;
import com.dvnor.Asynctask.DownloadServiceManager;
import com.dvnor.Asynctask.HttpHandler;
import com.dvnor.hls.player.PlayerActivity;
import com.dvnor.utils.AppUtils;
import com.dvnor.utils.BaseClass;
import com.dvnor.utils.Constant;
import com.google.android.exoplayer.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/** This Activity can be displayed from 2 perspective.<br>
 * 1. From home screen ({@link HomeActivity}) <br>
 * 2. From search Screen ({@link SearchActivity})  <br>
 * Based on this, this activity displays videos.
 * @author Raam Kumar*/

public class MovieDetailsActivity extends Activity implements OnClickListener {

	private String 									TAG = "MovieDetailsActivity";
	private SharedPreferences 						sharedpreferences;
	private SharedPreferences.Editor 				sharedPreferenceEditor;


	private Button 									downloadMoview;
	private ImageView 								videoPoster;
	private ImageView								ratingIcon;
	private ProgressBar 							myProgressbar;
	private ImageView 								play;
	private ProgressBar 							progressBarDownload;

	String 											mActivityName,mVideoSelectedId;

	String mId,mTitle,mSynopsis,mProdYear,mMovieLength,mDirector,mWriter,mMusic,mCast,mRating,mFilmType,mImageUrl,mVideoUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fullscreen_demo);

		sharedpreferences = getSharedPreferences(Constant.MYPREFERENCES, Context.MODE_PRIVATE);
		sharedPreferenceEditor = sharedpreferences.edit();

		myProgressbar =(ProgressBar)findViewById(R.id.progressBar1);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {

			mActivityName = bundle.getString(Constant.ACTIVITY_NAME);
			mVideoSelectedId = bundle.getString(Constant.CLICKED_MOVIE_ID_KEY);
			Log.i(TAG, "Movie Details: " + mActivityName + ": " + mVideoSelectedId);

			if(mActivityName.equals("HomeActivity") || mActivityName.equals("SearchActivity"))
			{
				new HttpHandler() {
					
					@Override
					public HttpUriRequest getHttpRequestMethod() {
						HttpGet httpget = null;
						if (AppUtils.isNetworkAvailable(MovieDetailsActivity.this) == true) {
							httpget = new HttpGet(BaseClass.MOVIES_DETAILS_URL+mVideoSelectedId);
							httpget.setHeader("X-Authorization", BaseClass.AUTH_ID);
						return httpget;
					} else {
						AppUtils.Show_Dialog("Please Check your internet connectivity",MovieDetailsActivity.this);
						return httpget;
					}
					}
					@Override
					public void onResponse(String result) {
						Log.e(TAG, "Single Movie DATA: " + result);
						if(result!=null){

							try {
								JSONObject mObj = new JSONObject(result);
								JSONObject movieObj = mObj.getJSONObject(Constant.MOVIE_ROOT_KEY);
								mId = movieObj.getString(Constant.MOVIE_ID_KEY);
								mTitle = movieObj.getString(Constant.MOVIE_TITLE_KEY);
								mSynopsis = movieObj.getString(Constant.MOVIE_SYNOPSIS_KEY);
								mProdYear = movieObj.getString(Constant.MOVIE_PROD_YEAR_KEY);
								mMovieLength = movieObj.getString(Constant.MOVIE_LENGTH_KEY);
								mDirector = movieObj.getString(Constant.MOVIE_DIRECTOR_KEY);
								mWriter = movieObj.getString(Constant.MOVIE_WRITER_KEY);
								mMusic = movieObj.getString(Constant.MOVIE_MUSIC_KEY);
								mCast = movieObj.getString(Constant.MOVIE_CAST_KEY);
								mRating = movieObj.getString(Constant.MOVIE_RATING_KEY);
								mFilmType = movieObj.getString(Constant.MOVIE_FILM_TYPE_KEY);
								mImageUrl = movieObj.getJSONObject(Constant.MOVIE_IMAGE_KEY).getString("small");
								mVideoUrl = movieObj.getString(Constant.MOVIE_VIDEO_URL_KEY).replace("/jwplayer.smil", "/playlist.m3u8");

								initializeAllViews();


							} catch (JSONException e) {
								e.printStackTrace();
							}
						} else {
							AppUtils.Show_Dialog("Please Check your internet connectivity", MovieDetailsActivity.this);
						}
					}
					
				}.execute(getApplicationContext());
			}

		}
		
	}

	/**
	 * Deletes the downloaded file if time limit expiries.
	 * @param videoID ID of the video
     */
	private void deleteFileIfTimeExceeds(String videoID) {

		String path=getCacheDir()+"/"+videoID+".mp4";
		File file = new File ( path ); 
		Log.e(TAG, " Video To Be Deleted: File Exists: " + file.exists() + " : " + file.getAbsolutePath());
		if ( file.exists() ) 
		{
			file.delete();
			sharedPreferenceEditor.remove(Constant.MOVIE_DETAILS + ";" + videoID);
			sharedPreferenceEditor.apply();
		}
	}

	/** This method initializes all the views */
	private void initializeAllViews()
	{

		Map<String,?> keys = sharedpreferences.getAll();
		for(Map.Entry<String,?> entry : keys.entrySet()){
		  Log.d("map values",entry.getKey() + ": " +  entry.getValue().toString());
		  if(entry.getKey().contains(Constant.MOVIE_DETAILS)){
			  String value = entry.getValue().toString();
			  String[] data = value.split(";");
			  Log.i(TAG, "isDownloadedTimeExceedsis: " + AppUtils.isDownloadedTimeExceeds(data[0]));
			  if(AppUtils.isDownloadedTimeExceeds(data[0])){
				  deleteFileIfTimeExceeds(data[1]);
			  }

		  }
		 }
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
		findViewById(R.id.ScrollView).setVisibility(View.VISIBLE);
		ImageButton Back =(ImageButton)findViewById(R.id.Back);
		Back.setOnClickListener(this);

		TextView vTitle=(TextView)findViewById(R.id.title_text);
		Typeface titleTf = Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf");
		vTitle.setTypeface(titleTf, Typeface.NORMAL);
		vTitle.setText(Html.fromHtml(mTitle));

		TextView filmType=(TextView)findViewById(R.id.filmType);
		filmType.setTypeface(tf, Typeface.NORMAL);
		if(mFilmType.equals("S")){
			mFilmType = "Kortfilm";
		}else if (mFilmType.equals("D")){
			mFilmType = "Dokumentar";
		}else{
			mFilmType = "Spillefilm";
		}
		String filmtypeText="<font color='#32A3BF'>FILMTYPE: </font>"+mFilmType;
		filmType.setText(Html.fromHtml(filmtypeText));

		TextView productionYear=(TextView)findViewById(R.id.productionYear);
		String mProdYearText="<font color='#32A3BF'>PRODUKSJONSÅR: </font>"+mProdYear;
		productionYear.setText(Html.fromHtml(mProdYearText));
		productionYear.setTypeface(tf, Typeface.NORMAL);

		TextView filmLength=(TextView)findViewById(R.id.filmLength);
		String mMovieLengthText="<font color='#32A3BF'>SPILLETID: </font>"+mMovieLength;
		filmLength.setText(Html.fromHtml(mMovieLengthText));
		filmLength.setTypeface(tf, Typeface.NORMAL);

		TextView director=(TextView)findViewById(R.id.director);
		String mDirectorText="<font color='#32A3BF'>REGISSØR: </font>"+mDirector;
		director.setText(Html.fromHtml(mDirectorText));
		director.setTypeface(tf, Typeface.NORMAL);

		TextView writer=(TextView)findViewById(R.id.writer);
		String mWriterText="<font color='#32A3BF'>MANUS: </font>"+mWriter;
		writer.setText(Html.fromHtml(mWriterText));
		writer.setTypeface(tf, Typeface.NORMAL);

		TextView music=(TextView)findViewById(R.id.music);
		String mMusicText="<font color='#32A3BF'>MUSIKK AV: </font>"+mMusic;
		music.setText(Html.fromHtml(mMusicText));
		music.setTypeface(tf, Typeface.NORMAL);

		TextView cast=(TextView)findViewById(R.id.cast);
		String mCastText="<font color='#32A3BF'>MED: </font>"+mCast;
		cast.setText(Html.fromHtml(mCastText));
		cast.setTypeface(tf, Typeface.NORMAL);
		
		play=(ImageView)findViewById(R.id.play);
		play.setOnClickListener(this);
		
		ratingIcon = (ImageView)findViewById(R.id.ratingIcon);
			int rating_drawable = 0;
			  switch (mRating)
              {
                  case "1":
                	  rating_drawable = R.drawable.liten1;
                      break;
                  case  "6":
                	  rating_drawable = R.drawable.liten6;
                      break;
                  case  "9":
                	  rating_drawable = R.drawable.liten9;
                      break;
                  case  "12":
                	  rating_drawable = R.drawable.liten12;
                      break;
                  case  "15":
                	  rating_drawable = R.drawable.liten15;
                      break;
                  case  "18":
                	  rating_drawable = R.drawable.liten18;
                      break;

                  default:
                      break;
              }
			  try {
				ratingIcon.setBackgroundResource(rating_drawable);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		
		videoPoster =(ImageView)findViewById(R.id.VideoPoster);
		videoPoster.setOnClickListener(this);
		// Glide.with(this).load(mImageUrl).centerCrop().placeholder(R.drawable.app_logo).crossFade().into(videoPoster);
		 Glide.with(this)
	     .load(mImageUrl)
	     .listener(new RequestListener<String, GlideDrawable>() {
	         @Override
	         public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
	             return false;
	         }

	         @Override
	         public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
	        	 myProgressbar.setVisibility(View.GONE);
	        	 play.setVisibility(View.VISIBLE);
	             return false;
	         }
	     }).crossFade().into(videoPoster);

		TextView filmNameSynopsis=(TextView)findViewById(R.id.filmNameSynopsis);
		filmNameSynopsis.setTypeface(titleTf, Typeface.NORMAL);
		filmNameSynopsis.setText(Html.fromHtml(mTitle));

		TextView synopsys=(TextView)findViewById(R.id.synopsis);
		synopsys.setTypeface(tf, Typeface.NORMAL);
		synopsys.setText(Html.fromHtml(mSynopsis));
		
		downloadMoview = (Button)findViewById(R.id.downloadMoview);
		progressBarDownload = (ProgressBar)findViewById(R.id.progressBarDownload);
		
		if (DownloadServiceManager.getSharedDownloadManager().isDowloadingMoview(mVideoSelectedId)) {
			downloadMoview.setText("Venter...");
			downloadMoview.setEnabled(false);
			play.setEnabled(false);
			videoPoster.setEnabled(false);
		}else if (isVideoExists(mVideoSelectedId)) {
			downloadMoview.setText("Nedlastet");
			downloadMoview.setEnabled(true);
			play.setEnabled(true);
			videoPoster.setEnabled(true);
		}
		
		downloadMoview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (AppUtils.isNetworkAvailable(MovieDetailsActivity.this) == true) {
					if (AppUtils.getInternetType(MovieDetailsActivity.this).equals(AppUtils.TYPE_WIFI)){
						
						checkAndDownload();
					
					}else {
						showDownloadWarning();
					}
					
			} else {
				AppUtils.Show_Dialog("Please Check your internet connectivity",MovieDetailsActivity.this);
			}
				
			
			}

			
		});
	}
	
	protected void checkAndDownload() {
		
		if (isVideoExists(mVideoSelectedId)) {
			Intent fileIntent = new Intent(MovieDetailsActivity.this, DownloadListActivity.class);
			startActivity(fileIntent);
		} else {

			if (DownloadServiceManager.getSharedDownloadManager()
					.isDowloadingMoview(mVideoSelectedId)) {
				Intent intent = DownloadServiceManager.getSharedDownloadManager().videoDownloading
						.get(mVideoSelectedId);
				intent.putExtra("receiver", new DownloadReceiver(new Handler()));
				intent.putExtra("movieId", mVideoSelectedId);
				intent.putExtra("movieName", mTitle);
				DownloadServiceManager.getSharedDownloadManager().addNewMovieToDownload(intent,
						mVideoSelectedId);
				DownloadServiceManager.getSharedDownloadManager().videoDetails.put(mVideoSelectedId,
						mTitle);
				downloadMoview.setEnabled(false);
				play.setEnabled(false);
				videoPoster.setEnabled(false);
			} else {

				Intent intent = new Intent(MovieDetailsActivity.this, DownloadService.class);
				intent.putExtra("url", BaseClass.MOVIES_DOWNLOAD_URL + mVideoSelectedId);
				intent.putExtra("movieId", mVideoSelectedId);
				intent.putExtra("movieName", mTitle);
				DownloadReceiver downloadReciver = new DownloadReceiver(new Handler());
				DownloadServiceManager.getSharedDownloadManager().videoDownloadingRecivier
						.put(mVideoSelectedId, downloadReciver);
				DownloadServiceManager.getSharedDownloadManager().videoDetails.put(mVideoSelectedId,
						mTitle);
				intent.putExtra("receiver", downloadReciver);
				startService(intent);
				DownloadServiceManager.getSharedDownloadManager().addNewMovieToDownload(intent,
						mVideoSelectedId);
				downloadMoview.setText("Venter…");
				downloadMoview.setEnabled(false);
				play.setEnabled(false);
				videoPoster.setEnabled(false);
			}
		}
		
	}

	private void showDownloadWarning() {
		
		 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	      alertDialogBuilder.setMessage("Du er ikke tilkoblet noe trådløst nettverk. Ønsker du å laste ned allikevel?");
	      
	      alertDialogBuilder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
	         @Override
	         public void onClick(DialogInterface dialog, int arg1) {
	        	 dialog.dismiss();
	        	 checkAndDownload();
	         }
	      });
	      
	      alertDialogBuilder.setNegativeButton("Nei",new DialogInterface.OnClickListener() {
	         @Override
	         public void onClick(DialogInterface dialog, int which) {
	        	 dialog.dismiss();
	         }
	      });
	      
	      AlertDialog alertDialog = alertDialogBuilder.create();
	      alertDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DownloadServiceManager.getSharedDownloadManager().isDowloadingMoview(mVideoSelectedId)) {
			DownloadReceiver downloadReciver  = new DownloadReceiver(new Handler());
			DownloadServiceManager.getSharedDownloadManager().videoDownloadingRecivier.put(mVideoSelectedId, downloadReciver);
			DownloadServiceManager.getSharedDownloadManager().videoDetails.put(mVideoSelectedId, mTitle);
		}
	};
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.Back:
			finish();
			break;
		case R.id.play:
		case R.id.VideoPoster:
			if (AppUtils.isNetworkAvailable(MovieDetailsActivity.this)) {
				if (isVideoExists(mVideoSelectedId)) {
					String path = getCacheDir() + "/" + mVideoSelectedId + ".mp4";
					File file = new File(path);
					Intent in = new Intent(MovieDetailsActivity.this, DownloadVideoPlayer.class);
					in.putExtra(DownloadVideoPlayer.EXTRA_URL, file.getAbsolutePath());
					startActivity(in);
				} else {
					Intent mpdIntent = new Intent(this, PlayerActivity.class)
					        .setData(Uri.parse(mVideoUrl))
					        .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, Util.TYPE_HLS);
					    startActivity(mpdIntent);
				}
			}else{
				AppUtils.Show_Dialog("Please Check your internet connectivity",MovieDetailsActivity.this);
			}
			break;
		default:
			break;
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
	
	private class DownloadReceiver extends ResultReceiver{
	    public DownloadReceiver(Handler handler) {
	        super(handler);
	    }

	    @Override
	    protected void onReceiveResult(int resultCode, Bundle resultData) {
	        super.onReceiveResult(resultCode, resultData);

	        if (resultCode == DownloadService.UPDATE_PROGRESS) {

	            int progress = resultData.getInt("progress");

	            Log.e(TAG, "Downloading: " + "MovieName : " + resultData.getString("movieName")
						+ "MovieID : " + resultData.getString("movieId")
						+ "Progress : " + progress);

				if (progressBarDownload != null) {

	            	downloadMoview.setText("Laster ned...");
	            	downloadMoview.setEnabled(false);
		            progressBarDownload.setVisibility(View.VISIBLE);
		            progressBarDownload.setProgress(progress);

		            if (progress == 100) {

		            	progressBarDownload.setVisibility(View.GONE);
		            	downloadMoview.setText("Nedlastet");
		            	downloadMoview.setEnabled(true);
		            	play.setEnabled(true);
		            	videoPoster.setEnabled(true);

		                Calendar c = Calendar.getInstance();
		                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		                String dateTimeStart = df.format(c.getTime());
		                String movieId = resultData.getString("movieId");
		                String movieName = resultData.getString("movieName");
		                String movie_details = dateTimeStart + ";" + movieId + ";" + movieName ;
		                Log.e(TAG, "Movie Details: " + movie_details);

		            	sharedPreferenceEditor.putString(Constant.MOVIE_DETAILS + ";" + movieId, movie_details);
		            	sharedPreferenceEditor.commit();
		            	DownloadServiceManager.getSharedDownloadManager().videoDownloadingRecivier.remove(mVideoSelectedId);
		            	DownloadServiceManager.getSharedDownloadManager().videoDetails.remove(mVideoSelectedId);
		            	DownloadServiceManager.getSharedDownloadManager().videoDownloading.remove(mVideoSelectedId);
		            }
				}
				
	        }else if(resultCode == DownloadService.CANCEL_DOWNLOAD){
	        	
	        	String movieId = resultData.getString("movieId");
	        	DownloadServiceManager.getSharedDownloadManager().videoDownloadingRecivier.remove(movieId);
            	DownloadServiceManager.getSharedDownloadManager().videoDetails.remove(movieId);
            	DownloadServiceManager.getSharedDownloadManager().videoDownloading.remove(movieId);

            	downloadMoview.setText("Last Ned Filmen");
            	downloadMoview.setEnabled(true);
            	play.setEnabled(true);
            	videoPoster.setEnabled(true);
            	
	        }
	    }
	}

	/**
	 * Checks if the file exists.
	 * @param videoID file name
	 * @return
     */
	private boolean isVideoExists(String videoID){

		String path = getCacheDir() + "/" + videoID + ".mp4";
		File file = new File ( path ); 
		if ( file.exists() && sharedpreferences.contains(Constant.MOVIE_DETAILS + ";" + videoID)) {
			return true;
		}
		return false;
	}
}
