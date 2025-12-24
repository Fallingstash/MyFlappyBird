package com.example.myflappybird.data.remote

import com.example.myflappybird.domain.model.GameRecord
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LeaderboardApi {
    @GET("records")
    suspend fun getRecords(): List<GameRecord>

    @POST("records")
    suspend fun addRecord(@Body record: GameRecord): GameRecord
}