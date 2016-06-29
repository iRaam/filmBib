package com.dvnor.filmbib;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dvnor.utils.AppUtils;
import com.dvnor.utils.Constant;

public class DownloadListActivity extends Activity {

	private final String 						TAG = "DownloadListActivity";
	private SharedPreferences 					sharedpreferences;
	private SharedPreferences.Editor 			editor;
	private ArrayList<String> 					movieName = new ArrayList<String>();
	private ArrayList<String> 					movieDownloadDate = new ArrayList<String>();
	private ArrayList<String> 					movieDownloadTime = new ArrayList<String>();
	private ArrayList<String> 					movieDownloadId = new ArrayList<String>();

	private ListView 							downloadList;
	private ProgressBar 						progressBar;
	private TextView 							noDownloadFound;

	private Timer 								timer;
	private MyTimerTask 						myTimerTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_list_screen);

		downloadList = (ListView)findViewById(R.id.downloadList);
		progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		noDownloadFound = (TextView)findViewById(R.id.noDownloadFound);
		ImageView Back = (ImageView)findViewById(R.id.Back);
		Back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();
			}
		});

		sharedpreferences = getSharedPreferences(Constant.MYPREFERENCES, Context.MODE_PRIVATE);
		editor = sharedpreferences.edit();

		setDownloadedList();

		if(timer != null){
			timer.cancel();
		}

		timer = new Timer();
		myTimerTask = new MyTimerTask();
		timer.schedule(myTimerTask, 60000, 60000);
	}

	private void clearList()
	{
		movieName.clear();
		movieDownloadDate.clear();
		movieDownloadTime.clear();
		movieDownloadId.clear();

	}

	/**
	 * Inflates the list based on downlaoded media.
	 */
	private void setDownloadedList() {

		Map<String,?> keys = sharedpreferences.getAll();

		for(Map.Entry<String,?> entry : keys.entrySet()){
		  Log.d("map values",entry.getKey() + ": " +  entry.getValue().toString());
		  if(entry.getKey().contains(Constant.MOVIE_DETAILS)){
			  String value = entry.getValue().toString();
			  String[] data = value.split(";");
			  Log.d(TAG, "isDownloadedTimeExceeds: "+ AppUtils.isDownloadedTimeExceeds(data[0]));
			  if(!AppUtils.isDownloadedTimeExceeds(data[0])){
				  movieName.add(data[2]);
				  movieDownloadDate.add(data[0].split(" ")[0]);
				  movieDownloadTime.add(downloadedElapseTime(data[0]));
				  movieDownloadId.add(data[1]);
			  }else{
				  deleteFileIfTimeExceeds(data[1]);
			  }

		  }
		 }
		if(movieName.size()>0){
			noDownloadFound.setVisibility(View.GONE);
			downloadList.setVisibility(View.VISIBLE);
			DownloadListCustomAdapter adapter=new DownloadListCustomAdapter();
			downloadList.setAdapter(adapter);
		}else {
			noDownloadFound.setVisibility(View.VISIBLE);
			downloadList.setVisibility(View.GONE);
		}
	}

	/**
	 * calculates the downloaded elapsed time.
	 * @param downloadTime
	 * @return
     */
	private String downloadedElapseTime(String downloadTime){
		String elapseTime = "";
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		Calendar deleteCal = Calendar.getInstance();
        try {
			Date date = format.parse(downloadTime);
			deleteCal.setTime(date);
			deleteCal.add(Calendar.HOUR, 48);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        String deleteTime = format.format(deleteCal.getTime());
        Calendar currentCal = Calendar.getInstance();
        String currentTime = format.format(currentCal.getTime());
        Log.d(TAG, "DELETE TIME : " + format.format(deleteCal.getTime()));
        Log.d(TAG, "CURRENT TIME : " + format.format(currentCal.getTime()));

		Date d1 = null;
		Date d2 = null;

		try {
			d1 = format.parse(currentTime);
			d2 = format.parse(deleteTime);

			//in milliseconds
			long diff = d2.getTime() - d1.getTime();

			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);

			Log.e(TAG, "Time Difference : DAY: " + diffDays + " : Hours : " + diffHours
					+ " : minutes : " + diffMinutes);
			if (diffDays > 0) {
				diffHours = diffHours + 24 * diffDays;
			}

			elapseTime = diffHours + " time " + diffMinutes + " minutter";

			Log.d(TAG, "ELAPSED TIME : " + elapseTime);

		} catch (Exception e) {
			e.printStackTrace();

		}

		return elapseTime;
	}

	/**
	 * deletes the file if time exceeds
	 * @param videoID
     */
	private void deleteFileIfTimeExceeds(String videoID) {

		String path=getCacheDir()+"/"+videoID+".mp4";
		File file = new File ( path );
		Log.e(TAG, "Video To Be Deleted: File Exists: " + file.exists()+ " : " + file.getAbsolutePath());
		if ( file.exists() )
		{
			file.delete();
			editor.remove(Constant.MOVIE_DETAILS+";"+videoID);
			editor.apply();
		}
	}

	/**
	 * Adapter to inflate the downloded media list
	 */
	public class DownloadListCustomAdapter extends BaseAdapter {
	    LayoutInflater inflater;

	       public DownloadListCustomAdapter() {
	        inflater = LayoutInflater.from(DownloadListActivity.this);
	    }
	    public class ViewHolder {
	        TextView MovieTitle,downloadDate,downloadTime;
	        ImageView play,delete;
	    }

	    @Override
	    public int getCount() {
	        return movieName.size();
	    }

	    @Override
	    public Object getItem(int position) {
	        return 0;
	    }

	    @Override
	    public long getItemId(int position) {
	        return position;
	    }

	    @Override
		public View getView(final int position, View view, ViewGroup parent) {
	        final ViewHolder holder;
	        if (view == null) {
	            holder = new ViewHolder();
	            view = inflater.inflate(R.layout.downloaded_list_item, parent, false);
	            holder.MovieTitle = (TextView) view.findViewById(R.id.MovieTitle);
	            holder.downloadDate= (TextView) view.findViewById(R.id.downloadDate);
	            holder.downloadTime= (TextView) view.findViewById(R.id.downloadTime);
	            holder.play = (ImageView) view.findViewById(R.id.play);
	            holder.delete = (ImageView) view.findViewById(R.id.delete);

	            view.setTag(holder);
	        } else {
	            holder = (ViewHolder) view.getTag();
	        }

	        holder.MovieTitle.setText(movieName.get(position));
	        holder.downloadDate.setText("Nedlastet: "+movieDownloadDate.get(position));
	        holder.downloadTime.setText("Timer igjen: "+movieDownloadTime.get(position));

	        holder.play.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String path=getCacheDir()+"/"+movieDownloadId.get(position)+".mp4";
					File file = new File ( path );
					Log.e( TAG, "Video Url: File Exists: " + file.exists() + " : " + file.getAbsolutePath());
					if ( file.exists() )
					{
						Intent in=new Intent(DownloadListActivity.this,DownloadVideoPlayer.class);
						in.putExtra(DownloadVideoPlayer.EXTRA_URL, file.getAbsolutePath());
						startActivity(in);
					}
				}
			});

	        holder.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DownloadListActivity.this);
					alertDialogBuilder.setMessage("Du er i ferd med å slette en nedlastet fil, klikk Ok for å fortsette.");

				      alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				         @Override
				         public void onClick(DialogInterface arg0, int arg1) {
				        	deleteFile(position);
				         }

				      });

				      alertDialogBuilder.setNegativeButton("AVBRYT",new DialogInterface.OnClickListener() {
				         @Override
				         public void onClick(DialogInterface dialog, int which) {
				        	 dialog.dismiss();
				         }
				      });

				      AlertDialog alertDialog = alertDialogBuilder.create();
				      alertDialog.show();

				}
			});
	        return view;
	    }

	}

	/**
	 * Deletes the file
	 * @param position
     */
	private void deleteFile(int position) {

		 String path=getCacheDir()+"/"+movieDownloadId.get(position)+".mp4";
			File file = new File ( path );
			Log.e("Video To Be Deleted: ", "File Exists: "+file.exists()+ " : "+file.getAbsolutePath());
			if ( file.exists() )
			{
				file.delete();
				editor.remove(Constant.MOVIE_DETAILS+";"+movieDownloadId.get(position));
				editor.apply();
				clearList();
				setDownloadedList();
			}
	}

	class MyTimerTask extends TimerTask {

		  @Override
		  public void run() {
		   runOnUiThread(new Runnable(){

		    @Override
		    public void run() {
		    	clearList();
		    	setDownloadedList();
		    }});
		  }

		 }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer!=null){
		     timer.cancel();
		     timer = null;
		    }
	}
}
