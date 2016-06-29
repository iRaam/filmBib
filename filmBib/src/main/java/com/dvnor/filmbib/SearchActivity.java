package com.dvnor.filmbib;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.dvnor.Asynctask.HttpHandler;
import com.dvnor.utils.AppUtils;
import com.dvnor.utils.BaseClass;
import com.dvnor.utils.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnClickListener {

	private final String					TAG = "SearchActivity";
	public  ArrayList<String> 				PlayListNames = new ArrayList<String>();
	public  HashMap<String,String> 			PlayList_Map = new HashMap<String,String>();
	
	private EditText 						searchbar;
	private ImageView 						btn_Search;
	private ImageButton 					back;
	private ListView 						mListView;
	private ProgressBar 					mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_screen);
		initViews();
	}
	private void initViews() {
	
		searchbar = (EditText)findViewById(R.id.searchEdit);
		View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
		searchbar.setOnFocusChangeListener(ofcListener);
		searchbar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
	                    performSearch(v.getText().toString());
	                    return true;
	                }
	                return false;
	            }

			
	        });
		btn_Search = (ImageView)findViewById(R.id.search);
		btn_Search.setOnClickListener(this);
		back = (ImageButton)findViewById(R.id.Back);
		back.setOnClickListener(this);
		mListView = (ListView)findViewById(R.id.searchList);
		mProgressBar=(ProgressBar)findViewById(R.id.progressBar1);

	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.Back:
			finish();
			break;
		case R.id.search:
			 performSearch(searchbar.getText().toString());
			break;

		default:
			break;
		}
	
	}
	private void performSearch(final String string) {

		if(searchbar.getText().toString().length()!=0){
			
			hideKeyboard();
			mProgressBar.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.VISIBLE);
			findViewById(R.id.noDataFound).setVisibility(View.GONE);
			PlayListNames.clear();
			PlayList_Map.clear();
			new HttpHandler() {
				@Override
				public HttpUriRequest getHttpRequestMethod() {
					HttpGet httpget = null;
					if (AppUtils.isNetworkAvailable(SearchActivity.this) == true) {
							httpget = new HttpGet(BaseClass.MOVIES_SEARCH_URL+string.replace(" ", "%20"));
							httpget.setHeader("X-Authorization", BaseClass.AUTH_ID);
						return httpget;
					} else {
						AppUtils.Show_Dialog("Please Check your internet connectivity",SearchActivity.this);
						mProgressBar.setVisibility(View.VISIBLE);
						return httpget;
					}
				}

				@Override
				public void onResponse(String result) {
					// what to do with result
					Log.e(TAG, "Searched Moview DATA: " + result);
					if (result != null) {
						try {
							JSONObject mainObject = new JSONObject(result);
							JSONArray jArray=mainObject.getJSONArray(Constant.MOVIE_ROOT_KEY);
							for(int i=0; i<jArray.length(); i++)
							{
								JSONObject jobj=jArray.getJSONObject(i);
								PlayListNames.add(jobj.getString(Constant.VIDEO_TITLE));
								PlayList_Map.put(jobj.getString(Constant.VIDEO_TITLE), jobj.toString());
								
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
						updateUI();
					
					}
				}

			}.execute(getApplicationContext());
		} else
		{
			Toast.makeText(this,"Please write a search entry",Toast.LENGTH_SHORT).show();
		}
	}
	
	private void updateUI() {

		mProgressBar.setVisibility(View.GONE);

		VideoListAdapter mAdapter = new VideoListAdapter();
		mListView.setAdapter(mAdapter);
		if (PlayList_Map.size() == 0) {
			findViewById(R.id.noDataFound).setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}
	}

	public class VideoListAdapter extends BaseAdapter {

		Typeface tf;

		public VideoListAdapter() {
		}

		@Override
		public int getCount() {
			return PlayList_Map.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, null);

			TextView title = (TextView) retval.findViewById(R.id.Title);
			tf = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
			title.setTextColor(Color.WHITE);
			title.setTypeface(tf, Typeface.NORMAL);
			final ImageView image = (ImageView) retval.findViewById(R.id.movie_icon);
			JSONObject videoObject = null;
			try {
				videoObject = new JSONObject(PlayList_Map.get(PlayListNames.get(position)));
				title.setText(videoObject.getString(Constant.VIDEO_TITLE));
				retval.setTag(Integer.parseInt(videoObject.getString(Constant.VIDEO_ID)));
				Glide.with(SearchActivity.this)
						.load(videoObject.getJSONObject("image")
						.getString("small"))
						.transform(new CircleTransform(SearchActivity.this))
						.placeholder(R.drawable.app_logo).crossFade().into(image);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			retval.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent playVideo = new Intent(SearchActivity.this, MovieDetailsActivity.class);
					playVideo.putExtra(Constant.CLICKED_MOVIE_ID_KEY, v.getTag().toString());
					playVideo.putExtra(Constant.ACTIVITY_NAME, "SearchActivity");
					startActivity(playVideo);

				}
			});
			return retval;
		}

	}

	public  class CircleTransform extends BitmapTransformation {
		public CircleTransform(Context context) {
			super(context);
		}

		@Override
		protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
			return circleCrop(pool, toTransform);
		}

		private  Bitmap circleCrop(BitmapPool pool, Bitmap source) {
			if (source == null) return null;

			int size = Math.min(source.getWidth(), source.getHeight());
			int x = (source.getWidth() - size) / 2;
			int y = (source.getHeight() - size) / 2;

			// TODO this could be acquired from the pool too
			Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

			Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
			if (result == null) {
				result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
			}

			Canvas canvas = new Canvas(result);
			Paint paint = new Paint();
			paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
			paint.setAntiAlias(true);
			float r = size / 2f;
			canvas.drawCircle(r, r, r, paint);
			return result;
		}

		@Override
		public String getId() {
			return getClass().getName();
		}
	}

	private class MyFocusChangeListener implements View.OnFocusChangeListener {

		public void onFocusChange(View v, boolean hasFocus) {

			if (v.getId() == R.id.searchEdit && !hasFocus) {
				hideKeyboard();
			}
		}
	}

	/**
	 * Hides the keyboad
	 */
	private void hideKeyboard() {

		if (getCurrentFocus() != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}
}
