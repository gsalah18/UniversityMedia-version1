package com.univesity.gsalah.unimedia.Fragments.Student;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import android.support.design.widget.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.univesity.gsalah.unimedia.Activites.Student.PublisherActivity;
import com.univesity.gsalah.unimedia.Activites.Student.SubscriberActivity;
import com.univesity.gsalah.unimedia.Models.Session_Class;
import com.univesity.gsalah.unimedia.R;

import java.util.ArrayList;
import java.util.Iterator;


public class Live extends Fragment implements ListView.OnItemClickListener,FloatingActionButton.OnClickListener{

    private View view;
    private FloatingActionButton fab;
    private Context context=getActivity();
    private DatabaseReference dataref;
    private ArrayList<Session_Class> data;
    private ListView listView;
    private ArrayAdapter<Session_Class>adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_live, container, false);
        data=new ArrayList<>();
        listView=view.findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        fab=view.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        dataref= FirebaseDatabase.getInstance().getReference().child("sessions");
        dataref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Refresh(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Remove(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
        adb.setMessage(getResources().getString(R.string.enter_live_title));
        final View view2=LayoutInflater.from(getActivity()).inflate(R.layout.etxt_dialog,null);
        adb.setView(view2);
        adb.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText=view2.findViewById(R.id.etxt);
                if(editText.getText().length()>0){
                    Intent intent=new Intent(getActivity(), PublisherActivity.class);
                    intent.putExtra("sessionTitle",editText.getText().toString());
                    startActivity(intent);
                }
            }
        });
        adb.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(getActivity(), SubscriberActivity.class);
        intent.putExtra("live",data.get(position));
        startActivity(intent);
    }


    private void Refresh(DataSnapshot dataSnapshot){
        String studentId="";
        String studentName="";
        String sessionTitle="";
        String sessionId="";
        String token="";
        Iterator i=dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            sessionId=((DataSnapshot)i.next()).getValue().toString();
            sessionTitle=((DataSnapshot)i.next()).getValue().toString();
            studentId=((DataSnapshot)i.next()).getValue().toString();
            studentName=((DataSnapshot)i.next()).getValue().toString();
            token=((DataSnapshot)i.next()).getValue().toString();
        }
        data.add(new Session_Class(studentId,studentName,sessionTitle,sessionId,token));
        adapter=new ArrayAdapter<Session_Class>(getActivity(),R.layout.simple_row,R.id.rowTextView,data);
        listView.setAdapter(adapter);
    }
    private void Remove(DataSnapshot dataSnapshot){
        String studentId="";
        Iterator i=dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            ((DataSnapshot)i.next()).getValue().toString();
            ((DataSnapshot)i.next()).getValue().toString();
            studentId=((DataSnapshot)i.next()).getValue().toString();
            ((DataSnapshot)i.next()).getValue().toString();
            ((DataSnapshot)i.next()).getValue().toString();
        }
        data.remove(FindIndex(studentId));
        adapter=new ArrayAdapter<Session_Class>(getActivity(),R.layout.simple_row,R.id.rowTextView,data);
        listView.setAdapter(adapter);
    }
    private int FindIndex(String studentId){
        for (int i = 0; i <data.size() ; i++) {
            if(data.get(i).getStudentId().equals(studentId))
                return i;
        }
        return -1;
    }
}
