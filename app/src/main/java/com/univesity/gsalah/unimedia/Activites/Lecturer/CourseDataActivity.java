package com.univesity.gsalah.unimedia.Activites.Lecturer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Adapters.Courses_Adapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Models.Std_Class;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CourseDataActivity extends AppCompatActivity {

    private ServerDB database;
    private String SessionId;
    private LinkedList<Std_Class>Data=new LinkedList<>();
    private ListView list;
    private Context context=this;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database=new ServerDB(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i=getIntent();
        String course=i.getExtras().getString("course");
        SessionId=i.getExtras().getString("session_id");
        this.setTitle(course);
        list=(ListView)findViewById(R.id.listview);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                InitSTD();
            }
        });
        InitSTD();

    }

    private void InitSTD() {
            final String query="SELECT s.std_id, concat(s.std_fname, ' ', s.std_lname), t.ta " +
                    "FROM student s, time_table t " +
                    "WHERE  t.session_id='"+SessionId+"' and t.std_id=s.std_id";
            StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr.length() ; i++) {
                        arr2=arr.getJSONArray(i);
                        Data.add(new Std_Class(arr2.getJSONObject(0).getString("col0"),arr2.getJSONObject(1).getString("col1"),arr2.getJSONObject(2).getString("col2")));
                    }
                    list.setAdapter(new Courses_Adapter(context, Data,SessionId));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(getResources().getString(R.string.server_error));
                swipeRefreshLayout.setRefreshing(false);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
