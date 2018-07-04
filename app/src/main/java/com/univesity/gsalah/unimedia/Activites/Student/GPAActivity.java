package com.univesity.gsalah.unimedia.Activites.Student;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.ViewAsserts;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.R;

import java.util.ArrayList;
import java.util.Calendar;

public class GPAActivity extends AppCompatActivity implements FloatingActionButton.OnClickListener{

    private LinearLayout BodyLayout;
    private ArrayList<View>Views=new ArrayList<>();
    private double Result;
    private double TermResult;
    private double TermHours=0;
    private double TermMarksSum=0;
    private double Hours=0;
    private double MarksSum=0;
    private EditText OldGPA;
    private EditText OldHours;
    private Button Calculate;
    private Button Clear;
    private ServerDB database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpa);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        database=new ServerDB(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            AddView();
            }
        });
        Calculate=(Button)findViewById(R.id.calculate_btn);
        Calculate.setOnClickListener(this);
        Clear=(Button)findViewById(R.id.clear_btn);
        Clear.setOnClickListener(this);
        OldGPA=(EditText)findViewById(R.id.oldgpa_etxt);
        OldHours=(EditText)findViewById(R.id.oldhours_etxt);
        BodyLayout=(LinearLayout)findViewById(R.id.body_layout);
        AddView();
        AddView();
        AddView();
    }

    private void AddView(){

        View view= LayoutInflater.from(this).inflate(R.layout.gpa_layout,null);
        BodyLayout.addView(view);
        Views.add(view);

    }
    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if(view==Clear)
            Clear();
        else {
            Calculating();
        }
    }
    private void Calculating(){
        Hours=0;
        MarksSum=0;
        if(OldHours.getText().toString().length()>0&&OldGPA.getText().toString().length()>0) {
            double oldgpa=Double.parseDouble(OldGPA.getText().toString());
            int oldhours=Integer.parseInt(OldHours.getText().toString());
            double oldtotal=oldgpa*oldhours;
            for (View v : Views) {
                EditText mark_etxt=(EditText)v.findViewById(R.id.mark_etxt);
                EditText oldmark_etxt=(EditText)v.findViewById(R.id.oldmark_etxt);
                Spinner hours_spin=(Spinner)v.findViewById(R.id.hour_spinner);
                ////////////////////////////////////////////////////////////////
                double mark=(mark_etxt.getText().length()>0)?Double.parseDouble(mark_etxt.getText().toString()):0;
                double oldmark=(oldmark_etxt.getText().length()>0)?Double.parseDouble(oldmark_etxt.getText().toString()):0;
                int hours=Integer.parseInt(hours_spin.getSelectedItem().toString());
                TermMarksSum+=(mark*hours);
                TermHours+=hours;
                if(oldmark_etxt.getText().toString().length()==0){
                    MarksSum+=(mark*hours);
                    Hours+=hours;
                }else if(oldgpa>0&&oldhours>0&&oldmark_etxt.getText().toString().length()>0){
                    oldtotal+=((mark-oldmark)*hours);
                }
            }
            TermResult=TermMarksSum/TermHours;
            Result=(MarksSum+oldtotal)/(Hours+oldhours);
            database.MSG(getResources().getString(R.string.semester_gpa)+TermResult+"\n"+getResources().getString(R.string.cumlative_gpa)+Result);
        }else database.MSG(getResources().getString(R.string.fill_all_infomarion)+"\n"+getResources().getString(R.string.freshmen_statment));
    }
    private void Clear(){
        OldGPA.setText("");
        OldHours.setText("");
        for(View v:Views){
            ((EditText)v.findViewById(R.id.mark_etxt)).setText("");
            ((EditText)v.findViewById(R.id.oldmark_etxt)).setText("");
            ((Spinner)v.findViewById(R.id.hour_spinner)).setSelection(0);
        }
    }
}
