package br.com.fiap.fiapplayer.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import br.com.fiap.fiapplayer.events.PlayerEvent;
import de.greenrobot.event.EventBus;

public class HeadsetReceiver extends BroadcastReceiver {
    private static final String TAG = "HeadsetReceiver";

    public HeadsetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    Log.d(TAG, "Headset unplugged");
                    EventBus.getDefault().post(new PlayerEvent(PlayerEvent.ActionType.PAUSE));
                    break;
                case 1:
                    Log.d(TAG, "Headset plugged");
                    break;
            }
        }
    }
}
