package br.com.fiap.fiapplayer.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.app.AlertDialog;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

import br.com.fiap.fiapplayer.PlayerAppWidget;
import br.com.fiap.fiapplayer.R;
import br.com.fiap.fiapplayer.SelectSongActivity;
import br.com.fiap.fiapplayer.broadcasts.HeadsetReceiver;
import br.com.fiap.fiapplayer.events.PlayerEvent;

import br.com.fiap.fiapplayer.models.VagalumeLyric;
import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class PlayerService extends Service {

    private static final String TAG = "PlayerService";
    public static final String API_URL = "http://api.vagalume.com.br";

    public static String BUNDLE_SONG_IS_PLAYING = "BUNDLE_SONG_IS_PLAYING";
    public static String BUNDLE_SONG_CURRENT_POSITION = "BUNDLE_SONG_CURRENT_POSITION";

    private MediaPlayer player;
    private Timer songCurrentTimer;

    private HeadsetReceiver headsetReceiver;
    private int seekForwardTime = 5000;

    private String song;
    private boolean isRunning = false;

    private AlertDialog dialog;
    private int fastPosition = 0;
    private Timer fastTimer;

    public PlayerService() {
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onEvent(new PlayerEvent(PlayerEvent.ActionType.STOP));
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(isRunning) return -1;

        Log.e(TAG, "Service started!");

        headsetReceiver = new HeadsetReceiver();
        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        EventBus.getDefault().register(this);

        isRunning = true;
        return START_STICKY;
    }

    public void onEvent(PlayerEvent event) {

        if(event.getAction() == PlayerEvent.ActionType.PLAY){
            Log.w(TAG, "Event::Play()");

            if(song == null){

                Intent selectSong = new Intent(this, SelectSongActivity.class);
                selectSong.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(selectSong);

            }else{

                player.start();
                notifyIfIsPlaying(true);

            }

        }else if(event.getAction() == PlayerEvent.ActionType.SONG_SELECTED){
            Log.w(TAG, "Event::SONG_SELECTED()");

            try {

                song = event.getSongSelected().getFilePath();
                Log.w(TAG, "Song selected: " + song);
                player.setDataSource(song);
                player.prepare();
                player.start();
                notifyIfIsPlaying(true);
                showLyrics();

            } catch (Exception e) {
                android.util.Log.e(TAG, "Play error", e);
                e.printStackTrace();
            }

        }else if(event.getAction() == PlayerEvent.ActionType.PAUSE){
            Log.w(TAG, "Event::PAUSE()");

            if(player.isPlaying()){
                player.pause();
            }

            notifyIfIsPlaying(false);
        }else if(event.getAction() == PlayerEvent.ActionType.STOP){
            Log.w(TAG, "Event::STOP()");

            if(song != null){
                player.stop();
                player.reset();
                song = null;
            }

            notifyIfIsPlaying(false);
        }else if(event.getAction() == PlayerEvent.ActionType.REWIND){
            Log.w(TAG, "Event::REWIND()");


            if(player.isPlaying()) {
                int currentPosition = player.getCurrentPosition();
                final int fastForwardPosition = currentPosition - seekForwardTime;

                if (fastForwardPosition >= 0) {

                    fastPosition = currentPosition;
                    if(fastTimer != null) fastTimer.cancel();
                    fastTimer = new Timer();

                    fastTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (fastPosition <= fastForwardPosition) {
                                fastTimer.cancel();
                                fastTimer = null;
                                return;
                            }

                            player.seekTo(fastPosition);
                            fastPosition = fastPosition - 1000;
                        }
                    }, 0, 300);

                } else {

                    player.seekTo(0);

                }

            }

        }else if(event.getAction() == PlayerEvent.ActionType.FORWARD){
            Log.w(TAG, "Event::FORWARD()");

            if(player.isPlaying()){
                int currentPosition = player.getCurrentPosition();
                final int fastForwardPosition = currentPosition + seekForwardTime;

                if(fastForwardPosition <= player.getDuration()){

                    fastPosition = currentPosition;
                    if(fastTimer != null) fastTimer.cancel();
                    fastTimer = new Timer();

                    fastTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if( fastPosition >= fastForwardPosition){
                                fastTimer.cancel();
                                fastTimer = null;
                                return;
                            }

                            player.seekTo(fastPosition);
                            fastPosition = fastPosition + 1000;
                        }
                    }, 0, 300);

                }else{

                    player.seekTo(player.getDuration());

                }
            }

        }else if(event.getAction() == PlayerEvent.ActionType.SHOW_LYRICS){

            Log.w(TAG, "Event::SHOW_LYRICS()");
            showLyrics();

        }

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(headsetReceiver);

        player.stop();
        player = null;

        super.onDestroy();
        Log.w(TAG, "Service stopped!");
    }


    private void notifyIfIsPlaying(final boolean isPlaying){

        if(isPlaying){
            songCurrentTimer = new Timer();
            songCurrentTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int currentPosition = player.getCurrentPosition();
                    Intent intent = new Intent(PlayerService.this, PlayerAppWidget.class);
                    intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
                    intent.putExtra(BUNDLE_SONG_CURRENT_POSITION, (long) currentPosition);
                    sendBroadcast(intent);
                    Log.w(TAG, "Updating song current position to: " + currentPosition);
                }
            }, 0, 1000);
        }else{
            if(songCurrentTimer != null){
                Log.w(TAG, "Stopping to update song current position.");
                songCurrentTimer.cancel();
            }
        }

        int state = isPlaying ? 1 : 2;
        Intent intent = new Intent(PlayerService.this, PlayerAppWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        intent.putExtra(BUNDLE_SONG_IS_PLAYING, state);
        sendBroadcast(intent);

    }

    private void showLyrics(){

        if(song == null){
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)) {
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String[] split = song.split("/");

        String[] songName = split[split.length-1].replace(".mp3", "").split(" - ");

        Log.w(TAG, "Calling Vagalume API: " + songName[0] + ", " + songName[1]);
        VagalumeService vagalume = retrofit.create(VagalumeService.class);
        Call<VagalumeLyric> call = vagalume.GetLyrics(songName[0], songName[1]);

        call.enqueue(new Callback<VagalumeLyric>() {
            @Override
            public void onResponse(Response<VagalumeLyric> response, Retrofit retrofit) {
                VagalumeLyric result = response.body();

                if(result != null && result.getMus() != null && result.getMus().size() > 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlayerService.this);
                    builder.setTitle(R.string.label_lyrics);

                    VagalumeLyric.VagalumeSong vagalumeSong = result.getMus().get(0);

                    builder.setMessage(result.getArt().getName() + " - " + vagalumeSong.getName() + "\n\n" + vagalumeSong.getText());
                    builder.setPositiveButton(android.R.string.ok, null);

                    dialog = builder.create();
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Request error", t);
            }
        });


    }

}
