package com.univesity.gsalah.unimedia.Fragments.Student;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Student.ChatActivity;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Adapters.AddStdAdapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Chat extends Fragment implements ListView.OnItemClickListener,FloatingActionButton.OnClickListener{

    private ArrayList<String>Data=new ArrayList<>();
    private ArrayList<String>Ids=new ArrayList<>();
    private ListView listView;
    private ServerDB database;
    private String AddedChatId;
    private ArrayList<String>AddedStdNames=new ArrayList<>();
    private ArrayList<String>AddedStdIds=new ArrayList<>();
    private ArrayList<String>ConfirmedStdIds=new ArrayList<>();
    private ArrayList<Integer>Indices=new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_chat, container, false);
        listView=(ListView)v.findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        database=new ServerDB(getActivity());
        FloatingActionButton fab=(FloatingActionButton)v.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        swipeRefreshLayout=(SwipeRefreshLayout)v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Refresh();
            }
        });
        Refresh();
        return v;
    }
    private void Refresh(){
        Data.clear();
        final String query="SELECT ch.chat_name,i.chat_id FROM chat ch,inchat i WHERE i.std_id='"+ StudentActivity.Id+"'" +
                " AND ch.chat_id=i.chat_id";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0"));
                        Ids.add(arr2.getJSONObject(1).getString("col1"));
                    }
                    listView.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.simple_row,R.id.rowTextView,Data));

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
        String chat_name=database.FirebaseName(Data.get(i));
        Intent intent=new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("chat_id",Ids.get(i));
        intent.putExtra("chat_name",chat_name);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
        View v=LayoutInflater.from(getActivity()).inflate(R.layout.etxt_dialog,null);
        adb.setView(v);
        adb.setTitle(getResources().getString(R.string.add_chat));
        adb.setMessage(getResources().getString(R.string.add_chat_name));
        final EditText etxt=(EditText)v.findViewById(R.id.etxt);
        etxt.requestFocus();
        etxt.setHint(getResources().getString(R.string.chat_name));
        adb.setPositiveButton(getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String str=etxt.getText().toString();
                if(str.length()>0) {
                    final String query = "INSERT INTO chat (chat_name)VALUES('"+str+"')";
                    StringRequest stringRequest=new StringRequest(Request.Method.POST,
                            getActivity().getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response.trim().equals("Done")){
                                getChatId(str);
                            }else database.MSG(getResources().getString(R.string.same_chat_name));
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            database.MSG(getString(R.string.server_error));
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
                    dialogInterface.cancel();
                }
            }
        });
        adb.setNegativeButton(getResources().getString(R.string.cancel),null);
        adb.show();
    }
    private void getChatId(String Chatname){
        final String query="SELECT chat_id FROM chat WHERE chat_name='"+Chatname+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    AddedChatId=(new JSONArray(response).getJSONArray(0).getJSONObject(0).getString("col0"));
                    GetMajorStudent();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(getActivity().getResources().getString(R.string.server_error));
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
    private void GetMajorStudent(){
        AddedStdIds.clear();
        AddedStdNames.clear();
        ConfirmedStdIds.clear();
        ConfirmedStdIds.add(StudentActivity.Id+"");
        final String query="SELECT std_id,concat(std_fname,' ',std_lname) FROM student WHERE std_major='"+StudentActivity.Major+"' AND std_id<>'"+StudentActivity.Id+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        AddedStdIds.add(arr2.getJSONObject(0).getString("col0"));
                        AddedStdNames.add(arr2.getJSONObject(1).getString("col1"));
                    }
                    AddSTD();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(getActivity().getResources().getString(R.string.server_error));
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
    private void AddSTD(){
        AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
        View v=LayoutInflater.from(getActivity()).inflate(R.layout.list_dialog,null);
        adb.setView(v);
        final ListView listView=(ListView)v.findViewById(R.id.listview);
        final AddStdAdapter adapter=new AddStdAdapter(AddedStdIds,AddedStdNames,getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!Indices.contains(i)) {
                    ConfirmedStdIds.add(AddedStdIds.get(i));
                    adapter.Selected(i);
                }
                Indices.add(i);
            }
        });
        adb.setPositiveButton(getResources().getString(R.string.add_them), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(ConfirmedStdIds.size()>0){
                    for (int j = 0; j <ConfirmedStdIds.size() ; j++) {
                        AddStdtoChat(ConfirmedStdIds.get(j));
                    }
                    dialogInterface.cancel();
                    Toast.makeText(getActivity(), getResources().getString(R.string.chat_added), Toast.LENGTH_SHORT).show();
                    Refresh();
                }else database.MSG(getResources().getString(R.string.pic_one_student));
            }
        });
        AlertDialog alertDialog=adb.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    private void AddStdtoChat(String StdId){
        final String query="INSERT INTO inchat VALUES('"+StdId+"','"+AddedChatId+"')";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(getActivity().getResources().getString(R.string.server_error));
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

