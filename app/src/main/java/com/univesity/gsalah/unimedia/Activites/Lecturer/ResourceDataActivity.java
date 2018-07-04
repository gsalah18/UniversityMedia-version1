package com.univesity.gsalah.unimedia.Activites.Lecturer;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SNIHostName;

public class ResourceDataActivity extends DriveActivity implements FloatingActionButton.OnClickListener {

    private String SessionId;
    private ListView listView;
    private ServerDB database;
    private Context context=this;
    private ArrayList<String> Data=new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        SessionId=getIntent().getExtras().getString("session_id");
        setTitle(getIntent().getExtras().getString("session_name"));
        database=new ServerDB(this);
        listView=(ListView)findViewById(R.id.listview);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                IntResource();
            }
        });
        IntResource();
    }

    private void IntResource() {
        Data.clear();
        final String query="SELECT re_name FROM resources WHERE session_id='"+SessionId+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1 = new JSONArray(response);
                    JSONArray arr2;
                    for (int i = 0; i <arr1.length() ; i++) {
                        arr2=arr1.getJSONArray(i);
                        Data.add(arr2.getJSONObject(0).getString("col0"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listView.setAdapter(new ArrayAdapter<String>(context,R.layout.temp_txt_layout,Data));
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
        AlertDialog.Builder adb=new AlertDialog.Builder(this);
        adb.setItems(new String[]{getResources().getString(R.string.select_file), getResources().getString(R.string.select_folder)}, new DialogInterface.OnClickListener() {
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
        adb.show();
    }
    private void InsertRes(String resource_name, final String resource_link){
        final String query="INSERT INTO resources VALUES('"+resource_name+"','"+SessionId+"','"+resource_link+"')";
        StringRequest request=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp)+ "/dbAction.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.trim().equals("Done")){
                    IntResource();
                }else database.MSG(getResources().getString(R.string.something_went_wrong));
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
        Singlton.getInstance(context).AddtoRequest(request);
    }
    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    protected void onDriveClientReady() {

    }

    private void RetriveFileMetadata(DriveFile mfile){
        Task<Metadata>getMetadataTask=getDriveResourceClient().getMetadata(mfile);
        getMetadataTask.addOnSuccessListener(new OnSuccessListener<Metadata>() {
            @Override
            public void onSuccess(Metadata metadata) {
                InsertRes(metadata.getTitle(),metadata.getAlternateLink());
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
                InsertRes(metadata.getTitle(),metadata.getAlternateLink());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(getString(R.string.read_failed));
            }
        });
    }
}
