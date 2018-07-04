package com.univesity.gsalah.unimedia.Fragments.Student;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AskStd extends Fragment implements ListView.OnItemClickListener{

    private View view;
    private ListView listView;
    private ServerDB database;
    private ArrayList<String> Data = new ArrayList<>();
    private DatabaseReference ask_root;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ask, container, false);
        database = new ServerDB(getActivity());
        listView = (ListView) view.findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        ask_root= FirebaseDatabase.getInstance().getReference().child("ask");
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
        final String query = "SELECT concat(c.cr_name,' ',s.session_time)" +
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
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
        View v=LayoutInflater.from(getActivity()).inflate(R.layout.add_question_dialog,null);
        adb.setView(v);
        final EditText question_etxt=(EditText)v.findViewById(R.id.question_etxt);
        final CheckBox any_check=(CheckBox)v.findViewById(R.id.any_check);
        adb.setTitle(getResources().getString(R.string.send_question));
        adb.setMessage(getResources().getString(R.string.write_question));
        adb.setPositiveButton(getResources().getString(R.string.send), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String question=question_etxt.getText().toString();
                String UserName=any_check.isChecked()?"Anonymous":StudentActivity.UserName;
                if(question.length()>0){
                    DatabaseReference root=ask_root.child(database.FirebaseName(Data.get(position)));
                    Map<String,Object>map=new HashMap<>();
                    root.updateChildren(map);
                    String temp_key=root.push().getKey();
                    DatabaseReference root2=root.child(temp_key);
                    Map<String,Object>map2=new HashMap<>();
                    map2.put("user_name",UserName);
                    map2.put("question",question);
                    map2.put("date",new Date().getTime());

                    root2.updateChildren(map2);
                    Toast.makeText(getActivity(), getResources().getString(R.string.sent), Toast.LENGTH_SHORT).show();
                    dialogInterface.cancel();
                }else{
                    database.MSG(getResources().getString(R.string.write_question_before));
                }
            }
        });
        adb.setNegativeButton(getResources().getString(R.string.cancel),null);
        AlertDialog alertDialog=adb.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
