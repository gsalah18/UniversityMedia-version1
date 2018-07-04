package com.univesity.gsalah.unimedia.Activites.Lecturer;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SolutionActivity extends AppCompatActivity implements ListView.OnItemClickListener{

    private ListView listview;
    private String AssignmentId;
    private ArrayList<String>Data=new ArrayList<>();
    private ArrayList<String>Links=new ArrayList<>();
    private ServerDB database;
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Context context=this;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ///////////////////////////////////////////////////////////////////
        database=new ServerDB(this);
        AssignmentId=getIntent().getExtras().getString("ass_id");
        String ass_name=getIntent().getExtras().getString("ass_name");
        setTitle(ass_name+" Solutions");
        listview=(ListView)findViewById(R.id.listview);
        listview.setOnItemClickListener(this);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Refresh();
            }
        });
        Refresh();
    }

    private void Refresh() {
        Data.clear();
        Links.clear();
        final String query="SELECT concat(s.std_fname,' ',s.std_lname),so.solution_date,so.solution_link FROM stdsolution so,student s" +
                " WHERE s.std_id=so.std_id AND so.ass_id='"+AssignmentId+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i < arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0")+"\n"
                                +fmt.format(fmt.parse(arr2.getJSONObject(1).getString("col1"))));
                        Links.add(arr2.getJSONObject(2).getString("col2"));
                    }
                    listview.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,Data));

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    database.MSG(e+"");
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Uri uri=Uri.parse(Links.get(i));
        Intent intent=new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }
}
