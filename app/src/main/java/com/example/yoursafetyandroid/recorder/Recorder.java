package com.example.yoursafetyandroid.recorder;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.example.yoursafetyandroid.menu.MenuActivity;

import java.io.IOException;

public class Recorder {
    public static final String LOG_TAG = "Recording";
    public static MediaRecorder mRecorder;
    public  static String mFilename ="";
    public static MediaRecorder mediaRecorder;


    public Recorder()
    {
        mFilename= Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilename+="/recorder_audio.3gp";
    }
    public static void startRecording() {

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFilename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e){
            Log.e(LOG_TAG, "prepare()failed");
        }
        mRecorder.start();
    }
    public static void startRecording2() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(MenuActivity.pathForRecording);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            Log.e("Recording", "startRecording: ", e);
        }
    }


    public static void stopRecording()
    {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder=null;
    }
}
