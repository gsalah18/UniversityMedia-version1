package com.univesity.gsalah.unimedia.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Models.Std_Class;
import com.univesity.gsalah.unimedia.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class Courses_Adapter extends BaseAdapter {
    private Context context;
    private LinkedList<Std_Class>Data;
    private String SessionId;
    private String query="";
    public Courses_Adapter(Context context, LinkedList<Std_Class> data,String SessionId) {
        this.context = context;
        Data = data;
        this.SessionId=SessionId;
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view==null)
            view= LayoutInflater.from(context).inflate(R.layout.courses_cardview,viewGroup,false);
        TextView std_id=(TextView)view.findViewById(R.id.std_id);
        ToggleButton TA=(ToggleButton)view.findViewById(R.id.ta_btn);
        std_id.setText(Data.get(i).getStd_id()+"");
        TextView std_name=(TextView)view.findViewById(R.id.std_name);
        std_name.setText(Data.get(i).getStd_name());
        TA.setChecked(Data.get(i).isTA());
        TA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    query="UPDATE time_table set ta='yes' WHERE session_id='"+SessionId+"' and std_id='"+Data.get(i).getStd_id()+"'";
                }else if(!b){
                    query="UPDATE time_table set ta='no' WHERE session_id='"+SessionId+"' and std_id='"+Data.get(i).getStd_id()+"'";
                }
                StringRequest stringRequest=new StringRequest(Request.Method.POST, context.getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String>map=new HashMap<>();
                        map.put("query",query);
                        return map;
                    }
                };
                Singlton.getInstance(context).AddtoRequest(stringRequest);
            }
        });
        return view;
    }
}
