package android.meraki.speedpost;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    //Fields for the Inputs
    private EditText display_name;
    private EditText email_id;
    private EditText pass_word;
    private Button reg_Button;

    //FireBase Part
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    //Toolbar
    private android.support.v7.widget.Toolbar  mToolbar;

    //Progress Bar
    private ProgressDialog mProgressBar;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();



        //Bindings
        display_name = findViewById(R.id.reg_dispplayname);
        email_id = findViewById(R.id.login_email);
        pass_word = findViewById(R.id.login_password);
        reg_Button = findViewById(R.id.reg_create_userbtn);

        //Toolbar Binding
        mToolbar=findViewById(R.id.settings_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progressbar
        mProgressBar = new ProgressDialog(this);


        reg_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = display_name.getText().toString();
                String email = email_id.getText().toString();
                String password = pass_word.getText().toString();

                if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))
                {
                    mProgressBar.setTitle("Registering User");
                    mProgressBar.setMessage("Please wait while we create your Account");
                    mProgressBar.setCanceledOnTouchOutside(true);
                    mProgressBar.show();

                    register_user(displayName,email,password);
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "Please Fill all the details",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void register_user(final String displayName, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String, String> UserMap= new HashMap<>();
                            UserMap.put("name",displayName);
                            UserMap.put("status","Hey there! I am using Speed Post app.");
                            UserMap.put("image","profile_pic");
                            UserMap.put("thumb_image","profile_pic");
                            mDatabase.setValue(UserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mProgressBar.dismiss();

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            mProgressBar.hide();
                            Toast.makeText(RegisterActivity.this, "Can not Create the Account. Please Fill in the details correctly",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }}
