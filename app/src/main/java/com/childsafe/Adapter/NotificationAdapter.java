package com.childsafe.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.childsafe.Model.Notification;
import com.childsafe.R;

import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    public NotificationAdapter(Context context, ArrayList<Notification> notifications){
        super(context, R.layout.notification_list_item,notifications);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Notification notification = getItem(position);
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification_list_item,parent,false);
        }

        TextView msg = convertView.findViewById(R.id.noti_item_msg);
        TextView date = convertView.findViewById(R.id.noti_item_date);

        msg.setText(notification.getMessage());
        date.setText(notification.getDate());

        return convertView;
    }
}
