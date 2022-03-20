package com.childsafe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ModeSelection extends AppCompatActivity {
    CardView childCardView;
    CardView parentCardView;
    boolean selectedMode;
    String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);
        childCardView = findViewById(R.id.childCard);
        parentCardView = findViewById(R.id.parentCard);

    }

    public void onSelectChild(View view) {
        selectedMode = true;
        mode = "child";
        childCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
        parentCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_secondary));
    }

    public void onSelectParent(View view) {
        selectedMode = true;
        mode = "parent";
        childCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_secondary));
        parentCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
    }

    public void launchSignIn(View view) {
        if(selectedMode) {
            System.out.println("Parent       "+mode);
            Intent intent = new Intent(ModeSelection.this,SignIn.class);
            intent.putExtra("mode", mode);
            startActivity(intent);
        }else {
            alert();
        }
    }

    public void launchSignUp(View view) {
        if(selectedMode) {
            Intent intent = new Intent(ModeSelection.this, SignUp.class);
            intent.putExtra("mode", mode);
            startActivity(intent);
        }else {
            alert();
        }
    }

    private void alert(){
        AlertDialog dialog = new AlertDialog.Builder(ModeSelection.this)
                .setTitle("Oops!")
                .setMessage("Please select mode to continue")
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