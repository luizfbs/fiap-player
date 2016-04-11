package br.com.fiap.fiapplayer.events;

import br.com.fiap.fiapplayer.models.Song;

public class PlayerEvent {
    public enum ActionType {
        PLAY, PAUSE, STOP, REWIND, FORWARD, SONG_SELECTED, SHOW_LYRICS
    }

    private ActionType Action;

    private Song SongSelected = null;

    public PlayerEvent(ActionType action) {
        Action = action;
    }

    public ActionType getAction() {
        return Action;
    }

    public void setAction(ActionType action) {
        Action = action;
    }

    public Song getSongSelected() { return SongSelected; }

    public void setSongSelected(Song songSelected) { SongSelected = songSelected; }
}
