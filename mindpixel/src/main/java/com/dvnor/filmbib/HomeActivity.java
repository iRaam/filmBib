package com.dvnor.filmbib;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dvnor.Asynctask.HttpHandler;
import com.dvnor.utils.BaseClass;
import com.dvnor.utils.Constant;
import com.dvnor.utils.AppUtils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Raam Kumar
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

	private final String 					TAG = "HomeActivity";
	private static ArrayList<String> 		movieArrayList =  new ArrayList<String>();
	private static HashMap<String,String> 	movieHashmap = new HashMap<String,String>();

	private LinearLayout 					listLinear;
	private ProgressBar 					myProgressbar;
	private int 							mTextSize;
	private ImageView 						search;
	private ImageView 						showFile;

	private Typeface 						typeFace;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_screener_home);
		initializeAllViews();
		fetchMovieData();
	}

	private void initializeAllViews() {

		typeFace = Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf");

		TextView mTitleTextView = (TextView) findViewById(R.id.title_text);
		mTitleTextView.setTypeface(typeFace, Typeface.NORMAL);
		mTitleTextView.setText("Filmbib");

		showFile = (ImageView) findViewById(R.id.fileIcon);
		showFile.setVisibility(View.VISIBLE);
		showFile.setOnClickListener(this);

		search = (ImageView) findViewById(R.id.search);
		search.setVisibility(View.VISIBLE);
		search.setOnClickListener(this);
		findViewById(R.id.about).setOnClickListener(this);

		myProgressbar =(ProgressBar)findViewById(R.id.progressBar1);

		final float scale = getResources().getDisplayMetrics().density;
		mTextSize = (int) (5 * scale + 0.5f);
	}

	/**
	 * Fetches movie data from  the server.
	 */
	private void fetchMovieData() {

		myProgressbar.setVisibility(View.VISIBLE);
		movieArrayList.clear();
		if(movieArrayList.size() == 0) {

			new HttpHandler() {
				@Override
				public HttpUriRequest getHttpRequestMethod() {

					HttpGet httpget = null;
					if (AppUtils.isNetworkAvailable(HomeActivity.this) == true) {
						httpget = new HttpGet(BaseClass.MOVIES_URL);
						httpget.setHeader("X-Authorization", BaseClass.AUTH_ID);
						return httpget;
					} else {
						AppUtils.Show_Dialog("Please Check your internet connectivity",HomeActivity.this);
						return httpget;
					}

				}

				@Override
				public void onResponse(String result) {

					Log.i(TAG, "MOVIE DATA: " + result);

					if (result != null) {

						try {

							JSONObject mainObject = new JSONObject(result);
							JSONArray jArray=mainObject.getJSONArray(Constant.MOVIE_ROOT_KEY);
							for(int i=0; i<jArray.length(); i++) {

								JSONObject jobj=jArray.getJSONObject(i);
								movieArrayList.add(jobj.getString(Constant.PLAYLIST_TITLE_NAME_KEY));
								movieHashmap.put(jobj.getString(Constant.PLAYLIST_TITLE_NAME_KEY), jobj.toString());
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

						updateUI();

					}
				}

			}.execute(HomeActivity.this.getApplicationContext());
		} else {
			updateUI();
		}
	}

	/**
	 * Update the UI after getting the data from server
	 */
	protected void updateUI() {

		myProgressbar.setVisibility(View.GONE);
		listLinear = (LinearLayout)findViewById(R.id.ListLinear);
		JSONObject videoObject = null;
		int margin = 20;
		for (int i = 0; i < movieHashmap.size(); i++) {

			TextView Title = new TextView(HomeActivity.this);
			LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			titleLp.setMargins(margin, margin, margin, margin);
			Title.setTypeface(typeFace, Typeface.NORMAL);
			Title.setTextSize(mTextSize);
			Title.setTextColor(Color.WHITE);
			Title.setText(movieArrayList.get(i));

			RecyclerView listView = new RecyclerView(this);
			listView.setBackgroundColor(Color.parseColor("#3b3b3b"));
			LinearLayoutManager layoutManager
					= new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
			listView.setLayoutManager(layoutManager);
			try {
				videoObject = new JSONObject(movieHashmap.get(movieArrayList.get(i)));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			VideoListAdapter mAdapter = new VideoListAdapter(videoObject);
			listView.setAdapter(mAdapter);

			listLinear.addView(Title,titleLp);
			listLinear.addView(listView);

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(movieArrayList.size() == 0) {
			fetchMovieData();
		}
	}

	/**
	 * Adapter to inflate the movie thumbnail and name.
	 */
	public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.MyViewHolder> {

		private final int 			MAX_ONE_LIST_ITEM_SIZE = 10;
		private JSONArray 			jsonArray;

		public class MyViewHolder extends RecyclerView.ViewHolder {

			ImageView image;
			TextView title;

			public MyViewHolder(View convertView) {
				super(convertView);

				title = (TextView) convertView.findViewById(R.id.title);
				title.setTextColor(Color.WHITE);
				title.setTypeface(typeFace, Typeface.NORMAL);
				image = (ImageView) convertView.findViewById(R.id.image);
			}
		}


		public VideoListAdapter(JSONObject videoObject) {

			try {

				JSONObject allMovieObj = videoObject.getJSONObject("movies");
				jsonArray = allMovieObj.getJSONArray("data");

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, parent, false);

			return new MyViewHolder(itemView);
		}

		@Override
		public void onBindViewHolder(MyViewHolder viewHolder, int position) {

			try {

				final JSONObject SingleVideoDetails = jsonArray.getJSONObject(position);

				Glide.with(HomeActivity.this).load(SingleVideoDetails.getJSONObject(Constant.VIDEO_IMAGE).
						getString("small")).placeholder(R.drawable.app_logo).crossFade().into(viewHolder.image);

				viewHolder.title.setText(Html.fromHtml(SingleVideoDetails.getString(Constant.VIDEO_TITLE)));

				viewHolder.image.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent playVideo = new Intent(HomeActivity.this,MovieDetailsActivity.class);
                        try {

                            playVideo.putExtra(Constant.CLICKED_MOVIE_ID_KEY, SingleVideoDetails.getString(Constant.VIDEO_ID));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        playVideo.putExtra(Constant.ACTIVITY_NAME, "HomeActivity");
                        startActivity(playVideo);
                    }
                });
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getItemCount() {
			return MAX_ONE_LIST_ITEM_SIZE;
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.search:
				Intent search = new Intent(this, SearchActivity.class);
				startActivity(search);
				break;
			case R.id.about:
				Intent in = new Intent(this, AboutActivity.class);
				startActivity(in);
				break;
			case R.id.fileIcon:
				Intent fileIntent = new Intent(this, DownloadListActivity.class);
				startActivity(fileIntent);
				break;
			default:
				break;
		}
	}
}
