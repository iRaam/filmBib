package com.dvnor.Asynctask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class GetAllVideoData extends AsyncTask<String, String, String> {
	
	
	//private Context mContext;
	 private HttpHandler httpHandler;
	    public GetAllVideoData(HttpHandler httpHandler, Context ctx){
	        this.httpHandler = httpHandler;
	    }

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
  @Override
  protected String doInBackground(String... params)
  
  {	  InputStream inputStream = null;
      String result = "";
      try {
    	  HttpClient client = HttpClientFactory.getThreadSafeClient();
          HttpResponse httpResponse = client.execute(httpHandler.getHttpRequestMethod());
          inputStream = httpResponse.getEntity().getContent();
          if(inputStream != null)
              result = convertInputStreamToString(inputStream);
          else
              result =null;

      } catch (Exception e) {
          Log.d("InputStream", e.getLocalizedMessage());
      }


	  return result;
  }
 
  @Override
  protected void onPostExecute(String resultString) {
	  
	 if(resultString!=null)
	  {
		 httpHandler.onResponse(resultString);
	  }
	 else
	  {
		  //AppUtils.Show_Dialog("Error while fetching data.Please check internet connection!",(Activity) mContext);
	  }
	  
	  
}
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
      BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
      String line = "";
      String result = "";
      while((line = bufferedReader.readLine()) != null)
          result += line;

      inputStream.close();
      return result;   
  }  
}
