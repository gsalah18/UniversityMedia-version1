package com.univesity.gsalah.unimedia.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.univesity.gsalah.unimedia.Activites.ImageActivity;
import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.Databases.ServerDB;
import com.univesity.gsalah.unimedia.Models.Chat_Class;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private ArrayList<Chat_Class> Data;
    private Context context;
    private String UserId;
    //private View v;
    private ServerDB db;
    private SimpleDateFormat fmt = new SimpleDateFormat("MM/dd hh:mm:ss.a");
    private HashMap<String,Uri>images=new HashMap<>();

    public ChatAdapter(ArrayList<Chat_Class> data, Context context, String UserId) {
        Data = data;
        this.context = context;
        this.UserId = UserId;
        db = new ServerDB(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=null;
        if(viewType==1){
            view=LayoutInflater.from(context).inflate(R.layout.user_msg_layout,parent,false);
        }else if(viewType==2){
            view=LayoutInflater.from(context).inflate(R.layout.friend_msg_layout,parent,false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Chat_Class c=Data.get(position);
        holder.username_txt.setText(c.getSender_username());
        holder.textcontent.setText(c.getMessage());
        holder.date_txt.setText(fmt.format(new Date(c.getDate())));
        if(images.get(c.getSender_id())==null) {
            StorageReference image_root = FirebaseStorage.getInstance().getReference().child("pictures").child(c.getSender_id());
            image_root.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri).into(holder.profile_image);
                    images.put(c.getSender_id(), uri);
                }
            });
        }else Picasso.with(context).load(images.get(c.getSender_id())).into(holder.profile_image);
        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Drawable drawable = holder.profile_image.getDrawable();
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
    }

    @Override
    public int getItemCount() {
        return Data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(Data.get(position).isUser(UserId))
            return 1;
        else return 2;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView username_txt,textcontent,date_txt;
        public ImageView profile_image;
        public MyViewHolder(View v){
            super(v);
            username_txt= (TextView) v.findViewById(R.id.username_txt);
            textcontent=(TextView) v.findViewById(R.id.textContent);
            date_txt=(TextView) v.findViewById(R.id.date_txt);
            profile_image=(ImageView) v.findViewById(R.id.profile_image);
        }
    }

}
