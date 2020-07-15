package com.example.bemyhero

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class SubApplication : Application(), LifecycleObserver {

    private lateinit var userRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currUserId: String

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppStart(){
        updateOnlineStatus("online")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onAppForegrounded() {
        updateOnlineStatus("online")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onAppBackgrounded() {
        updateOnlineStatus("away")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onAppCrash() {
        updateOnlineStatus("offline")
    }

    private fun updateOnlineStatus(status: String){

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        val currDate: String = getDate()
        val currTime: String = getTime()

        val currStateMap = HashMap<String, String>()
        currStateMap["date"] = currDate
        currStateMap["time"] = currTime
        currStateMap["type"] = status

        if(currUserId != "null"){
            userRef.child(currUserId).child("OnlineStatus")
                .updateChildren(currStateMap as Map<String, Any>)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(): String{
        val calendar: Calendar = Calendar.getInstance()
        val currDate = SimpleDateFormat("dd/MM/yyyy")
        return currDate.format(calendar.time)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String {
        val calendar: Calendar = Calendar.getInstance()
        val currTime = SimpleDateFormat("hh:mm a")
        return currTime.format(calendar.time)
    }
}