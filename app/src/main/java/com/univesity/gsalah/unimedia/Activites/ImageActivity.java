package com.univesity.gsalah.unimedia.Activites;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.univesity.gsalah.unimedia.R;

public class ImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ImageView imageView = (ImageView) findViewById(R.id.full_image);
        byte[] arr=getIntent().getExtras().getByteArray("image");
        Bitmap image= BitmapFactory.decodeByteArray(arr,0,arr.length);
        imageView.setImageBitmap(image);
    }

    public void BackArrowClick(View view) {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
