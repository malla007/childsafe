package com.childsafe.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.childsafe.Model.MyLocation;
import com.childsafe.R;

import java.util.ArrayList;

public class LocationAdapter extends ArrayAdapter<MyLocation> {

    public LocationAdapter(Context context, ArrayList<MyLocation> myLocations){
        super(context, R.layout.location_list_item,myLocations);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        MyLocation location = getItem(position);
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_list_item,parent,false);
        }

        TextView childName = convertView.findViewById(R.id.child_item_name);

        childName.setText(location.getName());

        return convertView;
    }
}
