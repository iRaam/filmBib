package com.dvnor.Asynctask;

import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;

public abstract class HttpHandler {

    public abstract HttpUriRequest getHttpRequestMethod();

    public abstract void onResponse(String result);

    public void execute(Context ctx){
        new GetAllVideoData(this,ctx).execute();
    } 
}