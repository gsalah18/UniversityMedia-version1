package com.univesity.gsalah.unimedia.Activites.Student;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaCas;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Models.Session_Class;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONException;
import org.json.JSONObject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PublisherActivity extends Activity implements Session.SessionListener, PublisherKit.PublisherListener {
    private String sessionTitle;
    private String sessionId;
    private String token;
    private String studentId;
    private String studentName;
    private  String apiKey="46115932";
    private static final int RC_VIDEO_APP_PERM = 124;
    private String LOG_TAG=PublisherActivity.class.getSimpleName();
    private FrameLayout frame;
    private Button startButton;
    private ImageView switchImage,endImage;

    private String temp;

    private Session mSession;
    private Publisher mPublisher;
    private Context context=this;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publisher);
        sessionTitle=getIntent().getExtras().getString("sessionTitle");
        studentId=StudentActivity.Id+"";
        studentName=StudentActivity.UserName;
        frame=findViewById(R.id.frame);
        startButton=findViewById(R.id.start_button);
        switchImage=findViewById(R.id.switch_image);
        endImage=findViewById(R.id.end_image);
        requestPermissions();
        progressDialog=new ProgressDialog(this);
        progressDialog.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            fetchSessionConnectionData();

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }


    public void StartClick(View view) {
        mSession.publish(mPublisher);
        startButton.setEnabled(false);
    }

    public void StopClick(View view) {
        mPublisher.destroy();
        mSession.disconnect();
    }

    public void SwitchCamClick(View view) {
        mPublisher.cycleCamera();
    }

    public void fetchSessionConnectionData() {
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://uni-media.herokuapp.com" + "/session",
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    sessionId = response.getString("sessionId");
                    token = response.getString("token");


                    mSession = new Session.Builder(PublisherActivity.this, apiKey, sessionId).build();
                    mSession.setSessionListener(PublisherActivity.this);
                    mSession.connect(token);

                } catch (JSONException error) {
                    Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
            }
        }));
    }

    @Override
    public void onConnected(Session session) {
        mPublisher=new Publisher.Builder(this).build();
        frame.addView(mPublisher.getView());
        mPublisher.setPublisherListener(this);
        startButton.setVisibility(View.VISIBLE);
        switchImage.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }

    @Override
    public void onDisconnected(Session session) {
        //Delete Data from Firebase
        RemoveFromFB();

        this.finish();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        //Delete From Firebase
        RemoveFromFB();
        finish();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        endImage.setVisibility(View.VISIBLE);
        //Add Data to Firebase.
        DatabaseReference sessionRef= FirebaseDatabase.getInstance().getReference().child("sessions");
        temp=sessionRef.push().getKey();
        DatabaseReference innerRef=sessionRef.child(temp);
        Session_Class session=new Session_Class(studentId,studentName,sessionTitle,sessionId,token);
        innerRef.updateChildren(session.toMap());
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        RemoveFromFB();
        finish();
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    private void RemoveFromFB(){
       DatabaseReference sessionRef=FirebaseDatabase.getInstance().getReference("sessions").child(temp);
       sessionRef.removeValue();
    }
}
