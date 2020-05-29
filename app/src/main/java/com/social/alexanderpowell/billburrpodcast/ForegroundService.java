package com.social.alexanderpowell.billburrpodcast;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service implements MediaPlayer.OnPreparedListener {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private static final String ACTION_PLAY = "com.example.action.PLAY";

    private MediaPlayer mediaPlayer = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        MainActivity.createNotificationChannel(getApplicationContext());
        //createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Bitmap albumArtBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mmp);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.baseline_play_circle_filled_24)
                .addAction(R.drawable.baseline_replay_30_24, "Previous", pendingIntent)
                .addAction(R.drawable.baseline_play_circle_filled_24, "Previous", pendingIntent)
                .addAction(R.drawable.baseline_forward_30_24, "Previous", pendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .setLargeIcon(albumArtBitmap)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();

        //
        String url = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3";
        if (intent.getAction().equals(ACTION_PLAY)) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(url);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        //

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }
}