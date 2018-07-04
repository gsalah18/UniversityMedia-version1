package com.univesity.gsalah.unimedia.Activites;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends Activity {
    private ServerDB database;
    private ArrayList<String>SessionNames=new ArrayList<>();
    private ArrayList<String>SessionIds=new ArrayList<>();
    private ArrayList<String>StudentIds=new ArrayList<>();
    private HashMap<String,ArrayList<String>>InSession=new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        database=new ServerDB(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this,LoginActivity.class));
            }
        },5000);
    }
    private void BringSession(){
        final String query="SELECT s.session_id,concat(c.cr_name,' ',s.session_days) FROM sessions,courses" +
                " WHERE s.cr_id=c.cr_id";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        SessionIds.add(arr2.getJSONObject(0).getString("col0"));
                        SessionNames.add(arr2.getJSONObject(1).getString("col1"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(error+"");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>map=new HashMap<>();
                map.put("query",query);
                return map;
            }
        };
        Singlton.getInstance(this).AddtoRequest(stringRequest);
    }
    private int index;
    private void BringStudent(){
        for (index = 0; index <SessionIds.size() ; index++) {
            SessionIds.clear();
            final String query="SELECT std_id FROM time_table WHERE session_id='"+SessionIds.get(index)+"'";
            StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "dbQuery.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray arr1=new JSONArray(response);
                        JSONArray arr2;
                        for (int j = 0; j <arr1.length() ; j++) {
                            arr2=arr1.getJSONArray(j);
                            StudentIds.add(arr2.getJSONObject(0).getString("col0"));
                        }
                        InSession.put(SessionIds.get(index),SessionIds);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String>map=new HashMap<>();
                    map.put("query",query);
                    return map;
                }
            };
            Singlton.getInstance(this).AddtoRequest(stringRequest);
        }
    }
}
