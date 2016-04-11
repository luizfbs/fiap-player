package br.com.fiap.fiapplayer.models;

import java.util.List;

public class VagalumeLyric {

    private boolean badwords;
    private String type;
    private VagalumeArtist art;
    private List<VagalumeSong> mus;

    public boolean isBadwords() {
        return badwords;
    }

    public void setBadwords(boolean badwords) {
        this.badwords = badwords;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VagalumeArtist getArt() {
        return art;
    }

    public void setArt(VagalumeArtist art) {
        this.art = art;
    }

    public List<VagalumeSong> getMus() {
        return mus;
    }

    public void setMus(List<VagalumeSong> mus) {
        this.mus = mus;
    }

    public class VagalumeArtist{
        private String id;
        private String name;
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public class VagalumeSong{
        private String id;
        private int lang;
        private String name;
        private String text;
        private List<VagalumeSong> translate;
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getLang() {
            return lang;
        }

        public void setLang(int lang) {
            this.lang = lang;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<VagalumeSong> getTranslate() {
            return translate;
        }

        public void setTranslate(List<VagalumeSong> translate) {
            this.translate = translate;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
