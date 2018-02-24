package android.meraki.speedpost;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private CircleImageView imageView;
    private TextView mUsername;
    private TextView mUserstatus;

    private Button mChange_image_Btn;
    private Button mChange_status_Btn;

    private static final int GALLERY_PICK = 1;

    //Storage References
    private StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Setting up the instances of the contents of the layout
        imageView = (CircleImageView)findViewById(R.id.settings_image);
        mUsername = findViewById(R.id.settings_displayname);
        mUserstatus = findViewById(R.id.settings_user_status);
        mChange_image_Btn = findViewById(R.id.settings_changeimage);
        mChange_status_Btn = findViewById(R.id.settings_changestatus);


        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
               // Toast.makeText(SettingsActivity.this,dataSnapshot.toString(), Toast.LENGTH_LONG).show();
                mUsername.setText(name);
                mUserstatus.setText(status);
                if(!image.equals("default"))
                    {
                        Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.profile_pic).into(imageView);
                    }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChange_status_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mUserstatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);
            }
        });


        mChange_image_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery,"SELECT IMAGE"),GALLERY_PICK);*/

                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity().setAspectRatio(1,1).setGuidelines(CropImageView.Guidelines.ON).start(SettingsActivity.this);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {


                final ProgressDialog mProgressBar = new ProgressDialog(SettingsActivity.this);
                mProgressBar.setTitle("Uploading Image.");
                mProgressBar.setMessage("Please Wait While We Save Your Changes.");
                mProgressBar.setCanceledOnTouchOutside(false);
                mProgressBar.show();
                
                
                Uri resultUri = result.getUri();

                File thumb_file = new File(resultUri.getPath());

                String current_uid = mCurrentUser.getUid();
                Bitmap thumb_bitmap = null;

                try {
                     thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath = mImageStorage.child("profile_images").child(current_uid+".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumb_image").child(current_uid+".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            final String download_url = task.getResult().getDownloadUrl().toString().trim();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString().trim();
                                    if(thumb_task.isSuccessful())
                                    {
                                        Map update_HashMap = new HashMap<>();
                                        update_HashMap.put("image",download_url);
                                        update_HashMap.put("thumb_image",thumb_download_url);

                                        mUserDatabase.updateChildren(update_HashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mProgressBar.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Thumbnail Updated",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }

                                }
                            });
                            mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mProgressBar.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Profile Photo Updated",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(SettingsActivity.this, "Profile Photo not Updated",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Toast.makeText(SettingsActivity.this, "Profile Photo Updated",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            mProgressBar.dismiss();
                            Toast.makeText(SettingsActivity.this, "Profile Photo Not Updated, ERROR",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
//
//    public static String random() {
//        Random generator = new Random();
//        StringBuilder randomStringBuilder = new StringBuilder();
//        int randomLength = generator.nextInt(10);
//        char tempChar;
//        for (int i = 0; i < randomLength; i++){
//            tempChar = (char) (generator.nextInt(96) + 32);
//            randomStringBuilder.append(tempChar);
//        }
//        return randomStringBuilder.toString();
//    }
}
