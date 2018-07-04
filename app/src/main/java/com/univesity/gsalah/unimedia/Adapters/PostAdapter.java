package com.univesity.gsalah.unimedia.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.StorageException;
import com.univesity.gsalah.unimedia.Activites.ImageActivity;
import com.univesity.gsalah.unimedia.Activites.Student.ShowPostActivity;
import com.univesity.gsalah.unimedia.Models.Post_Class;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PostAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Post_Class>data;
    private HashMap<String,Uri>images=new HashMap<>();
    private DateFormat format=new SimpleDateFormat("hh:mm.a dd/MM");
    private View view;
    public PostAdapter(Context context, ArrayList<Post_Class> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if(view==null)
            view=LayoutInflater.from(context).inflate(R.layout.post_layout,viewGroup,false);
        final Post_Class post=data.get(position);
        TextView title= (TextView) view.findViewById(R.id.title_txt);
        TextView date= (TextView) view.findViewById(R.id.date_txt);
        final TextView content= (TextView) view.findViewById(R.id.content_txt);
        final ImageView imageView=(ImageView)view.findViewById(R.id.imageView);

        title.setText(post.getTitle());
        date.setText(format.format(new Date(post.getDate())));

        TextView more=(TextView)view.findViewById(R.id.more_txt);
        if(post.getContent().length()>100)
            content.setText(post.getContent().substring(0, 100) + "...");
        else {
            //more.setVisibility(View.INVISIBLE);
            content.setText(post.getContent());
        }

        if(images.get(post.getId())==null) {
            try {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                storageRef.child("posts/" + post.getId() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(context).load(uri).into(imageView);
                        images.put(post.getId(), uri);
                        imageView.buildDrawingCache();
                    }
                });
            }catch (Exception e){
                imageView.setVisibility(View.INVISIBLE);
            }
        }else Picasso.with(context).load(images.get(post.getId())).into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Drawable drawable = imageView.getDrawable();
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("image", imageInByte);
                    context.startActivity(intent);
                }catch (NullPointerException e){}
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Drawable drawable = imageView.getDrawable();
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();
                    Intent intent = new Intent(context, ShowPostActivity.class);
                    intent.putExtra("post", post);
                    intent.putExtra("image", imageInByte);
                    context.startActivity(intent);
                }catch (NullPointerException e){

                }
            }
        });
        return view;
    }

}
