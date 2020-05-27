package com.social.alexanderpowell.billburrpodcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity {

    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView currentDurationTextView, remainingDurationTextView;
    private ImageView playPauseButton, previewPlayPauseButton;
    private LinearLayout preview, main;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Handler mHandler;
    private Runnable mRunnable;

    private int currentPosition, duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mHandler = new Handler();

        //Button buttonExpand = findViewById(R.id.button_expand);
        //Button buttonCollapse = findViewById(R.id.button_collapse);

        currentDurationTextView = findViewById(R.id.current_duration_text_view);
        remainingDurationTextView = findViewById(R.id.remaining_duration_text_view);
        playPauseButton = findViewById(R.id.play_pause_button);
        previewPlayPauseButton = findViewById(R.id.preview_play_pause_button);
        seekBar = findViewById(R.id.seek_bar);

        preview = findViewById(R.id.preview);
        main = findViewById(R.id.main);

        /*buttonExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        buttonCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });*/

        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        preview.setVisibility(View.VISIBLE);
                        main.setVisibility(View.GONE);
                        //mTextViewState.setText("Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        preview.setVisibility(View.GONE);
                        main.setVisibility(View.VISIBLE);
                        //mTextViewState.setText("Expanded");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //mTextViewState.setText("Dragging...");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        //mTextViewState.setText("Hidden");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        //mTextViewState.setText("Settling...");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //mTextViewState.setText("Half Expanded");
                        break;
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //mTextViewState.setText("Sliding...");
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        try {
            String url = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"; // Short test mp3
            //String url = "https://dts.podtrac.com/redirect.mp3/chtbl.com/track/9EE2G/pdst.fm/e/rss.art19.com/episodes/9fc0fc76-84b2-4fa0-9ef6-b736412d045b.mp3";
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    duration = mediaPlayer.getDuration() / 1000;
                    initializeSeekBar();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b) {
                    mediaPlayer.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initializeSeekBar() {
        seekBar.setMax(duration);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    setAudioStats();
                }
                mHandler.postDelayed(mRunnable,100);
            }
        };
        mHandler.postDelayed(mRunnable,100);
    }

    private void setAudioStats() {
        currentDurationTextView.setText(String.valueOf(currentPosition));
        remainingDurationTextView.setText(String.valueOf(duration));
    }

    public void playPauseOnClick(View view) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.baseline_play_circle_filled_white_48);
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.baseline_pause_circle_filled_white_48);
        }
    }

    public void playPauseOnClickPreview(View view) {
        Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
    }

    public void rewindOnClick(View view) {
        int newPosition = currentPosition - 30;
        if (newPosition >= 0) {
            mediaPlayer.seekTo(newPosition * 1000);
        } else {
            mediaPlayer.seekTo(0);
        }
    }

    public void fastForwardOnClick(View view) {
        int newPosition = currentPosition + 30;
        if (newPosition <= duration) {
            mediaPlayer.seekTo(newPosition * 1000);
        } else {
            mediaPlayer.seekTo(duration * 1000);
        }
    }
}
