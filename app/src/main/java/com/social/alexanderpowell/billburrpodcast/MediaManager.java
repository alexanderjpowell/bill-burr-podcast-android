package com.social.alexanderpowell.billburrpodcast;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class MediaManager {

    private String url;
    private MediaPlayer mediaPlayer;
    private int durationSeconds;

    public MediaManager(String url) {
        try {
            this.url = url;
            this.mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    durationSeconds = mediaPlayer.getDuration() / 1000;
                    //initializeSeekBar();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}