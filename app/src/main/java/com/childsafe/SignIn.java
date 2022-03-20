package com.childsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.childsafe.Notification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailTxt, passwordTxt;
    private ProgressBar progressBar;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private SharedPreferences sp;
    private String modes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        emailTxt = findViewById(R.id.email_edit_text);
        passwordTxt = findViewById(R.id.password_edit_text);
        progressBar = findViewById(R.id.progress_bar);


        Intent intent = getIntent();
        modes = intent.getStringExtra("mode");
        System.out.println(modes);

        sp = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
    }

    public void goToSignUp(View view) {
        Intent intent = new Intent(SignIn.this, SignUp.class);
        intent.putExtra("mode", modes);
        startActivity(intent);
        finish();
    }

    public void onSignIn(View view) {

        String email = emailTxt.getText().toString().trim();
        String password = passwordTxt.getText().toString().trim();

        if (email.isEmpty()) {
            emailTxt.setError("Email is required");
            emailTxt.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTxt.setError("Please provide valid email");
            emailTxt.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordTxt.setError("Password is required");
            passwordTxt.requestFocus();
            return;
        }

        if (password.length() < 8) {
            passwordTxt.setError("Password must have at least 8 characters");
            passwordTxt.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            firebaseUser = mAuth.getCurrentUser();
                            reference = FirebaseDatabase.getInstance().getReference("Users");

                            reference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {

                                    String mode = snapshot.child("mode").getValue().toString();

                                    SharedPreferences.Editor editor = sp.edit();

                                    editor.putString("user_id", firebaseUser.getUid());
                                    editor.putString("name", snapshot.child("name").getValue().toString());
                                    editor.putString("mode", mode);
                                    editor.putString("email", snapshot.child("email").getValue().toString());

                                    editor.commit();

                                    Intent intent = null;
                                    updateToken();
                                    if (mode.equals("parent")) {
                                        intent = new Intent(SignIn.this, ParentTrackerHome.class);
                                    } else {
                                        if (snapshot.child("connections").exists()) {
                                            intent = new Intent(SignIn.this, ChildTrackerHome.class);
                                        } else {
                                            intent = new Intent(SignIn.this, ChildTrackerSelection.class);
                                        }
                                    }
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Toast.makeText(SignIn.this, "You are signed in as a " + mode, Toast.LENGTH_LONG).show();
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Toast.makeText(SignIn.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });

                        } else {
                            Toast.makeText(SignIn.this, "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void updateToken() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Token token = new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
}