package br.com.fiap.fiapplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import br.com.fiap.fiapplayer.events.PlayerEvent;
import br.com.fiap.fiapplayer.helpers.DateHelper;
import br.com.fiap.fiapplayer.services.PlayerService;

import de.greenrobot.event.EventBus;

public class PlayerAppWidget extends AppWidgetProvider {

    private static final String TAG = "PlayerAppWidget";

    private static final String SYNC_PLAYPAUSE_CLICKED = "SYNC_PLAYPAUSE_CLICKED";
    private static final String SYNC_STOP_CLICKED = "SYNC_STOP_CLICKED";
    private static final String SYNC_REWIND_CLICKED = "SYNC_REWIND_CLICKED";
    private static final String SYNC_FORWARD_CLICKED = "SYNC_FORWARD_CLICKED";
    private static final String SYNC_SONG_NAME_CLICKED = "SYNC_SONG_NAME_CLICKED";

    private static boolean isPlaying = false;
    private static Intent playerService;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[]appWidgetIds){
        RemoteViews remoteViews;

        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout._player_app_widget);

        watchWidget = new ComponentName(context, PlayerAppWidget.class);

        remoteViews.setOnClickPendingIntent(R.id.playpause, getPendingSelfIntent(context, SYNC_PLAYPAUSE_CLICKED));

        remoteViews.setOnClickPendingIntent(R.id.stop, getPendingSelfIntent(context, SYNC_STOP_CLICKED));

        remoteViews.setOnClickPendingIntent(R.id.rewind, getPendingSelfIntent(context, SYNC_REWIND_CLICKED));

        remoteViews.setOnClickPendingIntent(R.id.forward, getPendingSelfIntent(context, SYNC_FORWARD_CLICKED));

        remoteViews.setOnClickPendingIntent(R.id.songName, getPendingSelfIntent(context, SYNC_SONG_NAME_CLICKED));

        if(playerService == null){
            playerService = new Intent(context, PlayerService.class);
            context.startService(playerService);
        }

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout._player_app_widget);

        if (SYNC_PLAYPAUSE_CLICKED.equals(intent.getAction())) {

            if(!isPlaying){
                EventBus.getDefault().post(new PlayerEvent(PlayerEvent.ActionType.PLAY));
            }else{
                EventBus.getDefault().post(new PlayerEvent(PlayerEvent.ActionType.PAUSE));
            }

        }else if(SYNC_STOP_CLICKED.equals(intent.getAction())){

            EventBus.getDefault().post(new PlayerEvent(PlayerEvent.ActionType.STOP));
            remoteViews.setTextViewText(R.id.songName, context.getString(R.string.song_name));
            remoteViews.setTextViewText(R.id.songCurrent, "00:00");
            remoteViews.setTextViewText(R.id.songDuration, "00:00");

        }else if(SYNC_REWIND_CLICKED.equals(intent.getAction())){

            EventBus.getDefault().post(new PlayerEvent(PlayerEvent.ActionType.REWIND));

        }else if(SYNC_FORWARD_CLICKED.equals(intent.getAction())){

            EventBus.getDefault().post(new PlayerEvent(PlayerEvent.ActionType.FORWARD));

        }else if(SYNC_SONG_NAME_CLICKED.equals(intent.getAction())){

            EventBus.getDefault().post(new PlayerEvent(PlayerEvent.ActionType.SHOW_LYRICS));

        }else if(intent.getStringExtra(SelectSongActivity.BUNDLE_SELECTED_SONG_NAME) != null){

            String songName = intent.getStringExtra(SelectSongActivity.BUNDLE_SELECTED_SONG_NAME);
            remoteViews.setTextViewText(R.id.songName, songName);

            if(intent.getLongExtra(SelectSongActivity.BUNDLE_SELECTED_SONG_DURATION, 0) > 0){

                long duration = intent.getLongExtra(SelectSongActivity.BUNDLE_SELECTED_SONG_DURATION, 0);
                remoteViews.setTextViewText(R.id.songDuration, DateHelper.getTime(duration));

            }

        }else if(intent.getIntExtra(PlayerService.BUNDLE_SONG_IS_PLAYING, 0) > 0){

            int state = intent.getIntExtra(PlayerService.BUNDLE_SONG_IS_PLAYING, 0);
            switch (state){
                case 1:
                    remoteViews.setImageViewResource(R.id.playpause, R.drawable.ic_pause_circle_outline_pressed_24dp);
                    isPlaying = true;
                    break;

                case 2:
                    remoteViews.setImageViewResource(R.id.playpause, R.drawable.ic_play);
                    isPlaying = false;
                    break;
            }

        }else if(intent.getLongExtra(PlayerService.BUNDLE_SONG_CURRENT_POSITION, -1) > 0){

            long currentPosition = intent.getLongExtra(PlayerService.BUNDLE_SONG_CURRENT_POSITION, 0);
            remoteViews.setTextViewText(R.id.songCurrent, DateHelper.getTime(currentPosition));

        }

        ComponentName componentName = new ComponentName(context, PlayerAppWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);

        super.onReceive(context, intent);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onEnabled(Context context){
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context){
        if(playerService != null)
        {
            context.stopService(playerService);
            playerService = null;
        }
        super.onDisabled(context);
    }

}