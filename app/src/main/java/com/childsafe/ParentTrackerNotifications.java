package com.childsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.childsafe.Adapter.GuardianAdapter;
import com.childsafe.Adapter.NotificationAdapter;
import com.childsafe.Model.Notification;
import com.childsafe.Model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParentTrackerNotifications extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listView;
    private RelativeLayout relativeLayout;
    private DatabaseReference reference;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private String userId;
    private ArrayList<Notification> notifications;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_tracker_notifications);
        toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.notification_list);
        relativeLayout = findViewById(R.id.empty_notification_layout);

        reference = database.getReference("Notifications");
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        toolbar.setTitle("Parent Tracker");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        notifications = new ArrayList<Notification>();

        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Notification notificationItem;
                    for(DataSnapshot ds : snapshot.getChildren()) {
                        notificationItem = ds.getValue(Notification.class);
                        notifications.add(notificationItem);
                    }
                    NotificationAdapter notificationAdapter = new NotificationAdapter(ParentTrackerNotifications.this,notifications);
                    listView.setAdapter(notificationAdapter);
                }else{
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentTrackerNotifications.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }
}