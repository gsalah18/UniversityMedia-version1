package com.univesity.gsalah.unimedia.Activites.Lecturer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.LoginActivity;
import com.univesity.gsalah.unimedia.Adapters.FragmentsAdapter;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.AskLec;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.AssignmentLec;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.Courses;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.ExamsLec;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.MarksLec;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.ProfileLec;
import com.univesity.gsalah.unimedia.Fragments.Lecturer.ResourcesLec;
import com.univesity.gsalah.unimedia.Fragments.Student.Exams;
import com.univesity.gsalah.unimedia.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LecturerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    public static int Id;
    public static String Password;
    private ServerDB db;
    private String[][] Data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db=new ServerDB(this);
        Id=getIntent().getExtras().getInt("ID");
        Password=getIntent().getExtras().getString("Password");
        Init();

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        viewPager=(ViewPager)findViewById(R.id.viewpager);
        tabLayout=(TabLayout)findViewById(R.id.tablayout);
        Fragment []frags={new Courses(),new AskLec(),new AssignmentLec()};
        viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager(),frags,getResources().getStringArray(R.array.lecturer_tabs)));
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
    }

    private void Init() {
        final String query="SELECT lec_id, concat(lec_degree,'.',lec_fname ,' ', lec_lname) FROM lecturer WHERE lec_id='"+Id+"'";
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
                db.MSG(error+"");
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
        TextView v=(TextView)findViewById(R.id.lec_header_id);
        v.setText(Data[0][0]);
        ((TextView)findViewById(R.id.lec_header_title)).setText(Data[0][1]);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id==R.id.home){
            ((FrameLayout)findViewById(R.id.frame_layout)).setVisibility(View.INVISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.VISIBLE);
        }else if(id==R.id.singout){
            SharedPreferences sharedPreferences=getSharedPreferences("userinfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.clear().commit();
            startActivity(new Intent(LecturerActivity.this,LoginActivity.class));
            this.finish();
        }else if(id==R.id.exams){
            ((FrameLayout)findViewById(R.id.frame_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new ExamsLec()).commit();
        }else if(id==R.id.marks){
            ((FrameLayout)findViewById(R.id.frame_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new MarksLec()).commit();
        }else if(id==R.id.resources){
            ((FrameLayout)findViewById(R.id.frame_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new ResourcesLec()).commit();
        }
        else if(id==R.id.profile){
            ((FrameLayout)findViewById(R.id.frame_layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.main_layout)).setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new ProfileLec()).commit();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
