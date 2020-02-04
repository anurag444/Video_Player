package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import static com.example.videoplayer.MainActivity.VIDEO_ACTIVITY_INTENT;

public class VideoPlayer extends AppCompatActivity {

    private VideoView mVideoView= null;
    private int position = 0;
    private ProgressDialog progressDialog;
    private VideoController mediaControlls;
    private MediaPlayer mediaPlayer= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        if(mediaControlls==null){
            mediaControlls= new VideoController(VideoPlayer.this);
            mVideoView= findViewById(R.id.videoView);
            mVideoView.setKeepScreenOn(true);
            try{
                mVideoView.setMediaController(mediaControlls);
                String intentinfo= getIntent().getStringExtra(String.valueOf(VIDEO_ACTIVITY_INTENT));
                final SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (intentinfo.equals("continueWatching")){
                    intentinfo= sp.getString("continueWatching",null);
                    position=sp.getInt("position",0);
                }else{
                    sp.edit().putString("continueWatching",intentinfo).apply();
                }
                if (intentinfo==null){
                    startActivity(new Intent(VideoPlayer.this,MainActivity.class));
                }
                mVideoView.setVideoPath(intentinfo);
            }catch (Exception e){
                Log.e("Error",e.getMessage());
                e.printStackTrace();
            }
            mVideoView.requestFocus();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer=mp;
                    if (mVideoView!=null){
                        mVideoView.seekTo(position);
                        if (position==0){
                            mVideoView.start();
                        }else if (mp!=null){
                            mp.start();
                        }
                    }
                    hideSystemUI();
                }
            });
        }
    }

    //to go to full screen mode
    private void hideSystemUI() {
        View decorView= getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    //This is save the instance of video when the user left the video
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInsatnceState) {
        super.onSaveInstanceState(savedInsatnceState);
        if (mVideoView!=null){
            savedInsatnceState.putInt("position",mVideoView.getCurrentPosition());
            mVideoView.pause();
        }
    }


    //This method restores the saved position and then seek to the position
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position= savedInstanceState.getInt("position");
        if (mediaPlayer!=null){
            mediaPlayer.seekTo(position);
        }else if (mVideoView!=null){
            mVideoView.seekTo(position);
        }
    }

    @Override
    protected void onPause() {
        if (mediaPlayer!=null) {
            mediaPlayer.pause();
            if (mVideoView != null) {
                position = mVideoView.getCurrentPosition();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putInt("position", position).apply();
            }
        }else if (mVideoView!=null){
                position= mVideoView.getCurrentPosition();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putInt("position",position).apply();
                mVideoView.pause();
            }
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialog= new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==DialogInterface.BUTTON_POSITIVE){
                    moveTaskToBack(true);
                }else if (which==DialogInterface.BUTTON_NEGATIVE){
                    Intent intent= new Intent(VideoPlayer.this, MainActivity.class);
                    intent.putExtra(VIDEO_ACTIVITY_INTENT,"showContinueWatching");
                    startActivity(intent);
                }
            }
        };
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Exit?").setPositiveButton("Close App",dialog)
                .setNegativeButton("Close Video",dialog).show();
    }
}
