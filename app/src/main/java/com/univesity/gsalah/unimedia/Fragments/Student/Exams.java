package com.univesity.gsalah.unimedia.Fragments.Student;


import android.app.AlertDialog;
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
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class Exams extends Fragment implements ListView.OnItemClickListener{

    private LinkedList<String> courses;

    private LinkedList<Integer>sessions=new LinkedList<>();
    private ServerDB db;
    private ListView exp_list;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_exams, container, false);

        courses=new LinkedList<>();

        db=new ServerDB(getActivity());
        exp_list= (ListView)v.findViewById(R.id.listview);
        exp_list.setOnItemClickListener(this);
        swipeRefreshLayout=(SwipeRefreshLayout)v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                GetCourses();
            }
        });
        GetCourses();


        return v;
    }

    private void GetCourses() {
        sessions.clear();
        courses.clear();
        final String query="SELECT s.session_id,c.cr_name FROM sessions s, courses c, time_table t " +
                "WHERE s.cr_id=c.cr_id AND s.session_id=t.session_id AND t.std_id='"+ StudentActivity.Id+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //db.MSG(response);
                    JSONArray arr1 = new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i < arr1.length(); i++) {
                        arr2 = arr1.getJSONArray(i);

                        sessions.add(arr2.getJSONObject(0).getInt("col0"));
                        courses.add(arr2.getJSONObject(1).getString("col1"));


                    }
                    exp_list.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.temp_txt_layout,courses));

                } catch (JSONException e) {
                    db.MSG(e + "1");
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                db.MSG(getActivity().getResources().getString(R.string.server_error));
                swipeRefreshLayout.setRefreshing(false);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap();
                map.put("query",query);
                return map;
            }
        };
        Singlton.getInstance(getActivity()).AddtoRequest(stringRequest);
    }

    private void GetExams(int position) {
        final String query1="SELECT concat(type,' : ',exam_time) FROM exams WHERE session_id='"+sessions.get(position)+"'";
        StringRequest stringRequest1=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //db.MSG(response);
                    JSONArray arr1 = new JSONArray(response);
                    JSONArray arr2;
                    String str="There is no exams";
                    if(arr1.length()>0)
                        str="";
                    for (int i = 0; i < arr1.length(); i++) {
                        arr2 = arr1.getJSONArray(i);
                        str+=arr2.getJSONObject(0).getString("col0");
                        str+="\n\n";
                    }
                    AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
                    adb.setTitle("Exams");
                    adb.setMessage(str);
                    adb.setNeutralButton("OK",null);
                    adb.show();
                } catch (JSONException e) {
                    db.MSG(e + "2");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                db.MSG(error+"2");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>map1=new HashMap<>();
                map1.put("query",query1);
                return map1;
            }
        };
        Singlton.getInstance(getActivity()).AddtoRequest(stringRequest1);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        GetExams(i);
    }
}
