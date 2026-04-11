package com.example.gamesplatform.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;

import com.example.gamesplatform.R;

import java.io.File;
import java.io.IOException;

public class AudioService extends Service {
    private final IBinder binder = new AudioBinder();
    private MediaRecorder recorder;
    private MediaPlayer player;
    private String fileName;

    @Override
    public void onCreate() {
        super.onCreate();
        fileName = getFilesDir().getAbsolutePath() + "/user_audio.3gp";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // --- הקלטה בשרשור נפרד כדי למנוע ANR ---
    public void startRecording() {
        new Thread(() -> {
            stopPlayback();
            try {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile(fileName);

                recorder.prepare();
                recorder.start();
            } catch (Exception e) {
                e.printStackTrace();
                releaseRecorder();
            }
        }).start();
    }

    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                // קורה אם ההקלטה הייתה קצרה מדי
                new File(fileName).delete();
            } finally {
                releaseRecorder();
            }
        }
    }

    private void releaseRecorder() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    public void playAudio() {
        stopPlayback();

        File file = new File(fileName);
        if (!file.exists()) return;

        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.setOnPreparedListener(MediaPlayer::start);
            player.setOnCompletionListener(mp -> stopPlayback());
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlayback() {
        if (player != null) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.release();
            player = null;
        }
    }

    public void playPianoNote() {
        // יוצרים נגן חדש לכל לחיצה כדי שהצלילים יוכלו להישמע יחד
        final MediaPlayer mp = new MediaPlayer();
        try {
            File file = new File(fileName);
            if (file.exists()) {
                mp.setDataSource(fileName);
            } else {
                // אם אין קובץ, נגן את צליל ברירת המחדל
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.default_sound);
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }

            mp.prepare();
            mp.start();

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        stopPlayback();
    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }


}