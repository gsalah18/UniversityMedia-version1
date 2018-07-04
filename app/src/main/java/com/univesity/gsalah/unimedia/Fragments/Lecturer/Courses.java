package com.univesity.gsalah.unimedia.Fragments.Lecturer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Lecturer.CourseDataActivity;
import com.univesity.gsalah.unimedia.Activites.Lecturer.LecturerActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Courses extends Fragment {

    private View v;
    private ServerDB database;
    private ListView list;
    private ArrayList<String>Ids=new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    public View getV() {
        return v;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.fragment_courses, container, false);
        list=(ListView)v.findViewById(R.id.listview);
        database=new ServerDB(getActivity());

        swipeRefreshLayout=(SwipeRefreshLayout)v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                InitCourses();
            }
        });
        InitCourses();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String cr=adapterView.getItemAtPosition(i).toString();
                Intent intent=new Intent(getActivity(),CourseDataActivity.class);
                intent.putExtra("session_id",Ids.get(i));
                intent.putExtra("course",cr);
                startActivity(intent);
            }
        });


        return v;
    }

    private void InitCourses() {
        Ids.clear();
        final String query="SELECT concat(c.cr_name,' ',s.session_time),s.session_id " +
                "FROM sessions s,courses c " +
                "WHERE s.lec_id='"+ LecturerActivity.Id+"' and c.cr_id=s.cr_id";

        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;


                    ArrayList<String>data=new ArrayList<>();
                    for (int i = 0; i < arr1.length(); i++) {
                        arr2 = arr1.getJSONArray(i);
                        data.add(arr2.getJSONObject(0).getString("col0"));
                        Ids.add(arr2.getJSONObject(1).getString("col1"));
                    }
                    list.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.simple_row,R.id.rowTextView,data));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(getActivity().getResources().getString(R.string.server_error));
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
        Singlton.getInstance(getActivity()).AddtoRequest(stringRequest);
    }

}
