package com.dvnor.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AppUtils {
	
	public static String TYPE_WIFI = "wifi";
	public static String TYPE_MOBILE = "mobile";

	public static boolean isNetworkAvailable(Activity activity) {
		ConnectivityManager connectivity = (ConnectivityManager) activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static void Show_Dialog(String message,Activity act) {

		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setCancelable(true);
		builder.setTitle(message);
		builder.setInverseBackgroundForced(true);

		builder.setNeutralButton("Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,int which) {
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();		
	}
	
	public static long totalSecondBetween2Date(String beginDate,
			String endDate, String formatDate) {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(formatDate,Locale.ENGLISH);
			Date beginDateObj = sdf.parse(beginDate);
			Date endDateObj = sdf.parse(endDate);
			return (endDateObj.getTime() - beginDateObj.getTime()) / 1000;

		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}

	}
	
	public static boolean isEmailValid(String email) {
		boolean isValid = false;

		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;

		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	public static String getInternetType (Context mContext) {
		
		ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = connManager .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		String type = "";
		        if (wifi.isConnected()){         
		            //if wifi connected
		        	type = TYPE_WIFI;
		        }

		        if (mobile.isConnected()) {
		            //if internet connected
		        	type = TYPE_MOBILE;
		        }
		return type;
	}

	/**
	 * Checks if downloaded time exceeds 48 hours.
	 * @param downloadTime
	 * @return
	 */
	public static boolean isDownloadedTimeExceeds(String downloadTime){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String currentTime = df.format(c.getTime());

		//HH converts hour in 24 hours format (0-23), day calculation
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		Date d1 = null;
		Date d2 = null;

		try {
			d1 = format.parse(downloadTime);
			d2 = format.parse(currentTime);

			//in milliseconds
			long diff = d2.getTime() - d1.getTime();

			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);

			System.out.print(diffDays + " days, ");
			System.out.print(diffHours + " hours, ");
			System.out.print(diffMinutes + " minutes, ");
			System.out.print(diffSeconds + " seconds.");

			Log.e("Time Difference : ", "DAY: " + diffDays + " : Hours : " + diffHours
					+ " : minutes : " + diffMinutes);
			if (diffDays > 1) {
				return true;
			} else if (diffHours > 48) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}
