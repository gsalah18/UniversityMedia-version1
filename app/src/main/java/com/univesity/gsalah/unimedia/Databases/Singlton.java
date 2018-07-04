package com.univesity.gsalah.unimedia.Databases;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class Singlton {
    private static Singlton Instance;
    private RequestQueue queue;
    private Context context;

    public Singlton(Context context) {
        this.context = context;
        queue=getRequestQueue();
    }
    public RequestQueue getRequestQueue(){
        if(queue==null){
            queue= Volley.newRequestQueue(context);
        }
        return queue;
    }
    public static synchronized Singlton getInstance(Context context){
        if(Instance==null)
            Instance=new Singlton(context);
        return Instance;
    }
    public void AddtoRequest(Request request){
        queue.add(request);
    }
}
