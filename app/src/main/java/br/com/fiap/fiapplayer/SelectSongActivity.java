package br.com.fiap.fiapplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.fiap.fiapplayer.adapters.SongAdapter;
import br.com.fiap.fiapplayer.events.PlayerEvent;
import br.com.fiap.fiapplayer.models.Song;
import de.greenrobot.event.EventBus;

public class SelectSongActivity extends AppCompatActivity {

    private static final String TAG = "SelectSongActivity";
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;
    private static final int PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 102;

    public static final String BUNDLE_SELECTED_SONG_NAME = "BUNDLE_SELECTED_SONG_NAME";
    public static final String BUNDLE_SELECTED_SONG_DURATION = "BUNDLE_SELECTED_SONG_DURATION";

    private ListView songsViews;
    private int selectedSong;

    private List<Song> songs;
    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectsong);

        songsViews = (ListView) findViewById(R.id.songsViews);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.label_external_storage_permission_required);

                builder.setMessage(R.string.message_external_storage_permission_required);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requestReadExternalStoragePermission();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {

                requestReadExternalStoragePermission();

            }
        } else {

          fillSongs();

        }
    }

    private void requestReadExternalStoragePermission(){

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

    }

    private void fillSongs() {

        songs = getAllAudio();

        adapter = new SongAdapter(this, songs);

        songsViews.setAdapter(adapter);

        songsViews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            selectedSong = position;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(SelectSongActivity.this)) {

                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(permissionIntent, PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW);

            } else {

                playIt();

            }

            }
        });

    }

    private void playIt(){
        Intent intent = new Intent(SelectSongActivity.this, PlayerAppWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        intent.putExtra(BUNDLE_SELECTED_SONG_NAME, songs.get(selectedSong).getName());
        intent.putExtra(BUNDLE_SELECTED_SONG_DURATION, songs.get(selectedSong).getDuration());
        sendBroadcast(intent);

        PlayerEvent event = new PlayerEvent(PlayerEvent.ActionType.SONG_SELECTED);
        event.setSongSelected(songs.get(selectedSong));
        EventBus.getDefault().post(event);

        finish();
    }

    private List<Song> getAllAudio() {
        Cursor mCursor = null;

        List<Song> result = new ArrayList<>();

        try {
            mCursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, "_id");

            System.out.println("Cursor count is " + mCursor.getCount());

            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();

                do {

                    String file_path = mCursor.getString(mCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String duration = mCursor.getString(mCursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    if (file_path.matches("^(.*)\\-(.*)\\.mp3$")) {
                        System.out.println("Name:" + file_path);
                        System.out.println("Duration " + duration);
                        result.add(new Song(file_path, Integer.parseInt(duration)));
                    }

                } while (mCursor.moveToNext());

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    fillSongs();

                } else {

                    Toast.makeText(this, R.string.message_external_storage_permission_denied,
                            Toast.LENGTH_LONG).show();
                    finish();

                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "O app precisa desta permissão para exibir a letra da música. =/", Toast.LENGTH_LONG)
                        .show();
            }

            playIt();

        }
    }
}

