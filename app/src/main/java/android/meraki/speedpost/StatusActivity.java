package android.meraki.speedpost;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveButton;
    private ProgressDialog mProgressBar;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mStatus = findViewById(R.id.status_input);
        mSaveButton = findViewById(R.id.status_change_button);

        String status_value = getIntent().getStringExtra("status_value");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String current_uid =mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar = findViewById(R.id.status_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus.getEditText().setText(status_value);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressBar = new ProgressDialog(StatusActivity.this);
                mProgressBar.setTitle("Saving Changes");
                mProgressBar.setMessage("Please Wait While We Save Your Changes.");
                mProgressBar.show();

                String status = mStatus.getEditText().getText().toString().trim();

                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            mProgressBar.dismiss();
                            Intent back = new Intent(StatusActivity.this,SettingsActivity.class);
                            startActivity(back);
                            Toast.makeText(StatusActivity.this,"Status Changed",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            mProgressBar.hide();
                            Toast.makeText(StatusActivity.this,"Something’s not right.!",Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });



    }
}
