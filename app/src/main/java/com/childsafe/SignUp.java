package com.childsafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.childsafe.Model.Users;
import com.childsafe.Notification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText nameTxt, emailTxt, mobileTxt, passwordTxt, conPasswordTxt;
    private ProgressBar progressBar;
    private String mode;
    private List<String> connections;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        connections = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();

        nameTxt = findViewById(R.id.name_reg_edit_text);
        emailTxt = findViewById(R.id.email_reg_edit_text);
        mobileTxt = findViewById(R.id.mobile_reg_edit_text);
        passwordTxt = findViewById(R.id.password_reg_edit_text);
        conPasswordTxt = findViewById(R.id.confirm_password_reg_edit_text);
        progressBar = findViewById(R.id.progress_bar);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        System.out.println(mode);

        sp = getSharedPreferences("UserPref", Context.MODE_PRIVATE);

    }

    public void goToSignIn(View view) {
        Intent intent = new Intent(SignUp.this, SignIn.class);
        intent.putExtra("mode", mode);
        startActivity(intent);
        finish();
    }

    public void onSignUp(View view) {
        String name = nameTxt.getText().toString().trim();
        String email = emailTxt.getText().toString().trim();
        String mobile = mobileTxt.getText().toString().trim();
        String password = passwordTxt.getText().toString().trim();
        String conPassword = conPasswordTxt.getText().toString().trim();

        if (name.isEmpty()) {
            nameTxt.setError("Name is required");
            nameTxt.requestFocus();
            return;
        }

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

        if (mobile.isEmpty()) {
            mobileTxt.setError("Mobile number is required");
            mobileTxt.requestFocus();
            return;
        }

        if (mobile.length() != 10) {
            mobileTxt.setError("Mobile number is limited to 10 characters");
            mobileTxt.requestFocus();
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

        if (conPassword.isEmpty()) {
            conPasswordTxt.setError("Confirm Password is required");
            conPasswordTxt.requestFocus();
            return;
        }


        if (!conPassword.equals(password)) {
            conPasswordTxt.setError("Password doesn't match");
            conPasswordTxt.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Users users = new Users(name, mode, email, mobile, connections);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUp.this, "Registration is successful!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                        SharedPreferences.Editor editor = sp.edit();

                                        editor.putString("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        editor.putString("name", name);
                                        editor.putString("mode", mode);
                                        editor.putString("email", email);
                                        editor.commit();

                                        updateToken();

                                        Intent intent = null;
                                        if (mode.equals("parent")) {
                                            intent = new Intent(SignUp.this, ParentTrackerHome.class);
                                        } else {
                                            intent = new Intent(SignUp.this, ChildTrackerSelection.class);
                                        }
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignUp.this, "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SignUp.this, "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }
    private void updateToken(){
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
}