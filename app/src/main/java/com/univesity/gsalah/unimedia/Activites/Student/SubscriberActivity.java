package com.univesity.gsalah.unimedia.Activites.Student;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.squareup.picasso.Picasso;
import com.univesity.gsalah.unimedia.Models.Session_Class;
import com.univesity.gsalah.unimedia.R;

import org.w3c.dom.Text;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SubscriberActivity extends Activity implements Session.SessionListener {
    private  String apiKey="46115932";
    private static final int RC_VIDEO_APP_PERM = 124;
    private String LOG_TAG=PublisherActivity.class.getSimpleName();

    private FrameLayout frame;
    private ImageView profileImage;
    private TextView usernameText;

    private Session_Class session;
    private Session mSession;
    private Subscriber mSubcriber;
    private Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);
        frame=findViewById(R.id.frame);
        profileImage=findViewById(R.id.profile_image);
        usernameText=findViewById(R.id.username_txt);
        session= (Session_Class) getIntent().getExtras().getSerializable("live");

        StorageReference image_root = FirebaseStorage.getInstance().getReference().child("pictures").child(session.getStudentId());
        image_root.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).into(profileImage);
            }
        });
        usernameText.setText(session.getStudentName());
        requestPermissions();
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
            mSession=new Session.Builder(this,apiKey,session.getSessionId()).build();
            mSession.setSessionListener(this);
            mSession.connect(session.getToken());
        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }


    public void BackArrowClick(View view) {
        this.finish();
    }

    @Override
    public void onConnected(Session session) {

    }

    @Override
    public void onDisconnected(Session session) {
        this.finish();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        if(mSubcriber==null){
            mSubcriber=new Subscriber.Builder(context,stream).build();
            frame.addView(mSubcriber.getView());
            mSession.subscribe(mSubcriber);
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        this.finish();
        Toast.makeText(this, getResources().getString(R.string.live_ended), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }
}
