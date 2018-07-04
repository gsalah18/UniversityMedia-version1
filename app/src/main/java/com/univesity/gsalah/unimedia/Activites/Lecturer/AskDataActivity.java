package com.univesity.gsalah.unimedia.Activites.Lecturer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.univesity.gsalah.unimedia.Activites.Student.ChatActivity;
import com.univesity.gsalah.unimedia.Adapters.AskAdapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Models.Question_Class;
import com.univesity.gsalah.unimedia.Others.ForegroundCheckTask;
import com.univesity.gsalah.unimedia.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AskDataActivity extends AppCompatActivity implements ListView.OnItemClickListener{

    private ServerDB database;
    private String SessionName;
    private ListView listView;
    private DatabaseReference ask_root;
    private ArrayList<Question_Class>Data=new ArrayList<>();
    private AskAdapter adapter;
    private Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_data);
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
        SessionName=getIntent().getExtras().getString("session_name");
        listView=(ListView)findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        setTitle(SessionName);
        ask_root= FirebaseDatabase.getInstance().getReference().child("ask");
        DatabaseReference session_root=ask_root.child(SessionName);
        session_root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    boolean foreground=new ForegroundCheckTask().execute(context).get();
                    if(!foreground)
                        NotifyMe();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        session_root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Refresh(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Refresh(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Refresh(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void NotifyMe(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.mainlogo)
                        .setContentTitle(SessionName)
                        .setContentText(getResources().getString(R.string.someone_asked_question));
        Intent resultIntent = new Intent(this, AskDataActivity.class);
        resultIntent.putExtra("session_name",SessionName);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(AskDataActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NotificationManager.IMPORTANCE_HIGH, mBuilder.build());
    }
    private void Refresh(DataSnapshot dataSnapshot){
        Iterator i=dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            long date=Long.parseLong(((DataSnapshot)i.next()).getValue().toString());
            String question=((DataSnapshot)i.next()).getValue().toString();
            String user_name=((DataSnapshot)i.next()).getValue().toString();
            Data.add(new Question_Class(user_name,question,date));
        }
        adapter=new AskAdapter(Data,this);
        listView.setAdapter(adapter);
        listView.setSelection(adapter.getCount()-1);
    }
    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Question_Class q=Data.get(i);
        AlertDialog.Builder adb=new AlertDialog.Builder(this);
        adb.setTitle(q.getUsername());
        adb.setMessage(q.getQuestion());
        adb.setNeutralButton(getResources().getString(R.string.ok),null);
        adb.show();
    }
}
