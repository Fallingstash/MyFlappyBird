package com.example.myflappybird.data.remote.model

data class ApiGameRecord(
    val id: Int? = null,
    val title: String,        // playerName
    val userId: Int,          // score
    val body: String          // dateMillis.toString() или JSON
)