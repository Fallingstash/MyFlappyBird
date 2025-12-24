package com.example.myflappybird

import android.app.Application

class MyFlappyBirdApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.initialize(this)
    }
}