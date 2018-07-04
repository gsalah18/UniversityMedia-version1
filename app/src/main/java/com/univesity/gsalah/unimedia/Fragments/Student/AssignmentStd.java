package com.univesity.gsalah.unimedia.Fragments.Student;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.univesity.gsalah.unimedia.Activites.DriveActivity;
import com.univesity.gsalah.unimedia.Activites.Student.AssignmentStdActivity;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.AssignmentLec;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AssignmentStd extends Fragment implements ListView.OnItemClickListener{

    private View view;
    private ListView listView;
    private ArrayList<String>Data=new ArrayList<>();
    private ArrayList<String>Ids=new ArrayList<>();
    private ServerDB database;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_assignment_std, container, false);
        database=new ServerDB(getActivity());
        listView=(ListView)view.findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
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
        Ids.clear();
        final String query = "SELECT concat(c.cr_name,' ',s.session_time),s.session_id" +
                " FROM sessions s, courses c, time_table t" +
                " WHERE c.cr_id=s.cr_id AND s.session_id=t.session_id AND t.std_id='" + StudentActivity.Id + "'";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1 = new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i < arr1.length(); i++) {
                        arr2 = arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0"));
                        Ids.add(arr2.getJSONObject(1).getString("col1"));
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
        Intent intent=new Intent(getActivity(), AssignmentStdActivity.class);
        intent.putExtra("session_id",Ids.get(i));
        intent.putExtra("session_title",Data.get(i));
        startActivity(intent);
    }


}