package com.example.weather.screen

import android.annotation.SuppressLint
import android.location.Location
import com.example.weather.R
import com.example.weather.screen.home.WeatherFragment
import com.example.weather.utils.Constant
import com.example.weather.utils.PermissionUtils
import com.example.weather.utils.PermissionUtils.checkPermissions
import com.example.weather.utils.addFragmentToActivity
import com.example.weather.utils.base.BaseActivity
import com.example.weather.utils.distanceBetweenPoints
import com.example.weather.utils.listener.OnFetchListener
import com.example.weather.utils.replaceFragmentToActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : BaseActivity(), OnFetchListener {
    private var mCurrentLocation: Location? = null
    private var mLastLocation: Location? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                mLastLocation = location
                initWeatherView(location)
            }

        PermissionUtils.getLastLocation(
            this,
            this,
            PermissionUtils.isLocationEnabled(this)
        )
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_main
    }

    override fun onDeviceOffline() {
        requestPermissions()
    }

    override fun onLocationRequest() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestPermissions() {
        if (!checkPermissions(this)) {
            PermissionUtils.requestPermissions(this)
        }
    }

    private fun initWeatherView(location: Location?) {
        location?.let { location ->
            addFragmentToActivity(
                supportFragmentManager,
                WeatherFragment.newInstance(location.latitude, location.longitude),
                R.id.container
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionResult(
            requestCode,
            grantResults,
            this
        )
    }

    override fun onRestart() {
        super.onRestart()
        initWeatherView(mCurrentLocation)
    }

    override fun onDataLocation(location: Location?) {
        this.mCurrentLocation = location
        val distance = mCurrentLocation?.let { currentLocation ->
            mLastLocation?.let { lastLocation ->
                distanceBetweenPoints(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    lastLocation.latitude,
                    lastLocation.longitude
                )
            }
        }
        if (distance != null) {
            if (distance > Constant.MIN_DISTANCE_FIRST_TRIGGER) {
                initWeatherView(location)
            }
        } else {
            initWeatherView(location)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
