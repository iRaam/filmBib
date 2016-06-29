package com.dvnor.Asynctask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.dvnor.utils.BaseClass;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    public static final int CANCEL_DOWNLOAD = 8345;
    public DownloadService() {
        super("DownloadService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra("url");
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        String movieId = intent.getStringExtra("movieId");
        String mTitle = intent.getStringExtra("movieName");
        Log.e("Title1", ""+mTitle);
        try {
       	 URL url = new URL(urlToDownload);
       	 URLConnection  c =  url.openConnection();
            ((HttpURLConnection) c).setRequestMethod("GET");
            c.setRequestProperty("Content-Type", "application/octet-stream");
            c.addRequestProperty ("X-Authorization", BaseClass.AUTH_ID);
           // c.setDoOutput(true);
            c.connect();

//            String PATH = Environment.getExternalStorageDirectory()
//                    + "/download/Ram";
            String fileName = movieId+".mp4";
            File outputFile = new File(getCacheDir(), fileName);
            Log.v("DOWNLOAD PATH", "PATH: " + outputFile.getAbsolutePath());
            int httpStatus = ((HttpURLConnection) c).getResponseCode();
            Log.v("STATUS", "httpStatus " + httpStatus+" : "+((HttpURLConnection) c).getResponseMessage());

             int lenghtOfFile = c.getContentLength();
            FileOutputStream output = new FileOutputStream(outputFile);

            // input stream to read file - with 8k buffer
            InputStream input = c.getInputStream();
            byte data[] = new byte[1024];
            
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                Bundle resultData = new Bundle();
                resultData.putInt("progress" ,(int) (total * 100 / lenghtOfFile));
                receiver = DownloadServiceManager.getSharedDownloadManager().videoDownloadingRecivier.get(movieId);
//                System.out.println("receiver =  "+ receiver + "receiver1 =  "+ receiver1 );
                resultData.putString("movieId", movieId);
                resultData.putString("movieName", mTitle);
                receiver.send(UPDATE_PROGRESS, resultData);
            	
                // writing data to file
                output.write(data, 0, count);
            }
 
            // flushing output
            output.flush();
 
            // closing streams
            output.close();
            input.close();

       } catch (Exception e) {
       	Log.e("Error: ", e.getMessage());
      
       	Bundle resultData = new Bundle();
        receiver = DownloadServiceManager.getSharedDownloadManager().videoDownloadingRecivier.get(movieId);
        resultData.putString("movieId", movieId);
        intent.putExtra("movieName", mTitle);
        receiver.send(CANCEL_DOWNLOAD, resultData);
       	
       }

       /* receiver = DownloadServiceManager.getSharedDownloadManager().videoDownloadingRecivier.get(movieId);
        Bundle resultData = new Bundle();
        resultData.putInt("progress" ,100);
        System.out.println("movieId =  "+ movieId );
        resultData.putString("movieId", movieId);
        receiver.send(UPDATE_PROGRESS, resultData);*/
        
    }
}