package com.univesity.gsalah.unimedia.Activites.Student;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.LoginActivity;
import com.univesity.gsalah.unimedia.Adapters.FragmentsAdapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Fragments.Student.AskStd;
import com.univesity.gsalah.unimedia.Fragments.Student.AssignmentStd;
import com.univesity.gsalah.unimedia.Fragments.Student.Chat;
import com.univesity.gsalah.unimedia.Fragments.Student.Exams;
import com.univesity.gsalah.unimedia.Fragments.Student.Live;
import com.univesity.gsalah.unimedia.Fragments.Student.Marks;
import com.univesity.gsalah.unimedia.Fragments.Student.News;
import com.univesity.gsalah.unimedia.Fragments.Student.ProfileStd;
import com.univesity.gsalah.unimedia.Fragments.Student.TimeTable;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudentActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String[][]Data;
    private ServerDB db;
    public static int Id;
    public static ImageView imageView;
    public static String Password;
    public static String UserName;
    public static String Major;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db=new ServerDB(this);
        Id=getIntent().getExtras().getInt("ID");
        Password=getIntent().getExtras().getString("Password");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        viewPager=(ViewPager)findViewById(R.id.viewpager);
        tabLayout=(TabLayout)findViewById(R.id.tablayout);

        Fragment []frags={new TimeTable(),new Chat(),new Live(),new AskStd(),new News()};
        viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager(),frags,getResources().getStringArray(R.array.student_tabs)));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tabLayout.getSelectedTabPosition());
            }
        });
        Init();
    }

    private void Init() {
        final String query="SELECT std_id, concat(std_fname ,' ', std_lname),std_major FROM student WHERE std_id='"+Id+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getResources().getString(R.string.ServerIp) + "/dbQuery.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr1=new JSONArray(response);
                    Data=new String[arr1.length()][arr1.getJSONArray(0).length()];
                    JSONArray arr2;
                    JSONObject object;
                    for(int i=0;i<arr1.length();i++){
                        arr2=arr1.getJSONArray(i);
                        for(int j=0;j<arr2.length();j++){
                            object=arr2.getJSONObject(j);
                            Data[i][j]=object.getString("col"+j);
                            InitSlide();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                db.MSG(getResources().getString(R.string.server_error));
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

    private void InitSlide() {
        UserName=Data[0][1];
        Major=Data[0][2];
        ((TextView)findViewById(R.id.std_header_id)).setText(Data[0][0]);
        ((TextView)findViewById(R.id.std_header_title)).setText(Data[0][1]);
        ((TextView)findViewById(R.id.std_header_major)).setText(Data[0][2]);
        imageView=(ImageView)findViewById(R.id.imageView);
        StorageReference image_root= FirebaseStorage.getInstance().getReference().child("pictures").child(StudentActivity.Id+"");
        image_root.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(StudentActivity.this).load(uri).into(imageView);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.std_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.gpa){
            Intent i=new Intent(StudentActivity.this,GPAActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id==R.id.home){
            ((FrameLayout)findViewById(R.id.minor_layout)).setVisibility(View.INVISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.VISIBLE);
        }
        else if(id==R.id.profile){
            ((FrameLayout)findViewById(R.id.minor_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.minor_layout,new ProfileStd()).commit();
        }
        else if(id==R.id.singout){
            SharedPreferences sharedPreferences=getSharedPreferences("userinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.clear().commit();
            startActivity(new Intent(StudentActivity.this,LoginActivity.class));
            this.finish();
        }
        else if(id==R.id.marks){
            ((FrameLayout)findViewById(R.id.minor_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.minor_layout,new Marks()).commit();
        }
        else if(id==R.id.exams){
            ((FrameLayout)findViewById(R.id.minor_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.minor_layout,new Exams()).commit();
        }else if(id==R.id.assignment){
            ((FrameLayout)findViewById(R.id.minor_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.minor_layout,new AssignmentStd()).commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
