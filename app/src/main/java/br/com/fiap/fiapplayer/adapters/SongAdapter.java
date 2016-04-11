package br.com.fiap.fiapplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.com.fiap.fiapplayer.R;
import br.com.fiap.fiapplayer.models.Song;

public class SongAdapter extends BaseAdapter {

    private Context context;
    private List<Song> songs;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;

        Collections.sort(songs, new NameComparator());

        this.songs = songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Song getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();

            convertView = LayoutInflater
                    .from(context)
                    .inflate(R.layout.song_entry, parent, false);

            holder.songName = (TextView) convertView.findViewById(R.id.songName);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Song song = getItem(position);

        holder.songName.setText(song.getName() + " (" + song.getDurationFmt() + ")");

        return convertView;
    }


    private class ViewHolder{

        protected TextView songName;

    }

    public class NameComparator implements Comparator<Song>
    {
        public int compare(Song s1, Song s2)
        {
            return s1.getName().compareTo(s2.getName());
        }
    }
}
