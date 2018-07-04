package com.univesity.gsalah.unimedia.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.univesity.gsalah.unimedia.Activites.Student.ResStdDataActivity;
import com.univesity.gsalah.unimedia.Models.Course_Class;
import com.univesity.gsalah.unimedia.R;

import java.util.LinkedList;


public class Time_Table_Adapter extends BaseAdapter{
    private Context context;
    private LinkedList<Course_Class>Data=new LinkedList<>();
    private Button res_btn;
    public Time_Table_Adapter(Context context, LinkedList<Course_Class> data) {
        this.context = context;
        Data = data;
    }

    @Override
    public int getCount() {
        return Data.size();
    }
    @Override
    public Object getItem(int i) {
        return Data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view=LayoutInflater.from(context).inflate(R.layout.time_table_cardview,viewGroup,false);
        }
        final Course_Class d=Data.get(i);

        TextView course_name=(TextView)view.findViewById(R.id.course_name);
        course_name.setText(d.getCourse_name());
        TextView course_time=(TextView)view.findViewById(R.id.course_time);
        course_time.setText(d.getCourse_time());
        TextView course_days=(TextView)view.findViewById(R.id.course_days);
        course_days.setText(d.getCourse_days());
        TextView course_class=(TextView)view.findViewById(R.id.course_class);
        course_class.setText(d.getCourse_class());
        TextView course_lec=(TextView)view.findViewById(R.id.course_lec);
        course_lec.setText(d.getCourse_lec());
        res_btn=(Button)view.findViewById(R.id.res_btn);
        res_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, ResStdDataActivity.class);
                intent.putExtra("session_id",d.getSession_id()+"");
                intent.putExtra("course_name",d.getCourse_name());
                context.startActivity(intent);
            }
        });
        Button eval_btn=(Button)view.findViewById(R.id.eval_btn);
        //Eval Button Click Event Here
        return view;
    }


}
