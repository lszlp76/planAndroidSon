package com.zlpls.plantinsta;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.zlpls.plantinsta.menulist.UserMenuList;
import com.zlpls.plantinsta.visualselection.FileOperations;
import com.zlpls.plantinsta.visualselection.VisualMainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {
    public static ArrayList<Integer> postCount;
    RecyclerView recyclerView;
    Timestamp ts;
    TextView positionName;
    /*******bunlar firebaseden alacağımız verilerin yazılacağı diziler********/
    ArrayList<String> commentsFromFB;
    ArrayList<String> imagesFromFB;
    ArrayList<String> datesFromFB;
    ArrayList<String> idtodelFromFB;
    ArrayList<String> postCountFromFB;
    ArrayList<String> list;
    ArrayList<PlantModel> plant = new ArrayList<PlantModel>();
    BottomNavigationView bottomAppBar;
    /***********************************************************************/
    RecyclerAdapter recyclerAdapter;
    UserActions userActions = new UserActions();
    String plantinstauser;
    private String plantMarker;
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {


        switch (item.getItemId()) {
            case R.id.home:
                Intent intentq = new Intent(FeedActivity.this, PlantList.class);
                intentq.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentq);
                finish();
                break;
            case R.id.menu:
                Intent intentu = new Intent(FeedActivity.this, UserMenuList.class);

                startActivity(intentu);

                break;
            case R.id.photo:
                Intent intent = new Intent(FeedActivity.this, VisualMainActivity.class);
                intent.putExtra("fromList", 1);
                userActions.setPlantNameToModify(plantMarker);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();


                break;
        }
        return false;
    };
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Intent intent = getIntent();
        plantMarker = intent.getStringExtra("mark");
        //setupBottomAppBar();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        plantinstauser = firebaseAuth.getCurrentUser().getEmail();
        System.out.println("kullanıcı ..." + plantinstauser);
//        bottomAppBar = findViewById(R.id.bottomAppBar);
        commentsFromFB = new ArrayList<>();
        imagesFromFB = new ArrayList<>();
        datesFromFB = new ArrayList<>();
        idtodelFromFB = new ArrayList<>();


        recyclerView = findViewById(R.id.feedlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // recyclerAdapter = new RecyclerAdapter(commentsFromFB, imagesFromFB, datesFromFB);
        recyclerAdapter = new RecyclerAdapter(plant);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setHasFixedSize(true);
        positionName = findViewById(R.id.FeedActivity_Name);
        positionName.setText(plantMarker);
        bottomAppBar = findViewById(R.id.bottom_navigation);

        bottomAppBar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomAppBar.setItemIconTintList(null);
        ActionBar actionBar = getSupportActionBar();
        String title = plantMarker + " Günceleri";
        actionBar.setTitle(title);
        getSupportActionBar().setTitle(title);
        getDataFromFirestore();

        recyclerView.setNestedScrollingEnabled(true);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                String name = imagesFromFB.get(position);
                String date = datesFromFB.get(position);
                Intent intent = new Intent(FeedActivity.this, fullimage.class);
                intent.putExtra("imageUrl", name);
                intent.putExtra("date", date);
                intent.putExtra("plantname", plantMarker);
                startActivity(intent);

            }

            @Override
            public void onDeleteClick(int position) {
                remove(position);
                //userActions.setPostCountFor(getApplicationContext(),plantMarker, "del",idtodelFromFB.get(position));
                userActions.deleteDocFromFirebase(plantMarker, "feed", idtodelFromFB.get(position), plantinstauser);
                Intent intentq = new Intent(FeedActivity.this, PlantList.class);
                intentq.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentq);
                finish();

            }

            @Override
            public void onShareClick(int position, Bitmap bitmap) {
                shareBitmap(bitmap, "PlantInsta");
            }

            @Override
            public void onDownloadClick(int position, Bitmap bmp) {
                downloadBitmap(bmp, plantMarker + "@" + datesFromFB.get(position));

            }
        });
    }

    public void remove(int position) {

        recyclerAdapter.notifyItemRemoved(position);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void openImage(View view) {

    }


    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    private void downloadBitmap(Bitmap bitmap, String fileNames) {
        try {

            // folder yaratma
/*
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/PlantInsta");
            if (!folder.exists()){
                folder.mkdirs();
            }
            String extStorageDirectory = folder.toString();
            File file = new File(extStorageDirectory,fileNames + ".jpg");

            */

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileNames + ".jpg");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileNames, "PlantInsta");

            Toast.makeText(getApplicationContext(), fileNames +
                    "güncesi " + getExternalFilesDir(Environment.DIRECTORY_PICTURES) + " altına indirildi.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }


    }

    private void shareBitmap(Bitmap bitmap, String fileName) {

        try {
            Uri photoURI = null;
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName + ".jpg");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, true);

            photoURI = FileProvider.getUriForFile(getApplicationContext(), "com.zlpls.plantinsta.fileprovider", file);

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, photoURI); //Uri.fromFile(file));
            intent.putExtra(Intent.EXTRA_SUBJECT, "PlantInsta size resim gönderdi");
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent, "Resmim"));
            FileOperations fileOperations = new FileOperations();
            //fileOperations.deleteFiles(getApplicationContext());


/*

                ContentResolver contentResolver = getContentResolver();

                contentResolver.delete(photoURI,null, null);
                    System.out.println("file Deleted :");
*/


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }


    public String convertTimestampToDate(Timestamp timestamp) {
        try {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(timestamp.getSeconds() * 1000);
            return DateFormat.format("dd-MM-yyyy hh:mm ", cal).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void getDataFromFirestore() {

        try {

            CollectionReference collectionReference = firebaseFirestore.collection(plantinstauser).document(plantMarker).collection("history");
            //tarihe göre dizerek alma)
            collectionReference.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if (error != null) {
                        Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                    }
                    if (value != null) {

                        for (DocumentSnapshot snapshot : value.getDocuments()) {

                            Map<String, Object> data = snapshot.getData();// gönderilen hashmap leri alıyor
                            //Date creationDate = snapshot.getDate("date");

                            String comment = (String) data.get("comment");
                            String downloadurl = (String) data.get("image");
                            Timestamp timestamp = (Timestamp) data.get("date");
                            String date = convertTimestampToDate(timestamp);// timestamp firestore değerini okunabilir tarihe çeviren fonk
                            String id = snapshot.getId();
                            commentsFromFB.add(comment);
                            imagesFromFB.add(downloadurl);
                            //System.out.println("Örnek yol" + downloadurl);
                            datesFromFB.add(date);

                            idtodelFromFB.add(id);
                            plant.add(new PlantModel(downloadurl, "", comment, "", date, "", ""));
                            //System.out.println("plant:" + plant);
                            recyclerAdapter.notifyDataSetChanged();


                        }
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void addNewComments() {

    }
}
 /*
    @SuppressLint("NonConstantResourceId")

    private void setupBottomAppBar() {
        bottomAppBar = findViewById(R.id.bottomAppBar);


        //click event over Bottom bar menu item
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:


                    break;


            }
            return false;
        });

        //click event over navigation menu like back arrow or hamburger icon
        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userActions.signOutUser();
                Intent intent = new Intent(FeedActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                //
            }

        });
        /*
        FloatingActionButton feedfab = findViewById(R.id.floating_action_button);

        feedfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//UPLOAD new Plant state


                Intent intent = new Intent(FeedActivity.this, VisualMainActivity.class);
                intent.putExtra("fromList", 1);
                userActions.setPlantNameToModify(plantMarker);
                startActivity(intent);

            }
        });
         */


