package com.example.videoplayer;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.VisibilityAwareImageButton;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isLongClicked;
    private boolean isSelected;
    private int selectedPosition;
    private View bottomBar,selectedView;
    private VideoListAdapter adapter= new VideoListAdapter();

    private static final int REQUEST_PERMISSSIONS=1234;
    private static final String[] PERMISSIONS= {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_COUNT=2;


    public static final String VIDEO_ACTIVITY_INTENT="Video";

    private List<String> videosList= new ArrayList<>();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String activityState= getIntent().getStringExtra(VIDEO_ACTIVITY_INTENT);
        if (activityState!=null && activityState.equals("showContinueWatching")){
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final String videoPath= sp.getString("continueWatching",null);
            final Button continueWatchingBtn= findViewById(R.id.continueWatchingBtn);
            continueWatchingBtn.setVisibility(View.VISIBLE);
            FloatingActionButton fab = new FloatingActionButton(MainActivity.this);
            fab.setOnClickListener(this);
            continueWatchingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent= new Intent(MainActivity.this, VideoPlayer.class);
                    intent.putExtra(VIDEO_ACTIVITY_INTENT,"continueWatching");
                    startActivity(intent);
                }
            });

        }


    }


    //Method to check permissions
    @SuppressLint("NewApi")
    private boolean arePermissionsDenied(){
        for (int i =0;i<PERMISSIONS_COUNT;i++)
            if (checkSelfPermission(PERMISSIONS[i])!= PackageManager.PERMISSION_GRANTED){
                return true;

            }
        return false;
        }



        @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode==REQUEST_PERMISSSIONS && grantResults.length>0){
            if (arePermissionsDenied()){
                /*App will not work unless usr gives the permission
                This code will delete the app data. So, everytime user open the app
                it keeps asking for permissions until user grants the permission
                 */
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE)))
                .clearApplicationUserData();

                recreate();//Cause this Activity to be recreated with a new instance.
            }else{
                onResume();
            }
        }
    }


    private void addVideosFrom(String dirPath){
        final File videosDir= new File(dirPath);
        if (!videosDir.exists()){
            //making default path/directory
            videosDir.mkdir();
            return;
        }
        final File[] files= videosDir.listFiles();// list all files in directory
        for (File file:files){
            final String path = file.getAbsolutePath();

            //add only video files
            if (path.endsWith(".mp4")){
                videosList.add(path);
            }

        }

    }
    private void fillvideosList(){
        videosList.clear();
        //add files from the specific path in the system
        try {
            addVideosFrom(String.valueOf(Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_MOVIES)));
            addVideosFrom(String.valueOf(Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS)));
            addVideosFrom(String.valueOf(Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DCIM)));
        }catch (Exception e){

            Toast.makeText(this, "No videos found on your device", Toast.LENGTH_LONG).show();
        }


    }
    private boolean isVideoPlayerIntialiized; //By default it is false.
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && arePermissionsDenied()){
            requestPermissions(PERMISSIONS,REQUEST_PERMISSSIONS);
            return;
        }
        if (!isVideoPlayerIntialiized){
            final RecyclerView recyclerView= findViewById(R.id.videoList);
            fillvideosList();
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    DividerItemDecoration.VERTICAL
            );
            recyclerView.addItemDecoration(dividerItemDecoration);
            adapter.setData(videosList);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);




            //to play an item
            adapter.setOnItemClickListener(new VideoListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (isLongClicked){
                        isLongClicked= false;
                        return;
                    }
                    //get the path of current video
                    Intent intent= new Intent(MainActivity.this,VideoPlayer.class);
                    intent.putExtra(VIDEO_ACTIVITY_INTENT,videosList.get(position));
                    startActivity(intent);
                }
            });

            //to select an item
            adapter.setOnItemLongClickListener(new VideoListAdapter.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(int position, View view) {
                    bottomBar=findViewById(R.id.bottomBar);
                    isLongClicked= true;
                    if (isSelected){
                        if (selectedPosition != position){
                            return false;
                        }
                        view.setBackgroundColor(Color.WHITE);
                        isSelected=false;
                        bottomBar.setVisibility(View.GONE);
                    }else {
                        selectedPosition=position;
                        bottomBar.setVisibility(View.VISIBLE);
                        view.setBackgroundColor(view.getResources().getColor(R.color.gray));
                        isSelected=true;
                        selectedView=view;
                    }
                    return false;
                }
            });

            final Button rename= findViewById(R.id.rename);
            final Button delete= findViewById(R.id.delete);
            final Button share= findViewById(R.id.share);
            StrictMode.VmPolicy.Builder builder= new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            rename.setOnClickListener(this);
            delete.setOnClickListener(this);
            share.setOnClickListener(this);


            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            isVideoPlayerIntialiized= true;
        }
    }


    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.rename:
                final AlertDialog.Builder renameDialog=
                        new AlertDialog.Builder(MainActivity.this);
                renameDialog.setTitle("Rename To:");
                final EditText input = new EditText(MainActivity.this);
                final String renamePath= videosList.get(selectedPosition);
                input.setText(renamePath.substring(renamePath.lastIndexOf('/')+1));
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                renameDialog.setView(input);
                renameDialog.setPositiveButton("Rename",
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String s= new File(renamePath).getParent() + "/" + input.getText();
                        File newFile= new File(s);
                        new File(renamePath).renameTo(newFile);
                        //this will update the list with new data
                        fillvideosList();
                        adapter.setData(videosList);
                        updateList();
                    }
                });
                renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                updateList();
                renameDialog.show();
                break;
            case R.id.delete:
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);
                deleteDialog.setTitle("Delete");
                deleteDialog.setMessage("Do you really want to delete it?");
                deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       new File(videosList.get(selectedPosition)).delete();
                       //update
                        fillvideosList();
                        adapter.setData(videosList);
                        findViewById(R.id.continueWatchingBtn).setVisibility(View.GONE);
                    }
                });
                deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                updateList();
                deleteDialog.show();
                break;
            case R.id.share:
                final String videoPath = videosList.get(selectedPosition);
                final Intent intent= new Intent(Intent.ACTION_SEND);
                intent.setType("videos/mp4");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(videoPath)));
                startActivity(Intent.createChooser(intent,"Share To:"));
                updateList();
                break;
            case R.id.floatingActionButton:
                final Intent intent1= new Intent(MainActivity.this, VideoPlayer.class);
                intent1.putExtra(VIDEO_ACTIVITY_INTENT,"continueWatching");
                startActivity(intent1);
                break;
        }
    }

    private void updateList() {
        selectedView.setBackgroundColor(Color.WHITE);
        isSelected=false;
        bottomBar.setVisibility(View.GONE);
    }

}

