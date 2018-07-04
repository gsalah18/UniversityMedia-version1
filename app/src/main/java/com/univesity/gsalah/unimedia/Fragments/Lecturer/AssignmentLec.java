package com.univesity.gsalah.unimedia.Fragments.Lecturer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Lecturer.AssignmentLecActivity;
import com.univesity.gsalah.unimedia.Activites.Lecturer.LecturerActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AssignmentLec extends Fragment implements ListView.OnItemClickListener{
    private View view;
    private ServerDB database;
    private ArrayList<String>Data=new ArrayList<>();
    private ArrayList<String>Ids=new ArrayList<>();
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view= inflater.inflate(R.layout.fragment_assignment, container, false);
        database=new ServerDB(getActivity());
        listView=(ListView)view.findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Refresh();
            }
        });
        Refresh();
        return view;
    }

    private void Refresh() {
        Ids.clear();
        Data.clear();
        final String query="SELECT s.session_id,concat(c.cr_name,' ',s.session_time) FROM sessions s,courses c" +
                " WHERE c.cr_id=s.cr_id AND lec_id='"+ LecturerActivity.Id+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        Ids.add(arr2.getJSONObject(0).getString("col0"));
                        Data.add(arr2.getJSONObject(1).getString("col1"));
                    }
                    listView.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.temp_txt_layout,Data));

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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent=new Intent(getActivity(), AssignmentLecActivity.class);
        intent.putExtra("session_id",Ids.get(i));
        intent.putExtra("session_title",Data.get(i));
        startActivity(intent);
    }
}
