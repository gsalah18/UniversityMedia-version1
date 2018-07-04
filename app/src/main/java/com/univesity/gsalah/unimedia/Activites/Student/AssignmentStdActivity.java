package com.univesity.gsalah.unimedia.Activites.Student;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AssignmentStdActivity extends DriveActivity implements ListView.OnItemClickListener,ListView.OnItemLongClickListener{

    private String SessionID;
    private String SessionTitle;
    private Context context=this;
    private ServerDB database;
    private ArrayList<String>Data=new ArrayList<>();
    private ArrayList<String>Ids=new ArrayList<>();
    private ArrayList<String>Links=new ArrayList<>();
    private ListView listView;
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat fmt2 = new SimpleDateFormat("yyyy/MM/dd");
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_std);
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
        SessionID=getIntent().getExtras().getString("session_id");
        SessionTitle=getIntent().getExtras().getString("session_title");
        setTitle(SessionTitle);
        listView=(ListView)findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
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

    private void Refresh() {
        Data.clear();
        final String query="SELECT ass_name,ass_start,ass_deadline,ass_id,ass_link FROM assignment" +
                " WHERE session_id='"+SessionID+"' AND '"+fmt2.format(new Date())+"' BETWEEN ass_start and ass_deadline";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0")+"\n"
                                +"Start: "+arr2.getJSONObject(1).getString("col1")+"\t"
                                +"Deadline: "+arr2.getJSONObject(2).getString("col2"));
                        Ids.add(arr2.getJSONObject(3).getString("col3"));
                        Links.add(arr2.getJSONObject(4).getString("col4"));
                    }
                    listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,Data));
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
    public boolean onItemLongClick(AdapterView<?> adapterView, View view,final int position, long l) {
        AlertDialog.Builder adb=new AlertDialog.Builder(this);
        adb.setTitle(getResources().getString(R.string.add_selution));
        adb.setItems(new String[]{getResources().getString(R.string.select_file), getResources().getString(R.string.select_folder)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                if(i==0){
                    pickTextFile().addOnSuccessListener((Activity) context, new OnSuccessListener<DriveId>() {
                        @Override
                        public void onSuccess(DriveId driveId) {
                            RetriveFileMetadata(driveId.asDriveFile(),position);

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
                            RetiveFolderMetadata(driveId.asDriveFolder(),position);
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
        adb.show();
        return true;
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        Uri uri=Uri.parse(Links.get(position));
        Intent intent=new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }

    private void InsertRes(String alternateLink,int assignmentIndex) {
        final String query="INSERT INTO stdsolution VALUES("+Ids.get(assignmentIndex)+",'"+StudentActivity.Id+"','"+alternateLink+"'" +
                ",'"+fmt.format(new Date())+"')";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.trim().equals("Done"))
                showMessage(getResources().getString(R.string.solution_sent));
                else database.MSG(getResources().getString(R.string.something_went_wrong));
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

    private void RetriveFileMetadata(DriveFile driveFile, final int assignmentIndex) {
        Task<Metadata> getMetadataTask=getDriveResourceClient().getMetadata(driveFile);
        getMetadataTask.addOnSuccessListener(new OnSuccessListener<Metadata>() {
            @Override
            public void onSuccess(Metadata metadata) {
                InsertRes(metadata.getAlternateLink(),assignmentIndex);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(getString(R.string.read_failed));
            }
        });
    }
    private void RetiveFolderMetadata(DriveFolder folder, final int assignmentIndex) {
        Task<Metadata>getMetadataTask=getDriveResourceClient().getMetadata(folder);
        getMetadataTask.addOnSuccessListener(new OnSuccessListener<Metadata>() {
            @Override
            public void onSuccess(Metadata metadata) {
                InsertRes(metadata.getAlternateLink(),assignmentIndex);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(getString(R.string.read_failed));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDriveClientReady() {
        
    }


}
