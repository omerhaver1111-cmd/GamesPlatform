package com.example.gamesplatform.services;

import android.app.Service;
import android.content.Intent;
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

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

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
            } catch (Exception e) { e.printStackTrace(); }
            player.release();
            player = null;
        }
    }

    public void playPianoNote() {
        stopPlayback();
        player = new MediaPlayer();
        try {
            File userFile = new File(fileName);
            if (userFile.exists() && userFile.length() > 0) {
                player.setDataSource(fileName);
            } else {
                // אם אין הקלטה, נשתמש בצליל ברירת המחדל
                android.content.res.AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.default_sound);
                if (afd == null) return;
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }

            player.setOnPreparedListener(MediaPlayer::start);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        stopPlayback();
    }


}