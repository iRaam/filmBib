package com.dvnor.filmbib;


import java.util.UUID;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import com.dvnor.Asynctask.HttpHandler;
import com.dvnor.launcher.LoginActivity;
import com.dvnor.mediaplayer.VideoBrowserActivity;
import com.dvnor.utils.AppUtils;
import com.dvnor.utils.BaseClass;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;

public class SplashScreen extends Activity {
	private int READ_PHONE_STATE_REQUEST_CODE = 1001;
	ProgressBar MyProgressbar;
	String mDeviceIdUrl;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		
		// Here, thisActivity is the current activity
		if (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

				showExplanationDialog("FilmBib will now request permission which is necessary to register the device.\n\nPlease select \"allow\" on the following prompt. ");
		}else {
			authenticateDevice();
		}
		
		
	}
	
	private void authenticateDevice() {
		mDeviceIdUrl=BaseClass.BASE_URL_DEVICE+getDeviceUniqueId();
		Log.e("mDeviceIdUrl", "mDeviceIdUrl: "+mDeviceIdUrl);
		new HttpHandler() {
			
			@Override
			public HttpUriRequest getHttpRequestMethod() {
				HttpGet httpget = null;
				if (AppUtils.isNetworkAvailable(SplashScreen.this) == true) {
						httpget = new HttpGet(mDeviceIdUrl);
						httpget.setHeader("X-Authorization", BaseClass.AUTH_ID);
					return httpget;
				} else {
					AppUtils.Show_Dialog("Please Check your internet connectivity",SplashScreen.this);
					return httpget;
				}
			}
			
			@Override
			public void onResponse(String result) {
				// what to do with result
				Log.e("DATA: ", "Data: "+result);
				if (result != null && !result.equals("")) {
					try {
						JSONObject obj = new JSONObject(result);
						if (obj.has("error")) {
							JSONObject obj1 = obj.getJSONObject("error");
							if (obj1.getInt("http_code") == 404) {
								Intent login = new Intent(SplashScreen.this, LoginActivity.class);
								startActivity(login);
								finish();
							}

						} else {
//							Intent home = new Intent(SplashScreen.this, VideoBrowserActivity.class);
							Intent home = new Intent(SplashScreen.this, HomeActivity.class);
							startActivity(home);
							finish();
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else {
					AppUtils.Show_Dialog("Please Check your internet connectivity",SplashScreen.this);
				}
			}
			
			
		}.execute(getApplicationContext());
		
		
	}
	private String getDeviceUniqueId() {
		 String deviceId = null;
		try {
			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

			    final String tmDevice, tmSerial, androidId;
			    tmDevice = "" + tm.getDeviceId();
			    tmSerial = "" + tm.getSimSerialNumber();
			    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

			    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
			    deviceId = deviceUuid.toString();
			    //deviceId = "666-";
			    BaseClass.DEVICE_ID=deviceId;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		    return BaseClass.DEVICE_ID;
	}
	
	private void showExplanationDialog(String message) {
		
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		alertBuilder.setCancelable(false);
		alertBuilder.setMessage(message);
		alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				ActivityCompat.requestPermissions(SplashScreen.this,
						new String[] { Manifest.permission.READ_PHONE_STATE }, READ_PHONE_STATE_REQUEST_CODE);
			
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private void showWarningDialog(String message) {
	
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		alertBuilder.setCancelable(false);
		alertBuilder.setMessage(message);
		alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();

			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}
	
	 @Override
	    public void onRequestPermissionsResult(int requestCode, String[] permissions,
	             int[] grantResults) {

	        if (requestCode == READ_PHONE_STATE_REQUEST_CODE) {
	        	
	            // Check if the only required permission has been granted
	            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	            	authenticateDevice();
	            } else {
	            	showWarningDialog("Application can't run without that permission");
	            }
	        }
	    }
}
