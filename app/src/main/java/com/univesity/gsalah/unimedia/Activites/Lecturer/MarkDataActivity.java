package com.univesity.gsalah.unimedia.Activites.Lecturer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;
import com.github.clans.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarkDataActivity extends AppCompatActivity implements FloatingActionButton.OnClickListener,ListView.OnItemClickListener {

    private String SessionId;
    private ListView listView;
    private ServerDB database;
    private Context context=this;
    private int situation=1;
    private FloatingActionButton first_fab,second_fab,midterm_fab,final_fab,activities_fab;
    private ArrayList<String> Data=new ArrayList<>();
    private ArrayList<String>Ids=new ArrayList<>();
    private ArrayList<AlertDialog>Dialogs=new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SessionId=getIntent().getExtras().getString("session_id");
        setTitle(getIntent().getExtras().getString("session_name"));
        database=new ServerDB(this);
        listView=(ListView)findViewById(R.id.listview);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                InitStd();
            }
        });
        InitStd();
        listView.setOnItemClickListener(this);

        first_fab=(FloatingActionButton)findViewById(R.id.first_fab);
        first_fab.setOnClickListener(this);

        second_fab=(FloatingActionButton)findViewById(R.id.second_fab);
        second_fab.setOnClickListener(this);

        midterm_fab=(FloatingActionButton)findViewById(R.id.midterm_fab);
        midterm_fab.setOnClickListener(this);

        final_fab=(FloatingActionButton)findViewById(R.id.final_fab);
        final_fab.setOnClickListener(this);

        activities_fab=(FloatingActionButton)findViewById(R.id.activites_fab);
        activities_fab.setOnClickListener(this);
    }
    @Override
    public void onBackPressed() {
        finish();
    }

    private void InitStd() {
        final String query="SELECT s.std_id, concat(s.std_id,' , ',s.std_fname,' ',s.std_lname) " +
                "FROM student s, time_table t " +
                "WHERE t.session_id='"+SessionId+"' and s.std_id=t.std_id";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr.length() ; i++) {
                        arr2=arr.getJSONArray(i);
                        Ids.add(arr2.getJSONObject(0).getString("col0"));
                        Data.add((arr2.getJSONObject(1).getString("col1")));
                    }
                    listView.setAdapter(new ArrayAdapter<String>(context,R.layout.centered_textview,Data));

                } catch (JSONException e) {
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


    private void AddaMark(final int position,String type){
        AlertDialog.Builder MarkDialog=new AlertDialog.Builder(this);
        View view= LayoutInflater.from(this).inflate(R.layout.add_mark_dialog,null);
        MarkDialog.setView(view);
        ((TextView)view.findViewById(R.id.std_name)).setText(Data.get(position));
        final Spinner mark_spinner=(Spinner)view.findViewById(R.id.mark_spinner);
        String []strs=getResources().getStringArray(R.array.mark_types);
        for (int i = 0; i <strs.length ; i++) {
            if(type.equals(strs[i])){
                mark_spinner.setSelection(i);
                break;
            }
        }
        final EditText etxt=(EditText)view.findViewById(R.id.mark_etxt);
        final EditText fetxt=(EditText)view.findViewById(R.id.fullmark_etxt);

        mark_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final String Q="SELECT mark,total_mark FROM marks WHERE std_id='"+Ids.get(position)+"' and session_id='"+SessionId+"'" +
                        " and type='"+mark_spinner.getSelectedItem()+"'";
                StringRequest strRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray arr= new JSONArray(response);
                            JSONArray arr2=arr.getJSONArray(0);
                            etxt.setText(arr2.getJSONObject(0).getString("col0"));
                            fetxt.setText(arr2.getJSONObject(1).getString("col1"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(etxt.getText().toString().length()>0)
                            situation=2;
                        else situation=1;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        database.MSG(error+"");
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String>map=new HashMap<>();
                        map.put("query",Q);
                        return map;
                    }
                };
                Singlton.getInstance(context).AddtoRequest(strRequest);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        MarkDialog.setPositiveButton(getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                if(mark_spinner.getSelectedItem().toString().length()>0&&etxt.getText().toString().length()>0&&fetxt.getText().toString().length()>0){
                    final String query="INSERT INTO marks VALUES('"+mark_spinner.getSelectedItem()+"'," +
                            "'"+ etxt.getText()+"','"+fetxt.getText()+"','"+Ids.get(position)+"','"+SessionId+"')";
                    final String query2="UPDATE marks SET mark='"+etxt.getText()+"',total_mark='"+fetxt.getText()+"' WHERE std_id='"+Ids.get(position)+"' and session_id='"+SessionId+"'" +
                            " and type='"+mark_spinner.getSelectedItem()+"'";
                    StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response.trim().equals("Done"))
                                dialogInterface.cancel();
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
                            if (situation==1)
                                map.put("query",query);
                            else map.put("query",query2);
                            return map;
                        }
                    };
                    Singlton.getInstance(context).AddtoRequest(stringRequest);
                }
            }
        });
        MarkDialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(AlertDialog dialog:Dialogs)
                    dialog.cancel();
            }
        });
        AlertDialog alert=MarkDialog.create();
        alert.show();
        Dialogs.add(alert);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        AddaMark(i,getResources().getString(R.string.first));
    }

    @Override
    public void onClick(View view) {
        String type="";
        if(view==first_fab)
            type="First";
        else if(view==second_fab)
            type="Second";
        else if(view==midterm_fab)
            type="Midterm";
        else if (view==final_fab)
            type="Final";
        else if(view==activities_fab)
            type="Activities";

        for (int i = 0; i <Ids.size() ; i++) {
            AddaMark(i,type);
        }
    }
}
