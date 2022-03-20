package com.childsafe.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.childsafe.R;
import com.childsafe.Model.Users;

import java.util.ArrayList;

public class GuardianAdapter extends ArrayAdapter<Users> {

    public GuardianAdapter(Context context, ArrayList<Users> guardians){
        super(context, R.layout.guardian_list_item,guardians);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Users guardian = getItem(position);
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.guardian_list_item,parent,false);
        }

        TextView parentName = convertView.findViewById(R.id.parent_item_name);
        TextView parentMobile = convertView.findViewById(R.id.parent_item_number);
        TextView parentEmail = convertView.findViewById(R.id.parent_item_email);

       parentEmail.setText(guardian.getEmail());
       parentMobile.setText(guardian.getMobile());
       parentName.setText(guardian.getName());

        return convertView;
    }
}
