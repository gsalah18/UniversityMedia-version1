package com.univesity.gsalah.unimedia.Fragments.Student;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.univesity.gsalah.unimedia.Activites.Student.AddPostActivity;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Adapters.PostAdapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Models.Post_Class;
import com.univesity.gsalah.unimedia.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class News extends Fragment implements FloatingActionButton.OnClickListener {

    private PostAdapter adapter;
    private ListView listView;
    private ArrayList<Post_Class> data = new ArrayList<>();
    private View view;
    private ServerDB db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = new ServerDB(getActivity());

        if (view == null)
            view = inflater.inflate(R.layout.fragment_news, container, false);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        listView = (ListView) view.findViewById(R.id.listView);

        data.clear();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("posts");
        Query query = database.orderByChild("date");
        query.addChildEventListener(new ChildEventListener() {
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
                Refresh(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Refresh(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                db.MSG(databaseError + "");
            }
        });
        DatabaseReference adminsref = FirebaseDatabase.getInstance().getReference().child("admins").child(StudentActivity.Id + "");
        adminsref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.getValue().toString().equals("yes"))
                        fab.setVisibility(View.VISIBLE);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void Refresh(DataSnapshot dataSnapshot) {
        String id = dataSnapshot.getKey();
        String title = "";
        long date = 0;
        String content = "";
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            content = ((DataSnapshot) i.next()).getValue().toString();
            date = Long.parseLong(((DataSnapshot) i.next()).getValue().toString());
            title = ((DataSnapshot) i.next()).getValue().toString();
        }
        data.add(new Post_Class(id, title, content, date));
        adapter = new PostAdapter(getActivity(), Reverse(data));
        listView.setAdapter(adapter);

    }

    private ArrayList<Post_Class> Reverse(ArrayList<Post_Class> data) {
        ArrayList<Post_Class> result = new ArrayList<>();
        for (int i = data.size() - 1; i >= 0; i--) {
            result.add(data.get(i));
        }
        return result;
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(getActivity(), AddPostActivity.class));
    }
}
