package br.com.fiap.fiapplayer.models;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.fiap.fiapplayer.SelectSongActivity;
import br.com.fiap.fiapplayer.helpers.DateHelper;

public class Song {
    private String filePath;
    private long duration;

    public Song(String filePath, long duration) {
        this.filePath = filePath;
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public String getDurationFmt() {
        return DateHelper.getTime(getDuration());
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getName(){

        String[] split = getFilePath().split("/");

        return split[split.length-1].replace(".mp3", "");

    }
}
