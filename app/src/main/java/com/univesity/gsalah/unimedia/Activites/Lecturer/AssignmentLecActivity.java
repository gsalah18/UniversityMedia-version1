package com.univesity.gsalah.unimedia.Activites.Lecturer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.DriveActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentLecActivity extends DriveActivity implements FloatingActionButton.OnClickListener,ListView.OnItemClickListener{

    private String AssignmentLink="";
    private String AssignmentName="";
    private Context context=this;
    private Date StartDate;
    private Date Deadline;
    private ServerDB database;
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat fmt2 = new SimpleDateFormat("yyyy/MM/dd");
    private ArrayList<String>Data=new ArrayList<>();
    private ArrayList<String>Ids=new ArrayList<>();
    private ArrayList<String>AssNames=new ArrayList<>();
    private String SessionTitle;
    private String SessionId;
    private ListView listview;
    private TextView Link_txt;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_lec);
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
        SessionTitle=getIntent().getExtras().getString("session_title");
        listview=(ListView)findViewById(R.id.listview);
        listview.setOnItemClickListener(this);
        setTitle(SessionTitle);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Refresh();
            }
        });
        Refresh();
    }

    @Override
    public void onClick(View view) {
        AlertDialog.Builder adb=new AlertDialog.Builder(this);
        View v= LayoutInflater.from(this).inflate(R.layout.add_assignment_dialog,null);
        adb.setView(v);
        final EditText name_txt=(EditText)v.findViewById(R.id.name_etxt);
        Button select_ass=(Button)v.findViewById(R.id.select_ass_btn);
        Link_txt=(TextView)v.findViewById(R.id.select_ass_txt);
        select_ass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder a=new AlertDialog.Builder(context);
                a.setItems(new String[]{getResources().getString(R.string.select_file), getResources().getString(R.string.select_folder)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        if(i==0){
                            pickTextFile().addOnSuccessListener((Activity) context, new OnSuccessListener<DriveId>() {
                                @Override
                                public void onSuccess(DriveId driveId) {
                                    RetriveFileMetadata(driveId.asDriveFile());
                                    dialogInterface.cancel();
                                }
                            }).addOnFailureListener((Activity) context, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage(getString(R.string.file_not_selected));
                                    dialogInterface.cancel();
                                }
                            });
                        }else if(i==1){
                            pickFolder().addOnSuccessListener((Activity) context, new OnSuccessListener<DriveId>() {
                                @Override
                                public void onSuccess(DriveId driveId) {
                                    RetiveFolderMetadata(driveId.asDriveFolder());
                                    dialogInterface.cancel();
                                }
                            }).addOnFailureListener((Activity) context, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage(getString(R.string.select_folder));
                                    dialogInterface.cancel();
                                }
                            });
                        }
                    }

                });
                a.show();
            }
        });

        Button start_date=(Button)v.findViewById(R.id.start_date_btn);
        final TextView start_date_txt=(TextView)v.findViewById(R.id.start_date_txt);
        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        StartDate=new Date();
                        StartDate.setYear(i-1900);
                        StartDate.setMonth(i1);
                        StartDate.setDate(i2);
                        start_date_txt.setText(fmt2.format(StartDate));
                    }
                },mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        Button deadline=(Button)v.findViewById(R.id.deadline_btn);
        final TextView deadline_txt=(TextView)v.findViewById(R.id.deadline_txt);
        deadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Deadline=new Date();
                        Deadline.setYear(i-1900);
                        Deadline.setMonth(i1);
                        Deadline.setDate(i2);
                        deadline_txt.setText(fmt2.format(Deadline));
                    }
                },mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        adb.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AssignmentName=name_txt.getText().toString();
                if(AssignmentName.length()>0&&AssignmentLink.length()>0&&StartDate!=null&&Deadline!=null){
                    final String query="INSERT INTO assignment (ass_name,ass_link,ass_start,ass_deadline,session_id)" +
                            "VALUES('"+AssignmentName+"','"+AssignmentLink+"','"+fmt.format(StartDate)+"'" +
                            ",'"+fmt.format(Deadline)+"','"+SessionId+"')";
                    StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response.trim().equals("Done")){
                                Refresh();
                            }else database.MSG(getResources().getString(R.string.something_went_wrong));
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
                            map.put("query",query);
                            return map;
                        }
                    };
                    Singlton.getInstance(context).AddtoRequest(stringRequest);
                }else database.MSG(getResources().getString(R.string.you_need_to_enter_all_information));
            }
        });
        adb.setNegativeButton("Cancel",null);
        AlertDialog alertDialog=adb.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void Refresh() {
        Data.clear();
        Ids.clear();
        final String query="SELECT ass_name,ass_start,ass_deadline,ass_id FROM assignment WHERE session_id='"+SessionId+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    String assname;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        assname=arr2.getJSONObject(0).getString("col0");
                        AssNames.add(assname);
                        Data.add(assname+"\n"
                        +"Start: "+arr2.getJSONObject(1).getString("col1")+"\t"
                        +"Deadline: "+arr2.getJSONObject(2).getString("col2"));
                        Ids.add(arr2.getJSONObject(3).getString("col3"));
                    }
                    listview.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,Data));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(error+"");
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
    public void onBackPressed() {
        this.finish();
    }


    @Override
    protected void onDriveClientReady() {

    }

    private void RetriveFileMetadata(DriveFile mfile){
        Task<Metadata> getMetadataTask=getDriveResourceClient().getMetadata(mfile);
        getMetadataTask.addOnSuccessListener(new OnSuccessListener<Metadata>() {
            @Override
            public void onSuccess(Metadata metadata) {
                AssignmentLink=metadata.getAlternateLink();
                Link_txt.setText(AssignmentLink);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(getString(R.string.read_failed));
            }
        });

    }
    private void RetiveFolderMetadata(DriveFolder folder){
        Task<Metadata>getMetadataTask=getDriveResourceClient().getMetadata(folder);
        getMetadataTask.addOnSuccessListener(new OnSuccessListener<Metadata>() {
            @Override
            public void onSuccess(Metadata metadata) {
                AssignmentLink=metadata.getAlternateLink();
                Link_txt.setText(AssignmentLink);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(getString(R.string.read_failed));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent=new Intent(AssignmentLecActivity.this,SolutionActivity.class);
        intent.putExtra("ass_id",Ids.get(i));
        intent.putExtra("ass_name",AssNames.get(i));
        startActivity(intent);
    }
}
