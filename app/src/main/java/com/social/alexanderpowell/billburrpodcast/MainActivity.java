package com.social.alexanderpowell.billburrpodcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.social.alexanderpowell.billburrpodcast.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public static final String Broadcast_PLAY_PAUSE = "com.social.alexanderpowell.billburrpodcast.PLAY_PAUSE";

    private TextView currentDurationTextView, remainingDurationTextView, episodeTitleTextView;
    private ImageView playPauseButton, previewPlayPauseButton;
    private LinearLayout preview, main;
    private SeekBar seekBar;
    //private MediaPlayer mediaPlayer;
    private Handler mHandler;
    private Runnable mRunnable;
    private static BottomSheetBehavior mBottomSheetBehavior;
    private Context context;

    private NotificationManagerCompat mNotificationManager;
    private Notification notification;

    //private int currentPosition, duration;

    private MediaPlayerService player;
    boolean serviceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mHandler = new Handler();

        currentDurationTextView = findViewById(R.id.current_duration_text_view);
        remainingDurationTextView = findViewById(R.id.remaining_duration_text_view);
        episodeTitleTextView = findViewById(R.id.episode_title_text_view);
        playPauseButton = findViewById(R.id.play_pause_button);
        previewPlayPauseButton = findViewById(R.id.preview_play_pause_button);
        seekBar = findViewById(R.id.seek_bar);

        preview = findViewById(R.id.preview);
        main = findViewById(R.id.main);

        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        preview.setVisibility(View.VISIBLE);
                        main.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        preview.setVisibility(View.GONE);
                        main.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //if (mediaPlayer != null && b) {
                    //mediaPlayer.seekTo(i * 1000);
                //}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Bitmap albumArtBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mmp);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Foreground Service Example in Android")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.baseline_play_circle_filled_24)
                .addAction(R.drawable.baseline_replay_30_24, "Previous", pendingIntent)
                .addAction(R.drawable.baseline_play_circle_filled_24, "Previous", pendingIntent)
                .addAction(R.drawable.baseline_forward_30_24, "Previous", pendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .setLargeIcon(albumArtBitmap)
                .build();

        mNotificationManager = NotificationManagerCompat.from(this);
    }

    public static void expandBottomSheet() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void playPauseOnClick(View view) {
        Intent broadcastIntent = new Intent(Broadcast_PLAY_PAUSE);
        broadcastIntent.putExtra("ACTION", "PLAY_PAUSE");
        sendBroadcast(broadcastIntent);

        if (serviceBound) {
            //int cur = player.getCurrentPosition();
            if (player.audioIsPlaying()) {
                playPauseButton.setImageResource(R.drawable.baseline_play_circle_filled_white_48);
                previewPlayPauseButton.setImageResource(R.drawable.baseline_play_circle_filled_white_24);
            } else {
                playPauseButton.setImageResource(R.drawable.baseline_pause_circle_filled_white_48);
                previewPlayPauseButton.setImageResource(R.drawable.baseline_pause_circle_filled_white_24);
            }
        }
    }

    public void rewindOnClick(View view) {
        Intent broadcastIntent = new Intent(Broadcast_PLAY_PAUSE);
        broadcastIntent.putExtra("ACTION", "REWIND");
        sendBroadcast(broadcastIntent);
    }

    public void fastForwardOnClick(View view) {
        Intent broadcastIntent = new Intent(Broadcast_PLAY_PAUSE);
        broadcastIntent.putExtra("ACTION", "FAST_FORWARD");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    public static void createNotificationChannel(Context context) {
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;


            int dur = player.getDuration() / 1000;
            seekBar.setMax(dur);
            remainingDurationTextView.setText(String.valueOf(dur));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public String getDateFromTitle(String title) {
        String[] words = title.split(" ");
        if (words.length > 0) {
            return words[words.length - 1];
        } else {
            return title;
        }
    }

    public void playAudio(String media, String title) {
        //
        episodeTitleTextView.setText(getDateFromTitle(title));
        //
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int cur = player.getCurrentPosition() / 1000;
                    int dur = player.getDuration() / 1000;
                    seekBar.setProgress(cur);
                    currentDurationTextView.setText(formatSeconds(cur));
                    remainingDurationTextView.setText(formatSeconds(dur));
                    mHandler.postDelayed(mRunnable,1000);
                    //Toast.makeText(getApplicationContext(), "runnable", Toast.LENGTH_SHORT).show();
                }
            };
            mHandler.postDelayed(mRunnable,1000);
            //
        } else {
            // Change SeekBar max
            seekBar.setMax(player.getDuration() / 1000);
            //Toast.makeText(getApplicationContext(), "service already bound", Toast.LENGTH_SHORT).show();
            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_PAUSE);
            broadcastIntent.putExtra("ACTION", "NEW_AUDIO_SOURCE");
            broadcastIntent.putExtra("media", media);
            sendBroadcast(broadcastIntent);
        }
    }

    public static String formatSeconds(int seconds) {
        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;
        return p2 + ":" + p3 + ":" + p1;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    public void bottomSheetPreviewOnClick(View view) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
