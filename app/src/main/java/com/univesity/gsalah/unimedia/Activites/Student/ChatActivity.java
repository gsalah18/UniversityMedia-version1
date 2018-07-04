package com.univesity.gsalah.unimedia.Activites.Student;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Adapters.ChatAdapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Fragments.Student.Chat;
import com.univesity.gsalah.unimedia.Models.Chat_Class;
import com.univesity.gsalah.unimedia.Others.ForegroundCheckTask;
import com.univesity.gsalah.unimedia.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChatActivity extends AppCompatActivity implements ImageButton.OnClickListener{

    private RecyclerView listView;
    private EditText etext;
    private ImageButton send_btn;
    private ServerDB database;
    private String ChatName;
    private String ChatId;
    private String UserId;
    private String UserName;
    private ChatAdapter adapter;
    private String temp_key;
    DatabaseReference root;
    Context context=this;
    private ArrayList<Chat_Class>Data=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ChatName=getIntent().getExtras().getString("chat_name");
        ChatId=getIntent().getExtras().getString("chat_id");
        setTitle(ChatName);
        listView=(RecyclerView)findViewById(R.id.listview);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ChatAdapter(Data,context,UserId);
        listView.setAdapter(adapter);

        etext=(EditText)findViewById(R.id.editWriteMessage);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        send_btn=(ImageButton)findViewById(R.id.btnSend);
        send_btn.setOnClickListener(this);
        database=new ServerDB(this);
        UserId=StudentActivity.Id+"";
        UserName=StudentActivity.UserName;


        DatabaseReference chat_root=FirebaseDatabase.getInstance().getReference().child("chat");
        root= chat_root.child(ChatName);
        root.addValueEventListener(new ValueEventListener() {
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
        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RefreshFireBase(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                RefreshFireBase(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                RefreshFireBase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void NotifyMe() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.mainlogo)
                        .setContentTitle(ChatName)
                        .setContentText(getResources().getString(R.string.someone_sent_message));
        Intent resultIntent = new Intent(this, ChatActivity.class);
        resultIntent.putExtra("chat_name",ChatName);
        resultIntent.putExtra("chat_id",ChatId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatActivity.class);
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

    private void RefreshFireBase(DataSnapshot dataSnapshot) {

        Iterator i=dataSnapshot.getChildren().iterator();
        String user_id,user_name,msg;
        long date;
        while(i.hasNext()){
            date=Long.parseLong(((DataSnapshot)i.next()).getValue().toString());
            msg=((DataSnapshot)i.next()).getValue().toString();
            user_id=((DataSnapshot)i.next()).getValue().toString();
            user_name=((DataSnapshot)i.next()).getValue().toString();

            Chat_Class chat=new Chat_Class(msg,user_name,user_id,date);
            Data.add(chat);
        }
        adapter=new ChatAdapter(Data,context,UserId);
        listView.setAdapter(adapter);
        listView.getLayoutManager().scrollToPosition(adapter.getItemCount()-1);
        //adapter.notifyDataSetChanged();
        //listView.setSelection(adapter.getCount()-1);
    }


    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onClick(View view) {
        String message=etext.getText().toString();
        if(message.length()>0) {
            Map<String,Object>map=new HashMap<>();
            temp_key=root.push().getKey();
            root.updateChildren(map);
            DatabaseReference root_msg=root.child(temp_key);
            Map<String,Object>map2=new HashMap<>();
            map2.put("user_id",UserId);
            map2.put("msg",message);
            map2.put("date",new Date().getTime());
            map2.put("user_name",UserName);
            root_msg.updateChildren(map2);
            etext.setText("");
        }
    }
}
