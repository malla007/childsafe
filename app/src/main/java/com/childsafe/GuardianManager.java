package com.childsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.childsafe.Adapter.GuardianAdapter;
import com.childsafe.Model.Users;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuardianManager extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listView;
    private FloatingActionButton floatingActionButton;
    private Dialog dialog;
    private EditText emailText;
    private Button addBtn,cancelBtn;
    private SharedPreferences sp;
    private ProgressBar progressBar;
    private DatabaseReference reference;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private List<String> connections;
    private ArrayList<Users> guardians;
    private String email, userId, parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_manager);

        toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.guardians_list);
        floatingActionButton = findViewById(R.id.floating_button);

        reference = database.getReference("Users");
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();

        dialog = new Dialog(this);

        toolbar.setTitle("Child Tracker");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                connections = (List<String>) snapshot.child("connections").getValue();
                readItemData(value -> {
                    GuardianAdapter guardianAdapter = new GuardianAdapter(GuardianManager.this,value);
                    listView.setAdapter(guardianAdapter);
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GuardianManager.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void openDialog() {

        dialog.setContentView(R.layout.create_guardian_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emailText = dialog.findViewById(R.id.parent_email_edit_dialog);
        addBtn = dialog.findViewById(R.id.add_btn_dialog);
        cancelBtn = dialog.findViewById(R.id.cancel_btn_dialog);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parentEmail = emailText.getText().toString().trim();
                if (parentEmail.isEmpty()) {
                    emailText.setError("Email is required");
                    emailText.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(parentEmail).matches()) {
                    emailText.setError("Please provide valid email");
                    emailText.requestFocus();
                    return;
                }

                if (parentEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    emailText.setError("You entered have your own email.");
                    emailText.requestFocus();
                    return;
                }

               for(Users user:guardians){
                   if (user.getEmail().equals(parentEmail)) {
                       emailText.setError("Guardian already exists.");
                       emailText.requestFocus();
                       return;
                   }
               }

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
                            System.out.println(map.get("mode"));

                            if (map.get("mode").equals("parent")) {
                                databaseUpdate(parentId);
                            } else {
                                alert("This email is registered as a Child. Cannot add a child account as your guardian.");
                            }
                        } else {
                            alert("This email is not registered. Try again..");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(GuardianManager.this, "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public void readItemData(GuardianManager.DataCallback myCallback) {

        guardians = new ArrayList<>();
        for(String item:connections){
            reference.child(item).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name,email,mobile;
                    name = snapshot.child("name").getValue().toString();
                    email = snapshot.child("email").getValue().toString();
                    mobile = snapshot.child("mobile").getValue().toString();
                    guardians.add(new Users(name,email,mobile));
                   if(item == connections.get(connections.size()-1)){
                        myCallback.onCallback(guardians);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(GuardianManager.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void databaseUpdate(String parentId) {

        readData(value -> {
            System.out.println("Last");

            reference.child(userId).child("connections").setValue(connections);
            readItemData(items -> {
                GuardianAdapter guardianAdapter = new GuardianAdapter(GuardianManager.this,items);
                listView.setAdapter(guardianAdapter);
            });
            dialog.dismiss();
            Toast.makeText(GuardianManager.this,"Guardian added successfully", Toast.LENGTH_LONG).show();

        });

    }

    public void readData(GuardianManager.MyCallback myCallback) {

        reference.child(parentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> children;

                if (snapshot.child("connections").exists()) {
                    children = (List<String>) snapshot.child("connections").getValue();
                }else{
                    children = new ArrayList<>();
                }

                children.add(userId);

                reference.child(parentId).child("connections").setValue(children);
                connections.add(parentId);
                myCallback.onCallback(children);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GuardianManager.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
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
            Intent intent = new Intent(GuardianManager.this, ModeSelection.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void alert(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(GuardianManager.this)
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

    public interface DataCallback {
        void onCallback(ArrayList<Users> value);
    }

    public interface MyCallback {
        void onCallback(List<String> value);
    }

}