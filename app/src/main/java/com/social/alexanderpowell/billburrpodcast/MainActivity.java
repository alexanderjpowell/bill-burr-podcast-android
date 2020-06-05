package com.social.alexanderpowell.billburrpodcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.social.alexanderpowell.billburrpodcast.dummy.DummyContent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public static final String Broadcast_PLAY_PAUSE = "com.social.alexanderpowell.billburrpodcast.PLAY_PAUSE";

    private TextView currentDurationTextView, remainingDurationTextView;
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

    private int currentPosition, duration;

    private MediaPlayerService player;
    boolean serviceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        //createNotificationChannel(context);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        //mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mHandler = new Handler();

        currentDurationTextView = findViewById(R.id.current_duration_text_view);
        remainingDurationTextView = findViewById(R.id.remaining_duration_text_view);
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

        //String url = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"; // Short test mp3
        //String url = "https://dts.podtrac.com/redirect.mp3/chtbl.com/track/9EE2G/pdst.fm/e/rss.art19.com/episodes/9fc0fc76-84b2-4fa0-9ef6-b736412d045b.mp3";
        //String url = "https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg";

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

        String url = "https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg";
        //playAudio(url);

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

        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Toast.makeText(getApplicationContext(), "first", Toast.LENGTH_SHORT).show();
                    URL urll = new URL("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                    HttpURLConnection conn = (HttpURLConnection) urll.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = factory.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    //
                    String results = myparser.getText();
                    Log.d("MainActivity", results);
                    //Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
                    //

                    stream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.d("MainActivity", ex.getMessage());
                    //Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        thread.start();*/

        new FetchFeedTask().execute((Void) null);
    }

    public static void expandBottomSheet() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /*public void stopService(View view) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }*/

    public void initializeSeekBar() {
        seekBar.setMax(duration);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                //if (mediaPlayer != null) {
                    //currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    //seekBar.setProgress(currentPosition);
                    //setAudioStats();
                //}
                mHandler.postDelayed(mRunnable,1000);
            }
        };
        mHandler.postDelayed(mRunnable,1000);
    }

    private void setAudioStats() {
        currentDurationTextView.setText(String.valueOf(currentPosition));
        remainingDurationTextView.setText(String.valueOf(duration));
    }

    /*public void playPauseOnClick(View view) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.baseline_play_circle_filled_white_48);
            // Stop service but keep displaying notification
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            stopService(serviceIntent);
            //
            mNotificationManager.notify(1, notification);
            //
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.baseline_pause_circle_filled_white_48);
            // restart the service and dismiss the notification
            mNotificationManager.cancel(1);
            //
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
            ContextCompat.startForegroundService(this, serviceIntent);
            //
        }
    }*/

    public void playPauseOnClick(View view) {
        Intent broadcastIntent = new Intent(Broadcast_PLAY_PAUSE);
        broadcastIntent.putExtra("ACTION", "PLAY_PAUSE");
        sendBroadcast(broadcastIntent);

        if (serviceBound) {
            int cur = player.getCurrentPosition();
            //Log.d("MainActivity", String.valueOf(cur));
            //Toast.makeText(getApplicationContext(), String.valueOf(cur), Toast.LENGTH_SHORT).show();
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public void playAudio(String media) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            //
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int cur = player.getCurrentPosition() / 1000;
                    int dur = player.getDuration() / 1000;
                    seekBar.setProgress(cur);
                    currentDurationTextView.setText(String.valueOf(cur));
                    remainingDurationTextView.setText(String.valueOf(dur));
                    mHandler.postDelayed(mRunnable,1000);
                }
            };
            mHandler.postDelayed(mRunnable,1000);
            //
        } else {
            Toast.makeText(getApplicationContext(), "service already bound", Toast.LENGTH_SHORT).show();
            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_PAUSE);
            broadcastIntent.putExtra("ACTION", "NEW_AUDIO_SOURCE");
            sendBroadcast(broadcastIntent);
        }
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

    public void playPauseOnClickPreview(View view) {
        Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
    }

    private enum RSSXMLTag {
        TITLE, DESCRIPTION, DATE, IGNORETAG;
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;
        private RSSXMLTag currentTag;

        @Override
        protected void onPreExecute() {
            //urlLink = "https://xkcd.com/rss.xml";
            urlLink = "https://rss.art19.com/monday-morning-podcast";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                parseFeed(inputStream);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

        }

        public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
                IOException {
            String title = null;
            String link = null;
            String description = null;
            String pubDate = null;
            String guid = null;
            boolean isItem = false;
            List<RssFeedModel> items = new ArrayList<>();

            try {
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                xmlPullParser.setInput(inputStream, null);

                int eventType = xmlPullParser.getEventType();
                int count = 0;
                int quota = 200;
                RssFeedModel rssFeedModel = null;
                while (eventType != XmlPullParser.END_DOCUMENT && count < quota) {
                    String name = xmlPullParser.getName();

                    if (eventType == XmlPullParser.START_DOCUMENT) {

                    } else if (eventType == XmlPullParser.START_TAG) {
                        //Log.d("START_TAG", xmlPullParser.getName());
                        if (xmlPullParser.getName().equals("item")) {
                            rssFeedModel = new RssFeedModel();
                            currentTag = RSSXMLTag.IGNORETAG;
                        } else if (xmlPullParser.getName().equals("title")) {
                            //Log.d("START_TAG", rssFeedModel.printModel());
                            currentTag = RSSXMLTag.TITLE;
                        } else if (xmlPullParser.getName().equals("description")) {
                            currentTag = RSSXMLTag.DESCRIPTION;
                        } else if (xmlPullParser.getName().equals("pubDate")) {
                            currentTag = RSSXMLTag.DATE;
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        //Log.d("END_TAG", xmlPullParser.getName());
                        if (xmlPullParser.getName().equals("item")) {
                            if (rssFeedModel != null) {
                                Log.d("END_TAG", rssFeedModel.printModel());
                            }
                        } else {
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        String content = xmlPullParser.getText().trim();
                        //Log.d("TEXT", content);
                        if (rssFeedModel != null) {
                            switch (currentTag) {
                                case TITLE:
                                    rssFeedModel.setTitle(content);
                                    break;
                                case DESCRIPTION:
                                    rssFeedModel.setDescription(content);
                                    break;
                                case DATE:
                                    rssFeedModel.setPubDate(content);
                                    break;
                            }
                        }
                    }
                    eventType = xmlPullParser.next();
                    count++;
                }

                /*xmlPullParser.nextTag();
                int count = 0;
                int quota = 1;
                while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                //while (eventType != XmlPullParser.END_DOCUMENT) {

                    int eventType = xmlPullParser.getEventType();

                    String name = xmlPullParser.getName();
                    if(name == null)
                        continue;

                    if(eventType == XmlPullParser.END_TAG) {
                        if(name.equalsIgnoreCase("item")) {
                            isItem = false;
                        }
                        continue;
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if(name.equalsIgnoreCase("item")) {
                            count++;
                            isItem = true;
                            continue;
                        }
                    }

                    //
                    if (eventType == XmlPullParser.START_TAG && isItem) {
                        //Log.d("MyXmlParser", xmlPullParser.getName());
                        Log.d("MyXmlParser - TAG", name);
                    }
                    //
                    //Log.d("MyXmlParser", "Parsing name ==> " + name);
                    String result = "";
                    if (xmlPullParser.next() == XmlPullParser.TEXT && isItem) {
                    //if (xmlPullParser.getEventType() == XmlPullParser.TEXT && isItem) {
                    //if (eventType == XmlPullParser.TEXT) {
                        result = xmlPullParser.getText();
                        Log.d("MyXmlParser - TEXT", result);
                        Log.d("NAME", name);
                        if (name.equals("enclosure")) {
                            Log.d("url", "enclosure");
                            Log.d("url", xmlPullParser.getAttributeValue(null, "url"));
                            Log.d("url", xmlPullParser.getAttributeValue(0));
                        }
                        xmlPullParser.nextTag();
                    }

                    //if (name.equals("enclosure")) {

                    //}

                    /if (name.equalsIgnoreCase("title")) {
                        title = result;
                    } else if (name.equalsIgnoreCase("link")) {
                        link = result;
                    } else if (name.equalsIgnoreCase("description")) {
                        description = result;
                    } else if (name.equalsIgnoreCase("pubDate")) {
                        pubDate = result;
                    } else if (name.equalsIgnoreCase("guid")) {
                        guid = result;
                    }

                    if (title != null && link != null && description != null) {
                        if(isItem) {
                            RssFeedModel item = new RssFeedModel(title, link, description, pubDate, guid);
                            items.add(item);
                            //Log.d("MainActivity", item.description);
                        }
                        else {
                            //Log.d("MainActivity", description);
                            //mFeedTitle = title;
                            //mFeedLink = link;
                            //mFeedDescription = description;
                        }

                        title = null;
                        link = null;
                        description = null;
                        isItem = false;
                    }/
                    //count++;
                    if (count > quota) {
                        //break;
                    }
                    //eventType = xmlPullParser.next();
                    //xmlPullParser.next();
                }*/
                return items;
            } finally {
                inputStream.close();
            }
        }
    }

    public class RssFeedModel {

        public String title;
        //public String link;
        public String description;
        public String pubDate;
        //public String guid;

        public RssFeedModel() {

        }

        public RssFeedModel(String title, String description, String pubDate) {
            this.title = title;
            //this.link = link;
            this.description = description;
            this.pubDate = pubDate;
            //this.guid = guid;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public String printModel() {
            return this.title + " : " + this.description + " : " + this.pubDate;
        }
    }
}
