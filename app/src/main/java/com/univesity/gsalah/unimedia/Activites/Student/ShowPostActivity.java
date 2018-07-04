package com.univesity.gsalah.unimedia.Activites.Student;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.univesity.gsalah.unimedia.Models.Post_Class;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowPostActivity extends AppCompatActivity {

    private DateFormat format=new SimpleDateFormat("hh:mm.a dd/MM");
    private Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Post_Class post= (Post_Class) getIntent().getSerializableExtra("post");
        TextView title= (TextView) findViewById(R.id.title_txt);
        TextView date= (TextView) findViewById(R.id.date_txt);
        TextView content= (TextView) findViewById(R.id.content_txt);
        ImageView imageView=(ImageView)findViewById(R.id.imageView);
        title.setText(post.getTitle());
        date.setText(format.format(new Date(post.getDate())));
        content.setText(post.getContent());
        byte[] arr=getIntent().getExtras().getByteArray("image");
        Bitmap image= BitmapFactory.decodeByteArray(arr,0,arr.length);
        imageView.setImageBitmap(image);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
