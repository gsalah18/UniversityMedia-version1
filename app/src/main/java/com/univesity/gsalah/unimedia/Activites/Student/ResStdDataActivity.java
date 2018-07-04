package com.univesity.gsalah.unimedia.Activites.Student;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResStdDataActivity extends AppCompatActivity implements ListView.OnItemClickListener{
    private String SessionId;
    private ServerDB database;
    private ArrayList<String>Data=new ArrayList<>();
    private ListView listView;
    private Context context=this;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_res_std_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        database=new ServerDB(this);
        SessionId=getIntent().getExtras().getString("session_id");
        setTitle(getIntent().getExtras().getString("course_name"));
        listView=(ListView)findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                InitRes();
            }
        });
        InitRes();
    }

    private void InitRes() {
        final String query="SELECT re_name FROM resources WHERE session_id='"+SessionId+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1 = new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listView.setAdapter(new ArrayAdapter<String>(context,R.layout.temp_txt_layout,Data));
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String name=adapterView.getItemAtPosition(i).toString();
        final String query="SELECT re_link FROM resources WHERE re_name='"+name+"' AND session_id='"+SessionId+"'";
        StringRequest request=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String Link;
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2=arr1.getJSONArray(0);
                    Link=arr2.getJSONObject(0).getString("col0");
                    Uri uri=Uri.parse(Link);
                    Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(getResources().getString(R.string.server_error));
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>map=new HashMap<>();
                map.put("query",query);
                return map;
            }
        };
        Singlton.getInstance(this).AddtoRequest(request);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
