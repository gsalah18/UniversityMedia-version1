package com.univesity.gsalah.unimedia.Databases;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ServerDB {
    //public static String server="http://160.78.239.203/Android";
    Context context;
    public ServerDB(Context context) {
        this.context = context;
    }

    public  void MSG(String str){
        AlertDialog.Builder adb=new AlertDialog.Builder(context);
        adb.setTitle(context.getResources().getString(R.string.caution));
        adb.setMessage(str);
        adb.setNeutralButton(context.getResources().getString(R.string.ok),null);
        adb.show();
    }
    public String FirebaseName(String str){
        String s="";
        for (int i = 0; i <str.length() ; i++) {
            if(!(str.charAt(i)=='.'||str.charAt(i)=='#'||str.charAt(i)=='$'||str.charAt(i)=='['||str.charAt(i)==']'))
                s+=str.charAt(i);
        }
        return s;
    }
}
