package com.sample.vidance.app;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Danil on 27.03.2017.
 * Collaboration by Michelle on 01.05.2017.
 */

public class AppController extends Application {
    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private static AppController mInstance;
    private String username;
    private String childname;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public String getUser() { //Retrieve current user
        return username;
    }

    public void setUser(String username) { //Set current user
        this.username = username;
    }

    public String getChild() { //Retrieve current child
        return childname;
    }

    public void setChild(String childname) { //Set current child
        this.childname = childname;
    }

    public String getChildID() { //Retrieve current child
        return childname;
    }

    public void setChildID(String childname) { //Set current child
        this.childname = childname;
    }
}
