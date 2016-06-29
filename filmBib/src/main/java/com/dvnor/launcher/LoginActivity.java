package com.dvnor.launcher;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import com.dvnor.Asynctask.HttpHandler;
import com.dvnor.filmbib.HomeActivity;
import com.dvnor.filmbib.R;
import com.dvnor.utils.BaseClass;
import com.dvnor.utils.AppUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity {
	EditText userEdit,user_psw;
	Button loginButton;
	ProgressBar progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initializeAllviews();
	}
	private void initializeAllviews() {
		progressBar=(ProgressBar)findViewById(R.id.progressBar1);
		userEdit=(EditText)findViewById(R.id.user_name_edit);
		user_psw=(EditText)findViewById(R.id.user_psw_edit);
		loginButton=(Button)findViewById(R.id.logIn);
		loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String emailid = userEdit.getText().toString();
				String password = user_psw.getText().toString();
				String errorMsg = null;
				if (emailid == null || emailid.length() == 0) {
					errorMsg = "Invalid email id";
				} else if (password == null || password.length() == 0) {
					errorMsg = "Enter password";
				}
				if (errorMsg != null) {
					Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG)
							.show();
				} else {
					progressBar.setVisibility(View.VISIBLE);
					sendLoginInfoToServer(emailid,password);
				}				
			}
		});
	}
	protected void sendLoginInfoToServer(final String username,final String psw) {

		new HttpHandler() {
			
			@Override
			public HttpUriRequest getHttpRequestMethod() {
				HttpPost httppost = null;
				if (AppUtils.isNetworkAvailable(LoginActivity.this) == true) {
					String loginUrl=BaseClass.LOGIN_URL+"username="+username+"&password="+psw+"&uuid="+BaseClass.DEVICE_ID;
					Log.e("httpHandlerhttpHandler", "httpHandler: "+loginUrl);
					httppost = new HttpPost(loginUrl);
					httppost.setHeader("X-Authorization", BaseClass.AUTH_ID);
					return httppost;
				} else {
					AppUtils.Show_Dialog("Please Check your internet connectivity",LoginActivity.this);
					return httppost;
				}
			}
			
			@Override
			public void onResponse(String result) {
				// what to do with result
				Log.e("LOGIN DATA: ", "Data: "+result);
				progressBar.setVisibility(View.INVISIBLE);
				if (result != null) {
					try {
						JSONObject obj=new JSONObject(result);
						if(obj.has("error"))
						{
							JSONObject obj1=obj.getJSONObject("error");
							Toast.makeText(LoginActivity.this,obj1.getString("message"),Toast.LENGTH_SHORT).show();
								
						}else
						{
							Intent home=new Intent(LoginActivity.this,HomeActivity.class);
							startActivity(home);
							finish();
						}
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}
			}
			
		}.execute(getApplicationContext());
	}

}
