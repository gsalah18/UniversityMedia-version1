package com.univesity.gsalah.unimedia.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.univesity.gsalah.unimedia.Models.Question_Class;
import com.univesity.gsalah.unimedia.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;

/**
 * Created by GSALAH on 12/24/2017.
 */

public class AskAdapter extends BaseAdapter {
    private ArrayList<Question_Class>data;
    private Context context;
    private View v;
    private SimpleDateFormat fmt = new SimpleDateFormat("MM/dd HH:mm:ss");
    public AskAdapter(ArrayList<Question_Class> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Question_Class getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        v= LayoutInflater.from(context).inflate(R.layout.ask_list_layout,viewGroup,false);
        Question_Class q=getItem(i);
        TextView username_txt=(TextView)v.findViewById(R.id.username_txt);
        TextView question_txt=(TextView)v.findViewById(R.id.question_txt);
        TextView date_txt=(TextView)v.findViewById(R.id.date_txt);
        Date date=new Date(q.getDate());
        username_txt.setText(q.getUsername());
        question_txt.setText(q.getQuestion());
        date_txt.setText(fmt.format(date));

        return v;
    }
}
