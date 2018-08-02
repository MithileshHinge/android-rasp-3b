package com.example.app1;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by isha sagote on 30-07-2017.
 */
public class Bookmarkedimages extends Fragment {
    @Nullable
    private Context context;
    //public static ArrayList<ImageModel> data = new ArrayList<>();
    private static ArrayList<File> checkEntry = new ArrayList<>();
    //File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory("MagicEye"), "MagicEyePictures");

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_fragment,container,false);
        context = getContext();

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.gallery_list);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setHasFixedSize(true);

        ImageGalleryAdapter adapter = new ImageGalleryAdapter(context, ImageGalleryAdapter.bkmrkImages);
        adapter.classSelector = 3;
        adapter.imageRecyclerView = recyclerView;
        recyclerView.setAdapter(adapter);
        return v;

    }

}
