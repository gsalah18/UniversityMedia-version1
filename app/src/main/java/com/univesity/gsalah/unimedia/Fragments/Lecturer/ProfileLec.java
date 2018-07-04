package com.univesity.gsalah.unimedia.Fragments.Lecturer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Lecturer.LecturerActivity;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileLec extends Fragment implements Button.OnClickListener{

    private ServerDB database;

    private Button ChangePassBtn;
    private EditText OldPassword;
    private EditText NewPassword;
    private EditText ConfirmNewPassword;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_lec, container, false);
        database=new ServerDB(getActivity());
        ChangePassBtn=(Button)v.findViewById(R.id.changepass_btn);
        ChangePassBtn.setOnClickListener(this);
        OldPassword=(EditText)v.findViewById(R.id.old_pass);
        NewPassword=(EditText)v.findViewById(R.id.new_pass);
        ConfirmNewPassword=(EditText)v.findViewById(R.id.confirm_new_pass);
        return v;
    }


    @Override
    public void onClick(View view) {
        if(view==ChangePassBtn){
            if(OldPassword.getText().toString().equals(LecturerActivity.Password)){
                if(NewPassword.getText().toString().length()>0
                        &&NewPassword.getText().toString().equals(ConfirmNewPassword.getText().toString())){
                    ChangePassword(NewPassword.getText().toString());
                }else database.MSG("The new password doesn't match");
            }else database.MSG("Old password is wrong");
        }
    }
    private void ChangePassword(String newPassword){
        final String query="UPDATE lecturer SET lec_password='"+newPassword+"' WHERE lec_id='"+ LecturerActivity.Id+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), getResources().getString(R.string.pass_changed), Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(getResources().getString(R.string.server_error));
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>map=new HashMap<>();
                map.put("query",query);
                return map;
            }
        };
        Singlton.getInstance(getActivity()).AddtoRequest(stringRequest);
    }
}
