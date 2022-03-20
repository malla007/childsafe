package com.childsafe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.childsafe.Adapter.LocationAdapter;
import com.childsafe.Model.MyLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ParentTrackerHome extends AppCompatActivity {

    private TextView welcome;
    private SharedPreferences sp;
    private ProgressBar progressBar;
    private DatabaseReference refUsers;
    private DatabaseReference refLocation;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Toolbar toolbar;
    private String name, userid;
    private ListView listView;
    private RelativeLayout emptyLayout;
    private ArrayList<MyLocation> locations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_tracker_home);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Parent Tracker");
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progress_bar);
        welcome = findViewById(R.id.welcome_text_child);
        emptyLayout = findViewById(R.id.empty_location_layout);
        listView = findViewById(R.id.location_list);


        sp = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        userid = sp.getString("user_id", "");
        name = sp.getString("name", "");
        welcome.setText("Hi " + name + "!");
        refUsers = database.getReference("Users");
        refLocation = database.getReference("locations");


        locations = new ArrayList<>();

        refUsers.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> connections;
                if(snapshot.child("connections").exists()) {
                    connections = (List<String>) snapshot.child("connections").getValue();
                    System.out.println(Arrays.toString(connections.toArray()));
                    addLocations(connections);
                }else{
                    emptyLayout.setVisibility(View.VISIBLE);
                }

            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ParentTrackerHome.this, ""+ error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addLocations(List<String> connections) {
        refLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    HashMap<String, Object> map = null;

                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        for (String item:connections){
                            if(user.getKey().equals(item)){
                                map = (HashMap<String, Object>) user.getValue();
                                locations.add(new MyLocation(item, (String) map.get("name")));
                            }
                        }
                    }

                    LocationAdapter locationAdapter = new LocationAdapter(ParentTrackerHome.this,locations);

                    listView.setAdapter(locationAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            System.out.println(locations.get(position).getLatitude());
                            Intent intent = new Intent(ParentTrackerHome.this,ParentTrackerLocation.class);
                            intent.putExtra("user_id", locations.get(position).getUserId());
                            intent.putExtra("name", locations.get(position).getName());
                            startActivity(intent);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ParentTrackerHome.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_menu_2,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ParentTrackerHome.this, ModeSelection.class);
            startActivity(intent);
            finish();
        }else if(item.getItemId() == R.id.notification){
            Intent intent = new Intent(ParentTrackerHome.this, ParentTrackerNotifications.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}