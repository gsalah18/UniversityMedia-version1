package com.univesity.gsalah.unimedia.Fragments.Student;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Databases.Singlton;
import com.univesity.gsalah.unimedia.Manifest;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;


public class ProfileStd extends Fragment implements Button.OnClickListener{



    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private Button profile_pic;
    private ImageView imageView;
    private Button ChangePassBtn;
    private EditText OldPassword;
    private EditText NewPassword;
    private EditText ConfirmNewPassword;
    private ServerDB database;
    private StorageReference root;
    private ProgressDialog progressDialog;
    Bitmap thumbnail;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_profile_std, container, false);
        database=new ServerDB(getActivity());
        ChangePassBtn=(Button)v.findViewById(R.id.changepass_btn);
        ChangePassBtn.setOnClickListener(this);
        imageView=(ImageView)v.findViewById(R.id.profile_pic);
        profile_pic=(Button) v.findViewById(R.id.addphoto_btn);
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
                adb.setItems(new String[]{getResources().getString(R.string.take_photo), getResources().getString(R.string.pic_photo_from_gallery)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }else{
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_PICK);
                        }
                    }
                });
                adb.show();

            }
        });
        OldPassword=(EditText)v.findViewById(R.id.old_pass);
        NewPassword=(EditText)v.findViewById(R.id.new_pass);
        ConfirmNewPassword=(EditText)v.findViewById(R.id.confirm_new_pass);
        root= FirebaseStorage.getInstance().getReference();
        StorageReference image_root=root.child("pictures").child(StudentActivity.Id+"");
        image_root.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getActivity()).load(uri).into(imageView);
            }
        });
        progressDialog=new ProgressDialog(getActivity());
        return v;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri uri=data.getData();
            final int Size=256;
            String selectImagePath=getAbsolutePath(uri);
            thumbnail= ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(selectImagePath),Size,Size);
            imageView.setImageBitmap(thumbnail);
            InsertBitmap(thumbnail);
        }
        if(requestCode==REQUEST_IMAGE_PICK&&resultCode==RESULT_OK){
            int Size=256;
            String selectedImagePath=getAbsolutePath(data.getData());
            //imageView.setImageBitmap(decodeFile(selectedImagePath));
            thumbnail=ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(selectedImagePath),Size,Size);
            imageView.setImageBitmap(thumbnail);
            InsertBitmap(thumbnail);
        }
    }

    public String getAbsolutePath(Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
    private void InsertUri(Uri image){
        progressDialog.setMessage(getResources().getString(R.string.uploading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        StorageReference storageReference=root.child("pictures").child(StudentActivity.Id+"");
        storageReference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), getResources().getString(R.string.pic_added), Toast.LENGTH_LONG).show();
                Uri downloadUri=taskSnapshot.getDownloadUrl();
                Picasso.with(getActivity()).load(downloadUri).into(imageView);
                Picasso.with(getActivity()).load(downloadUri).into(StudentActivity.imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void InsertBitmap(Bitmap image){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte []arr=stream.toByteArray();
        progressDialog.setMessage(getResources().getString(R.string.uploading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        StorageReference storageReference=root.child("pictures").child(StudentActivity.Id+"");
        storageReference.putBytes(arr).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), getResources().getString(R.string.pic_added), Toast.LENGTH_LONG).show();
                Uri downloadUri=taskSnapshot.getDownloadUrl();
                Picasso.with(getActivity()).load(downloadUri).into(imageView);
                Picasso.with(getActivity()).load(downloadUri).into(StudentActivity.imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view==ChangePassBtn){
            //database.MSG(StudentActivity.Password);
            if(OldPassword.getText().toString().equals(StudentActivity.Password)){
                if(NewPassword.getText().toString().length()>0
                        &&NewPassword.getText().toString().equals(ConfirmNewPassword.getText().toString())){
                    ChangePassword(NewPassword.getText().toString());
                }else database.MSG(getResources().getString(R.string.old_pass_mismatch));
            }else database.MSG(getResources().getString(R.string.old_pass_wrong));
        }
    }


    private void ChangePassword(String newPassword){
        final String query="UPDATE student SET std_password='"+newPassword+"' WHERE std_id='"+StudentActivity.Id+"'";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, getActivity().getResources().getString(R.string.ServerIp) + "/dbAction.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), getResources().getString(R.string.pass_changed), Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                database.MSG(error+"");
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
