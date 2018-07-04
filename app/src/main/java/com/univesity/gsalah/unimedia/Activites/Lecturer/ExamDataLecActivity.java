package com.univesity.gsalah.unimedia.Activites.Lecturer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExamDataLecActivity extends AppCompatActivity implements FloatingActionButton.OnClickListener{
    private ServerDB database;
    private ArrayList<String>Data=new ArrayList<>();
    private String SessionId;
    private ListView listview;
    private String exam_type;
    private Context context=this;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_data_lec);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        database=new ServerDB(this);
        SessionId=getIntent().getExtras().getString("session_id");
        setTitle(getIntent().getExtras().getString("session_name"));
        FloatingActionButton floatingActionButton=(FloatingActionButton)findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
        listview=(ListView)findViewById(R.id.listview);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                InitExam();
            }
        });
        InitExam();
    }

    private void InitExam() {
        Data.clear();
        final String query="SELECT concat(type,' , ',DATE_FORMAT(exam_time,'%d/%m , %h')) FROM exams WHERE session_id='"+SessionId+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0"));
                    }
                    listview.setAdapter(new ArrayAdapter<String>(context,R.layout.temp_txt_layout,Data));

                } catch (JSONException e) {
                    e.printStackTrace();
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
    public void onClick(View view) {
        final Date exam_date=new Date();

        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        final View vv= LayoutInflater.from(context).inflate(R.layout.add_exam_dialog,null);
        dialog.setView(vv);
        dialog.setTitle(getResources().getString(R.string.add_exam));
        ((Button)vv.findViewById(R.id.add_date_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        exam_date.setYear(i);
                        exam_date.setMonth(i1);
                        exam_date.setDate(i2);
                    }
                },mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        ((Button)vv.findViewById(R.id.add_time_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mhour=c.get(Calendar.HOUR);
                int mmin=c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog=new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        exam_date.setHours(i);
                        exam_date.setMinutes(i1);
                    }
                },mhour,mmin,false);
                timePickerDialog.show();
            }
        });

        dialog.setNegativeButton(getResources().getString(R.string.cancel),null);
        dialog.setPositiveButton(getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String db_date=fmt.format(exam_date);
                exam_type=((Spinner)vv.findViewById(R.id.exam_spinner)).getSelectedItem().toString();
                final String query="INSERT INTO exams VALUES('"+SessionId+"','"+db_date+"','"+exam_type+"')";
                StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) +
                        "/dbAction.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("Done")) {
                            dialogInterface.cancel();
                            InitExam();
                        }
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
                        map.put("query",query);
                        return map;
                    }
                };
                Singlton.getInstance(context).AddtoRequest(stringRequest);
            }
        });
        dialog.show();
    }
}
