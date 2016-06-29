package com.dvnor.Asynctask;

import java.util.HashMap;

import android.content.Intent;
import android.os.ResultReceiver;

public class DownloadServiceManager {
	
	
	public HashMap<String,Intent> videoDownloading;
    public HashMap<String,ResultReceiver> videoDownloadingRecivier;
    public HashMap<String,String> videoDetails;
	static DownloadServiceManager sharedDownloadManager= null;
	
	public static DownloadServiceManager getSharedDownloadManager(){
		if(sharedDownloadManager == null){
			sharedDownloadManager = new DownloadServiceManager();
			sharedDownloadManager.videoDownloading = new HashMap<>();
			sharedDownloadManager.videoDownloadingRecivier = new HashMap<>();
			sharedDownloadManager.videoDetails = new HashMap<>();
		}
		return sharedDownloadManager;
	}
	
	public void addNewMovieToDownload(Intent intent,String movieId){
		sharedDownloadManager.videoDownloading.put(movieId, intent);
	}
	public void addDownloadingMovieName(String movieId, String movieName){
		sharedDownloadManager.videoDetails.put(movieId, movieName);
	}
	public String getDownloadedMovieName(String movieId){
		if (sharedDownloadManager.videoDetails.containsKey(movieId)) {
			return sharedDownloadManager.videoDetails.get(movieId);
		}
		return "";
	}
	public Boolean isDowloadingMoview(String movieId){
		if (sharedDownloadManager.videoDownloading.containsKey(movieId)) {
			return true;
		}
		return false;
	}
		

}
