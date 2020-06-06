package com.social.alexanderpowell.billburrpodcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.Console;
import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String CHANNEL_ID = "MediaPlayerServiceChannel";

    private static final String NOTIFICATION_ACTION_PLAY_PAUSE = "com.social.alexanderpowell.billburrpodcast.NOTIFICATION_ACTION_PLAY_PAUSE";
    private static final String NOTIFICATION_ACTION_REWIND = "com.social.alexanderpowell.billburrpodcast.NOTIFICATION_ACTION_REWIND";
    private static final String NOTIFICATION_ACTION_FAST_FORWARD = "com.social.alexanderpowell.billburrpodcast.NOTIFICATION_ACTION_FAST_FORWARD";

    private static final int NOTIFICATION_ID = 101;

    private MediaPlayer mediaPlayer;
    //path to the audio file
    private String mediaFile;
    private int resumePosition;
    private AudioManager audioManager;

    //
    private Handler mHandler;
    private Runnable mRunnable;
    //

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(mediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        //callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        //registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        registerPlayPauseBroadcast();

        /*mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                //if (mediaPlayer != null) {
                //currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                //seekBar.setProgress(currentPosition);
                //setAudioStats();
                //}
                Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(mRunnable,1000);
            }
        };
        mHandler.postDelayed(mRunnable,1000);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() == null) {
            createNotificationChannel(getApplicationContext());
            Bitmap albumArtBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mmp);

            Intent notificationIntent1 = new Intent(this, MediaPlayerService.class);
            notificationIntent1.setAction(NOTIFICATION_ACTION_PLAY_PAUSE);
            PendingIntent pendingIntentPlayPause = PendingIntent.getService(this, 0, notificationIntent1, 0);

            Intent notificationIntent2 = new Intent(this, MediaPlayerService.class);
            notificationIntent2.setAction(NOTIFICATION_ACTION_REWIND);
            PendingIntent pendingIntentRewind = PendingIntent.getService(this, 0, notificationIntent2, 0);

            Intent notificationIntent3 = new Intent(this, MediaPlayerService.class);
            notificationIntent3.setAction(NOTIFICATION_ACTION_FAST_FORWARD);
            PendingIntent pendingIntentFastForward = PendingIntent.getService(this, 0, notificationIntent3, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle("Foreground Service")
                    .setContentText("Notification Content")
                    .setContentInfo("Info")
                    .setSmallIcon(R.drawable.baseline_play_circle_filled_24)
                    .addAction(R.drawable.baseline_replay_30_24, "Rewind", pendingIntentRewind)
                    .addAction(R.drawable.baseline_play_circle_filled_24, "Play/Pause", pendingIntentPlayPause)
                    .addAction(R.drawable.baseline_forward_30_24, "Fast Forward", pendingIntentFastForward)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2))
                    .setLargeIcon(albumArtBitmap)
                    .setShowWhen(false) // Hide timestamp
                    .build();

            startForeground(NOTIFICATION_ID, notification);

            try {
                //An audio file is passed to the service through putExtra();
                mediaFile = intent.getExtras().getString("media");
                //String action = intent.getStringExtra("action");
            } catch (NullPointerException e) {
                stopSelf();
            }

            //Request audio focus
            if (!requestAudioFocus()) {
                //Could not gain focus
                stopSelf();
            }

            if (mediaFile != null && mediaFile != "")
                initMediaPlayer();

            //handleIncomingActions(intent);

        } else if (intent.getAction().equals(NOTIFICATION_ACTION_PLAY_PAUSE)) {
            if (mediaPlayer.isPlaying()) {
                pauseMedia();
            } else {
                resumeMedia();
            }
        } else if (intent.getAction().equals(NOTIFICATION_ACTION_REWIND)) {
            rewindMedia();
        } else if (intent.getAction().equals(NOTIFICATION_ACTION_FAST_FORWARD)) {
            fastForwardMedia();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIncomingActions(Intent playbackAction) {
        //pause
        //Toast.makeText(getApplicationContext(), "handleIncomingActions", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf();
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void rewindMedia() {
        int newPosition = mediaPlayer.getCurrentPosition() / 1000 - 30;
        if (newPosition >= 0) {
            mediaPlayer.seekTo(newPosition * 1000);
        } else {
            mediaPlayer.seekTo(0);
        }
    }

    private void fastForwardMedia() {
        int newPosition = mediaPlayer.getCurrentPosition() / 1000 + 30;
        if (newPosition <= mediaPlayer.getDuration() / 1000) {
            mediaPlayer.seekTo(newPosition * 1000);
        } else {
            mediaPlayer.seekTo(mediaPlayer.getDuration());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
    }

    private BroadcastReceiver playPauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("ACTION");
            if (action.equals("PLAY_PAUSE")) {
                if (mediaPlayer.isPlaying()) {
                    pauseMedia();
                } else {
                    resumeMedia();
                }
            } else if (action.equals("REWIND")) {
                rewindMedia();
            } else if (action.equals("FAST_FORWARD")) {
                fastForwardMedia();
            } else if (action.equals("NEW_AUDIO_SOURCE")) {
                stopMedia();
                mediaPlayer.reset();
                mediaFile = intent.getStringExtra("media");
                initMediaPlayer();
            } else {
                Toast.makeText(getApplicationContext(), intent.getAction(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void registerPlayPauseBroadcast() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_PAUSE);
        registerReceiver(playPauseReceiver, filter);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Boolean audioIsPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public String getStats() {return "";}

}