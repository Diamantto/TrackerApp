package com.example.trackerapp.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.trackerapp.R
import com.example.trackerapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.trackerapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.trackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.trackerapp.other.Constants.ACTION_STOP_SERVICE
import com.example.trackerapp.other.Constants.LOCATION_FASTEST_INTERVAL
import com.example.trackerapp.other.Constants.LOCATION_MAX_DELAY_INTERVAL
import com.example.trackerapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.trackerapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.trackerapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.trackerapp.other.Constants.NOTIFICATION_ID
import com.example.trackerapp.other.TrackingUtility
import com.example.trackerapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this) {
            updateLocationTracking(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("resuming service")
                        startForegroundService()
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL
                ).setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                    .setMaxUpdateDelayMillis(LOCATION_MAX_DELAY_INTERVAL).build()
                fusedLocationProviderClient.requestLocationUpdates(
                    request, locationCallback, Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                .setContentTitle("TrackerApp")
                .setContentText("00:00:00")
                .setContentIntent(getMainActivityPendingIntent())
                .setCategory(Notification.CATEGORY_NAVIGATION)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}