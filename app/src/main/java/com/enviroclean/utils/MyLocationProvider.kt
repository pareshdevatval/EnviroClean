package com.enviroclean.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*


class MyLocationProvider : GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>,
    LocationListener {

    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var locationListener: MyLocationListener? = null
    var context: Context

    constructor(activity: Activity, locationListener: MyLocationListener) : super() {
        this.activity = activity
        this.locationListener = locationListener
        this.context = activity
    }

    constructor(fragment: Fragment, locationListener: MyLocationListener) : super() {
        this.fragment = fragment
        this.locationListener = locationListener
        this.context = fragment.context!!
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 18
        const val TAG = "Location_Provider"
        /**
         * Constant used in the location settings dialog.
         */
        const val REQUEST_LOCATION_SETTINGS = 0x1
        /*The desired interval for location updates. Inexact. Updates may be more or less frequent.*/
        const val UPDATE_INTERVAL_IN_MILLISECONDS = 10000
        /*The fastest rate for active location updates. Exact. Updates will never be more frequent
        than this value.*/
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

    /* Provides the entry point to Google Play services.*/
    private var mGoogleApiClient: GoogleApiClient? = null
    /* Stores parameters for requests to the FusedLocationProviderApi.*/
    private var mLocationRequest: LocationRequest? = null
    /*Stores the types of location services the client is interested in using.
    * Used for checking settings to determine if the device has optimal location settings.*/
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    /* Represents a geographical location.*/
    private var mCurrentLocation: Location? = null

    /*Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.*/
    private var mRequestingLocationUpdates: Boolean = false

    fun init() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted. Hence requesting the permissions
            if(activity != null) {
                // Runtime permissions from activity
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                // Runtime permissions from fragment
                fragment?.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            // Location permissions are granted.
            initAllServices()
        }

    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                //AppUtils.showToast(activity, "Permissions Granted!")
                // Pemrission is granted now.
                initAllServices()
            } else {
                //AppUtils.showToast(activity, "Permissions Denied!")
            }
        }
    }

    private fun initAllServices() {
        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient()
        createLocationRequest()
        buildLocationSettingsRequest()

        mGoogleApiClient?.connect()

        startGettingLocations()

    }

    /* Builds a GoogleApiClient.*/
    private fun buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient")
        var context: Context? = null
        if (activity == null) {
            context = fragment!!.context
        } else {
            context = activity
        }

        mGoogleApiClient = GoogleApiClient.Builder(context!!)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }

    /* [START] googleApiClient connection callbacks methods*/
    // Runs when a GoogleApiClient object successfully connects.
    @SuppressLint("MissingPermission")
    override fun onConnected(connectionHint: Bundle?) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            //printLocation()
            //locationListener.onLocationReceived(mCurrentLocation)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG, "Connection suspended");
    }
    /* [END] googleApiClient connection callbacks methods*/

    /* [START] googleApiClient OnConnectionFailedListener method*/
    override fun onConnectionFailed(result: ConnectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
    /* [END] googleApiClient OnConnectionFailedListener method*/


    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest?.interval = UPDATE_INTERVAL_IN_MILLISECONDS.toLong()

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest?.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS.toLong()

        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        mLocationRequest?.let {
            builder.addLocationRequest(it)
        }
        mLocationSettingsRequest = builder.build()
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    private fun checkLocationSettings() {
        val result = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient, mLocationSettingsRequest
        )
        result.setResultCallback(this)
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    override fun onResult(locationSettingsResult: LocationSettingsResult) {
        val status = locationSettingsResult.status
        when (status.statusCode) {
            LocationSettingsStatusCodes.SUCCESS -> {
                Log.i(TAG, "All location settings are satisfied.")
                startLocationUpdates();
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                Log.i(
                    TAG,
                    "Location settings are not satisfied. Show the user a dialog to" + "upgrade location settings "
                )
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    if(activity != null) {
                        status.startResolutionForResult(activity, REQUEST_LOCATION_SETTINGS)
                    } else {
                        fragment?.startIntentSenderForResult(status.resolution.intentSender,
                            REQUEST_LOCATION_SETTINGS, null, 0, 0, 0, null)
                    }
                } catch (e: IntentSender.SendIntentException) {
                    Log.i(TAG, "PendingIntent unable to execute request.")
                }
            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                Log.i(
                    TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                            "not created."
                );
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_LOCATION_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.i(TAG, "User chose not to make required location settings changes.");
                    }
                }
            }
        }
    }

    /*Requests location updates from the FusedLocationApi.*/
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient,
            mLocationRequest,
            this
        ).setResultCallback(ResultCallback<Status> {
            mRequestingLocationUpdates = true
        })
    }

    /*Callback that fires when the location changes.*/
    override fun onLocationChanged(location: Location?) {
        mCurrentLocation = location
        //printLocation()
        locationListener?.onLocationReceived(mCurrentLocation)
    }

    /*Removes location updates from the FusedLocationApi.*/
    fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
            mGoogleApiClient,
            this
        ).setResultCallback {
            mRequestingLocationUpdates = false
        }
    }

    /*fun onStart() {
        //mGoogleApiClient?.connect()
    }

    fun onResume() {
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        *//*if (mGoogleApiClient?.isConnected!! && mRequestingLocationUpdates) {
            startLocationUpdates();
        }*//*
    }

    fun onPause() {
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        *//*if (mGoogleApiClient?.isConnected!!) {
            stopLocationUpdates();
        }*//*
    }*/

    fun onStop() {
        mGoogleApiClient?.disconnect()
    }

    fun startGettingLocations() {
        checkLocationSettings()
    }

    interface MyLocationListener {
        fun onLocationReceived(location: Location?)
    }
}