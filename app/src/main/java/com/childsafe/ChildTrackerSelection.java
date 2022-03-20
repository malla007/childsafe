package com.childsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChildTrackerSelection extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText emailTxt;
    private TextView welcome;
    private SharedPreferences sp;
    private ProgressBar progressBar;
    private DatabaseReference reference;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    String name, email, userid, parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_tracker_selection);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Child Tracker");
        setSupportActionBar(toolbar);

        sp = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        userid = sp.getString("user_id", "");
        name = sp.getString("name", "");
        email = sp.getString("email", "");

        emailTxt = findViewById(R.id.parent_email_edit_text);
        progressBar = findViewById(R.id.progress_bar);
        welcome = findViewById(R.id.welcome_text_child);

        welcome.setText("Hi " + name + "!");
        reference = database.getReference("Users");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_menu_1, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ChildTrackerSelection.this, ModeSelection.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addParent(View view) {

        String parentEmail = emailTxt.getText().toString().trim();
        if (parentEmail.isEmpty()) {
            emailTxt.setError("Email is required");
            emailTxt.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(parentEmail).matches()) {
            emailTxt.setError("Please provide valid email");
            emailTxt.requestFocus();
            return;
        }

        if (parentEmail.equals(email)) {
            emailTxt.setError("You entered have your own email.");
            emailTxt.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Query query = reference.orderByChild("email").equalTo(parentEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    HashMap<String, Object> map = null;

                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        parentId = user.getKey();
                        map = (HashMap<String, Object>) user.getValue();
                        break;
                    }

                    if (map.get("mode").equals("parent")) {
                        databaseUpdate();
                    } else {
                        alert("This email is registered as a Child. Cannot add a child account as your guardian.");
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBar.setVisibility(View.GONE);
                        emailTxt.requestFocus();
                    }
                } else {
                    alert("This email is not registered. Try again..");
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.GONE);
                    emailTxt.requestFocus();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChildTrackerSelection.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.GONE);
                emailTxt.requestFocus();
            }
        });
    }

    private void databaseUpdate() {

        readData(value -> {

            reference.child(parentId).child("connections").setValue(value);

            value.clear();
            value.add(parentId);
            reference.child(userid).child("connections").setValue(value);

            Intent intent = new Intent(ChildTrackerSelection.this, ChildTrackerHome.class);
            startActivity(intent);
            finish();
        });

    }

    public void readData(MyCallback myCallback) {

        reference.child(parentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> connections;

                if (snapshot.child("connections").exists()) {
                    connections = (List<String>) snapshot.child("connections").getValue();
                }else{
                    connections = new ArrayList<>();
                }
                connections.add(userid);
                myCallback.onCallback(connections);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChildTrackerSelection.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.GONE);
                emailTxt.requestFocus();
            }
        });
    }

    public interface MyCallback {
        void onCallback(List<String> value);
    }

    private void alert(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(ChildTrackerSelection.this)
                .setTitle("Oops!")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }
}