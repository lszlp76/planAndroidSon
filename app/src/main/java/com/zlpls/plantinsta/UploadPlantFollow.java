package com.zlpls.plantinsta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.storage.StorageTask;
import com.zlpls.plantinsta.menulist.UserMenuList;
import com.zlpls.plantinsta.visualselection.FileOperations;
import com.zlpls.plantinsta.visualselection.VisualMainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UploadPlantFollow extends AppCompatActivity {
    ImageView addpageimageView;
    EditText commentText;
    EditText dateText;
    Bitmap selectedImage;
    Uri imageData;
    String plantMarkerforUpload, selectedImageFromUser;
    String currentPhotoPath;
    FloatingActionButton followfab;
    public FileOperations fileOperations = new FileOperations();
    BottomNavigationView bottomAppBar;
    private final UserActions userActions = new UserActions();
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference; // nereye neyi kay??t yapmak i??in kullan??lacak
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private final Handler handler = new Handler();
    private Object View;
    private StorageTask mUploadTask;// y??kleme tamamland?? kontrol??



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_plant_follow);
        //setupBottomAppBar();
        addpageimageView = findViewById(R.id.plantfollwimageview);
        commentText = findViewById(R.id.commenttext);
        /***Storage Aktif etme***********/
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
//        followfab = findViewById(R.id.floating_action_button);

        bottomAppBar =  findViewById(R.id.bottom_navigation);

        bottomAppBar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomAppBar.setItemIconTintList(null);

        Menu menu = bottomAppBar.getMenu();
        menu.findItem(R.id.photo).setIcon(R.drawable.ic__send);
        menu.findItem(R.id.photo).setTitle("Add New Page");

        //   Intent intent = getIntent();
        plantMarkerforUpload = userActions.getPlantNameToModify();//intent.getStringExtra("mark");
        System.out.println("saya?? "+userActions.getPostCountValue());// --> plant.get(index).getPlantPostCount() ??al??????r.burad
        System.out.println("ad?? "+userActions.getPlantNameToModify()); // sayac de??erini ekrana yazd??rmak
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("New page for "+plantMarkerforUpload);

        Intent intentf = getIntent();
        selectedImageFromUser = intentf.getStringExtra("selectedimagefromuser");
        // gelen resmi imageivew ekle
        Picasso.get()
                .load("file:" + selectedImageFromUser)
                .into(addpageimageView);
        File bitmapFile = new File(selectedImageFromUser);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        selectedImage = fileOperations.getStreamByteFromImage(bitmapFile);
        //selectedImage = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), bmOptions);
/*
followfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                upload();


                ;
            }
        });
 */

//TODO ********PROGRESS BAR******************************************
        progressBar = findViewById(R.id.followprogressBar);

        progressBar.setVisibility(android.view.View.INVISIBLE);

        // progressTextView.setText(progressStatus )+ "/" + progressBar.getMax());
        new Thread(new Runnable() {
            public void run() {

                while (progressStatus < 100) {

                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        }).start();

//*****************************************************************************

        // storageReference ile bir g??rsel nereye kay??t edebilece??imizi yapar??z

        //FeedActivity den t??klanan??nca gelen plantmark bilgisi


    System.out.println("Feed ad?? : "+ userActions.getPlantNameToModify());

    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {

        switch (item.getItemId()) {
            case R.id.home:
                Intent intentq = new Intent(UploadPlantFollow.this, PlantList.class);
                startActivity(intentq);
                break;
            case R.id.menu:
                Intent intentu = new Intent(UploadPlantFollow.this, UserMenuList.class);

                startActivity(intentu);
                break;
            case R.id.photo:
                if (mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(UploadPlantFollow.this,"Uploading is on going.",Toast.LENGTH_SHORT).show();
                }else {
                    upload();
                }
                break;
        }
        return false;
    };
    public void chooseImage ( View view ){

        Intent intent = new Intent(UploadPlantFollow.this, VisualMainActivity.class);

        startActivity(intent);
    }
    public void upload() {
        if (selectedImage != null) {



//https://firebase.google.com/docs/storage/android/upload-files    storage ile ilgili tum detaylar
//**** ??nce image byte a ??evir***********

            //selectedImage ?? k??????lt
            selectedImage = makeSmallerImage(selectedImage, 800);
            //**** ??nce image byte a ??evir***********
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            System.out.println("dosya boyutu : " + selectedImage.getByteCount());
            byte[] bytes = byteArrayOutputStream.toByteArray();

            //***************************************
            /* ID ver****************************************/
            UUID uuid = UUID.randomUUID();
            final String imagename = "images/" + uuid + ".jpg";
            // UploadTask uploadTask = storageReference.child("imagename").putBytes(bytes);
            mUploadTask = storageReference.child("images").child(uuid + ".jpg")
                    .putBytes(bytes)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {


                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // storage kay??t ba??ar??l?? oldu??unda yap??lacak ??ey veriyolunu almak
                    StorageReference restorageReference = FirebaseStorage.getInstance().getReference(imagename);// kay??t edilen dosyan??n yeri
                    //download URL yi almak
                    restorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        //veritaban??na yazma buradan olacak
                        public void onSuccess(Uri uri) {

                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            //userActions.setPostCountFor(getApplicationContext(),plantMarkerforUpload, "add","");
                            String downloadUrl = uri.toString();
                            String comment = commentText.getText().toString();

                            String plantinstauser = firebaseAuth.getCurrentUser().getEmail();
                            String path = plantinstauser;

                            /*Sayac?? artt??rmak */

                            int newCounter = userActions.getPostCountValue() + 1;
                            firebaseFirestore.collection(plantinstauser).document(plantMarkerforUpload)
                                    .update("plantPostCount", String.valueOf(newCounter))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            ;
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println("Error updating document");
                                        }
                                    });

                            /* Sayac?? artt??rma sonu */
                            /*****Gonderilecek paket hashmap**********************/
                            HashMap<String, Object> postData = new HashMap<>();
                            postData.put("image", downloadUrl);
                            postData.put("comment", comment);
                            postData.put("date", FieldValue.serverTimestamp());



                            firebaseFirestore.collection(plantinstauser)
                                    .document(plantMarkerforUpload)
                                    .collection("history")
                                    .add(postData).addOnSuccessListener(new OnSuccessListener
                                    <DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    Toast.makeText(UploadPlantFollow.this, "Added", Toast.LENGTH_LONG).show();


                                    Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                                    intent.putExtra("mark", userActions.getPlantNameToModify());
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();




                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("hata var");
                                    Toast.makeText(UploadPlantFollow.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });


                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");

                    progressBar.setProgress((int) progress);

                    progressBar.setVisibility(android.view.View.VISIBLE);
                    //progressTextView.setVisibility(android.view.View.VISIBLE);
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                }
            }).addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadPlantFollow.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

            });


        } else {
            Toast.makeText(getApplicationContext(), "Hatal?? ????lem", Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }


}

