package com.univesity.gsalah.unimedia.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.univesity.gsalah.unimedia.Activites.Student.StudentActivity;
import com.univesity.gsalah.unimedia.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;



public class AddStdAdapter extends BaseAdapter {
    ArrayList<String>ids;
    ArrayList<String>names;
    Context context;
    TextView []txts;
    ImageView [] imageViews;
    public AddStdAdapter(ArrayList<String> ids, ArrayList<String> names, Context context) {
        this.ids = ids;
        this.names = names;
        this.context = context;
        txts=new TextView[ids.size()];
        imageViews=new ImageView[ids.size()];
    }

    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public Object getItem(int i) {
        return names.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        view= LayoutInflater.from(context).inflate(R.layout.add_std_list,viewGroup,false);
        final ImageView imageView=(ImageView)view.findViewById(R.id.std_pic_imageview);
        TextView txt=(TextView)view.findViewById(R.id.std_name_txt);
        txt.setText(names.get(i));
        txts[i]=txt;
        imageViews[i]=imageView;
        StorageReference image_root= FirebaseStorage.getInstance().getReference().child("pictures").child(ids.get(i));
        image_root.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).into(imageView);

            }
        });
        return view;
    }
    public void Selected(int i){
        txts[i].setBackgroundColor(Color.DKGRAY);
        imageViews[i].setBackgroundColor(Color.DKGRAY);
    }
}
