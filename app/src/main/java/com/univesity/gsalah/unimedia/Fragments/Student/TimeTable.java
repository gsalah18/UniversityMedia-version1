package com.univesity.gsalah.unimedia.Fragments.Student;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Adapters.Time_Table_Adapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Models.Course_Class;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TimeTable extends Fragment {

    private ServerDB db;
    private LinkedList<Course_Class> Data = new LinkedList<>();
    private ListView list;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_time_table, container, false);
        list = (ListView) v.findViewById(R.id.list);
        db = new ServerDB(getActivity());

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        Refresh();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Refresh();
            }
        });
        return v;
    }

    private void Refresh() {
        Data.clear();
        final String query = "SELECT c.cr_id, c.cr_name, s.session_time, s.session_days, s.session_room, " +
                "concat(l.lec_degree,'.',l.lec_fname ,' ', l.lec_lname), s.session_id " +
                "FROM sessions s,lecturer l, time_table t, courses c " +
                "WHERE t.std_id='" + StudentActivity.Id + "' AND t.session_id=s.session_id AND l.lec_id=s.lec_id AND c.cr_id=s.cr_id";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray arr1 = new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i < arr1.length(); i++) {
                        arr2 = arr1.getJSONArray(i);
                        Data.add(new Course_Class(arr2.getJSONObject(6).getInt("col6")
                                , arr2.getJSONObject(0).getInt("col0")
                                , arr2.getJSONObject(1).getString("col1")
                                , arr2.getJSONObject(2).getString("col2")
                                , arr2.getJSONObject(3).getString("col3")
                                , arr2.getJSONObject(4).getString("col4")
                                , arr2.getJSONObject(5).getString("col5")));
                    }
                } catch (JSONException e) {
                    db.MSG(e + "");
                }

                list.setAdapter(new Time_Table_Adapter(getActivity(), Data));
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                db.MSG(getActivity().getResources().getString(R.string.server_error));
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
}
