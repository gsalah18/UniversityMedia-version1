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
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Lecturer.AskDataActivity;
import com.univesity.gsalah.unimedia.Activites.Lecturer.LecturerActivity;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AskLec extends Fragment implements ListView.OnItemClickListener{
    private View view;
    private ListView listView;
    private ServerDB database;
    private ArrayList<String> Data = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(view==null)
            view=inflater.inflate(R.layout.fragment_ask_lec, container, false);
        database = new ServerDB(getActivity());
        listView =  view.findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        swipeRefreshLayout=view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                RefreshData();
            }
        });
        RefreshData();
        return view;
    }

    private void RefreshData() {
        Data.clear();
        final String query = "SELECT concat(c.cr_name,' ',s.session_time)" +
                " FROM sessions s, courses c" +
                " WHERE c.cr_id=s.cr_id AND s.lec_id='" + LecturerActivity.Id + "'";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1 = new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i < arr1.length(); i++) {
                        arr2 = arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0"));
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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("query", query);
                return map;
            }
        };
        Singlton.getInstance(getActivity()).AddtoRequest(stringRequest);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String SessionName=((TextView)view).getText().toString();
        Intent intent=new Intent(getActivity(), AskDataActivity.class);
        intent.putExtra("session_name", database.FirebaseName(SessionName));
        startActivity(intent);
    }
}
