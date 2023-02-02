package edu.msu.team9.proj1.Cloud;

import static edu.msu.team9.proj1.Cloud.Cloud.CATALOG_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.CHECK_JOINER;
import static edu.msu.team9.proj1.Cloud.Cloud.INC_COUNT;
import static edu.msu.team9.proj1.Cloud.Cloud.LOGIN_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.LOAD_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.CREATE_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.DELETE_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.UPDATE_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.JOIN_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.TURN_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.COUNT_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.CREATE_GAME_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.UPDATE_TURN_PATH;
import static edu.msu.team9.proj1.Cloud.Cloud.CONNECTION_PATH;


import edu.msu.team9.proj1.Catalog;
import edu.msu.team9.proj1.Cloud.Models.ConnectionStatus;
import edu.msu.team9.proj1.Cloud.Models.GameResult;
import edu.msu.team9.proj1.Cloud.Models.PlayerTurn;
import edu.msu.team9.proj1.Cloud.Models.SetupCount;
import edu.msu.team9.proj1.Cloud.Models.UserStatus;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BattleshipService {
    @GET(LOGIN_PATH)
    Call<UserStatus> login(
            @Query("username") String username,
            @Query("password") String password
    );

    @GET(CHECK_JOINER)
    Call<UserStatus> get_joiner(
            @Query("gameid") String id
    );

    @GET(LOAD_PATH)
    Call<GameResult> get_state(
            @Query("id") String id
    );


    @GET(CATALOG_PATH)
    Call<Catalog> get_games(
            @Query("magic") String magic
    );

    @FormUrlEncoded
    @POST(TURN_PATH)
    Call<PlayerTurn> check_turn(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(CONNECTION_PATH)
    Call<ConnectionStatus> check_connection(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(COUNT_PATH)
    Call<SetupCount> check_setupCount(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(UPDATE_TURN_PATH)
    Call<UserStatus> update_turn(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(INC_COUNT)
    Call<SetupCount> incrementSetupCount(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(CREATE_PATH)
    Call<UserStatus> create_user(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(DELETE_PATH)
    Call<UserStatus> deleteGame(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(JOIN_PATH)
    Call<UserStatus> join_game(@Field("xml") String xmlData);

    //@FormUrlEncoded
    //@POST(CHECK_JOINER)
    //Call<UserStatus> get_joiner(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(COUNT_PATH)
    Call<SetupCount> check_count(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(UPDATE_PATH)
    Call<GameResult> update_state(@Field("xml") String xmlData);

    @FormUrlEncoded
    @POST(CREATE_GAME_PATH)
    Call<GameResult> create_game(@Field("xml") String xmlData);

}
