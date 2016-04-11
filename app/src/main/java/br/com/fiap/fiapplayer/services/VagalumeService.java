package br.com.fiap.fiapplayer.services;

import br.com.fiap.fiapplayer.models.VagalumeLyric;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface VagalumeService {

    @GET("/search.php")
    Call<VagalumeLyric> GetLyrics(@Query("art") String artist, @Query("mus") String song);

}
