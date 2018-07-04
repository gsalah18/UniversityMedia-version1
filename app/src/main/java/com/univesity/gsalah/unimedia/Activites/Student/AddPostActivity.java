package com.univesity.gsalah.unimedia.Activites.Student;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.univesity.gsalah.unimedia.Activites.ImageActivity;
import com.univesity.gsalah.unimedia.Models.Post_Class;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    private int PICK_IMAGE=1;
    private int TAKE_IMAGE=2;
    private EditText title_etxt,content_etxt;
    private Bitmap thumbnail;
    private String Id;
    private ProgressDialog progressDialog;
    private Context context=this;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        title_etxt=(EditText)findViewById(R.id.title_etxt);
        content_etxt=(EditText)findViewById(R.id.content_etxt);
        imageView=(ImageView) findViewById(R.id.imageView);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    public void AddClick(View view) {
        if(title_etxt.getText().length()>0&&content_etxt.getText().length()>0){
            long date=new Date().getTime();
            String title=title_etxt.getText().toString();
            String content=content_etxt.getText().toString();
            DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("posts");
            Id=database.push().getKey();
            database.updateChildren(new HashMap<String, Object>());
            DatabaseReference postDB=database.child(Id);
            postDB.updateChildren(new Post_Class(Id,title,content,date).toMap());
            //Adding the Picture
            if(thumbnail!=null){
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG,100,stream);
                byte []arr=stream.toByteArray();
                progressDialog.setMessage(getResources().getString(R.string.uploading));
                progressDialog.setCancelable(false);
                progressDialog.show();
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("posts").child(Id);
                storageReference.putBytes(arr).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(context, getResources().getString(R.string.pic_added), Toast.LENGTH_LONG).show();
                        //Uri downloadUri=taskSnapshot.getDownloadUrl();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context,getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    public void AddPhotoClick(View view) {
        AlertDialog.Builder adb=new AlertDialog.Builder(context);
        adb.setItems(new String[]{getResources().getString(R.string.take_photo), getResources().getString(R.string.pic_photo_from_gallery)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, TAKE_IMAGE);
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            }
        });
        adb.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==PICK_IMAGE){
                int ThubNailSize=256;
                String selectedImagePath=getAbsolutePath(data.getData());
                //imageView.setImageBitmap(decodeFile(selectedImagePath));
                thumbnail= ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(selectedImagePath),ThubNailSize,ThubNailSize);
                imageView.setImageBitmap(thumbnail);
            }else if(requestCode==TAKE_IMAGE){
                Uri uri=data.getData();
                final int Size=256;
                String selectImagePath=getAbsolutePath(uri);
                thumbnail= ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(selectImagePath),Size,Size);
                imageView.setImageBitmap(thumbnail);

            }else super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public String getAbsolutePath(Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
}
