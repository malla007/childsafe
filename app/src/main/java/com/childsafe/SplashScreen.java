package com.childsafe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.childsafe.Notification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;

import java.util.HashMap;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        sp = getSharedPreferences("UserPref", Context.MODE_PRIVATE);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            System.out.println(currentUser.getEmail());

            reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Intent intent = null;
                    if(snapshot.exists()){
                        String mode = snapshot.child("mode").getValue().toString();

                        SharedPreferences.Editor editor = sp.edit();

                        editor.putString("user_id", currentUser.getUid());
                        System.out.println(snapshot.child("name").getValue().toString());
                        editor.putString("name", snapshot.child("name").getValue().toString());
                        editor.putString("mode", mode);
                        editor.putString("email", snapshot.child("email").getValue().toString());

                        editor.commit();
                        updateToken();

                        if(mode.equals("parent")){
                            intent = new Intent(SplashScreen.this,ParentTrackerHome.class);
                        }else{
                            if(snapshot.child("connections").exists()) {
                                intent = new Intent(SplashScreen.this, ChildTrackerHome.class);
                            }else{
                                intent = new Intent(SplashScreen.this, ChildTrackerSelection.class);
                            }
                        }
                    }else{
                        intent = new Intent(SplashScreen.this,ModeSelection.class);
                    }
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });

        }else{
            new Handler().postDelayed(
                    () -> {
                        Intent intent = new Intent(SplashScreen.this,ModeSelection.class);
                        startActivity(intent);
                        finish();
                    },2000
            );
        }
    }

    private void updateToken(){
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
}
