package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.AvoidType
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.DirectionConverter
import com.directions.route.*
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityLocationRouteBinding
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.MyLocationProvider
import com.enviroclean.viewmodel.LocationRouteViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*


class LocationRouteActivity :
    BaseActivity<LocationRouteViewModel>(LocationRouteActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener, OnMapReadyCallback, RoutingListener,
    MyLocationProvider.MyLocationListener, View.OnClickListener {

    override fun onClick(v: View?) {
        if (isDefualtMapSelected){
            isDefualtMapSelected=false
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            binding.ivMapMode.setImageDrawable(resources.getDrawable(R.drawable.ic_default_view))
        }else{
            binding.ivMapMode.setImageDrawable(resources.getDrawable(R.drawable.ic_satellite_view))
            isDefualtMapSelected=true
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    override fun onRoutingCancelled() {

    }

    override fun onRoutingStart() {

    }

    override fun onRoutingFailure(p0: RouteException?) {

    }

    override fun onRoutingSuccess(route: java.util.ArrayList<Route>?, p1: Int) {

        val center = CameraUpdateFactory.newLatLng(startLatLng)
        val zoom = CameraUpdateFactory.zoomTo(16f)
        mMap.moveCamera(center)
        if (polylines.size > 0) {
            for (poly in polylines) {
                poly.remove()
            }
        }
        polylines = ArrayList()
        for (i in 0 until route!!.size) {
            //In case of more than 5 alternative routes
            val colorIndex = i % COLORS.size
            val polyOptions = PolylineOptions()
            polyOptions.color(resources.getColor(COLORS[colorIndex]))
            polyOptions.width((10 + i * 3).toFloat())
            polyOptions.addAll(route.get(i).points)
            val polyline = mMap.addPolyline(polyOptions)
            polylines.add(polyline)

        }
        /*1 -checkin,2 - checkout, 3 clock in and clock out*/
        var options = MarkerOptions()
        options = MarkerOptions()
        options.position(startLatLng)
        options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking))
            .title("Current Location")
            .snippet(getAddress(startLatLng))
        mMap.addMarker(options)

        options = MarkerOptions()
        options.position(endLatLng)
        options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking))
            .title("Destination Location")
            .snippet(getAddress(endLatLng))
        mMap.addMarker(options)


        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker?): View {

                var info = LinearLayout(this@LocationRouteActivity);
                info.setOrientation(LinearLayout.VERTICAL);
                info.background = resources.getDrawable(R.drawable.background_shape)

                var title = TextView(this@LocationRouteActivity);
                title.setTextColor(
                    ContextCompat.getColor(
                        this@LocationRouteActivity,
                        R.color.blue_text
                    )
                )
                title.setGravity(Gravity.CENTER)
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker!!.title

                var snippet = TextView(this@LocationRouteActivity);
                snippet.setTextColor(
                    ContextCompat.getColor(
                        this@LocationRouteActivity,
                        R.color.Black_title
                    )
                )
                snippet.text = marker.snippet

                info.addView(title)
                info.addView(snippet)

                return info;
            }

            override fun getInfoContents(p0: Marker?): View {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    fun getAddress(latLng: LatLng):String{
        val geocoder:Geocoder
        val addresses:List<Address>
        geocoder = Geocoder(this, Locale.getDefault())
        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        val city = addresses[0].locality
        val state = addresses[0].adminArea
        val country = addresses[0].countryName
        val postalCode = addresses[0].postalCode
        val knownName = addresses[0].featureName
        return address
    }

    private val COLORS = intArrayOf(
        R.color.colorPrimary,
        R.color.colorPrimary,
        R.color.colorPrimary,
        R.color.colorPrimary,
        R.color.colorPrimary
    )
    var polylines: ArrayList<Polyline> = ArrayList()
    override fun onLocationReceived(location: Location?) {
        location?.let {
            Log.e("LATITUDE", "" + it.latitude)
            Log.e("LONGITUDE", "" + it.longitude)

            drawRoute(location)
            locationProvider?.stopLocationUpdates()

            // When we need to get location again, then call below line
            //locationProvider?.startGettingLocations()
        }

    }

    override fun onStop() {
        super.onStop()
        locationProvider?.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

    }

    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityLocationRouteBinding
    private val mViewModel: LocationRouteViewModel by lazy {
        ViewModelProviders.of(this).get(LocationRouteViewModel::class.java)
    }

    override fun getViewModel(): LocationRouteViewModel {
        return mViewModel
    }

    private lateinit var mMap: GoogleMap
    var locationProvider: MyLocationProvider? = null

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    private val mCommLat: String by lazy {
        intent.getStringExtra(AppConstants.LATTITUDE)
    }
    private val mCommLong: String by lazy {
        intent.getStringExtra(AppConstants.LONGITUDE)
    }

    var isDefualtMapSelected:Boolean=true
    companion object {
        fun newInstance(
            context: Context,
            commLatitude: String,
            commLongitude: String
        ): Intent {
            val intent = Intent(context, LocationRouteActivity::class.java)
            intent.putExtra(AppConstants.LATTITUDE, commLatitude)
            intent.putExtra(AppConstants.LONGITUDE, commLongitude)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_route)
        init()
        initToolbar()

    }

    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_directions)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
    }


    fun init() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getLocation()
        binding.ivMapMode.setOnClickListener(this)
    }

    fun getLocation() {
        locationProvider = MyLocationProvider(this, this)
        locationProvider?.init()
    }

    lateinit var startLatLng: LatLng
    lateinit var endLatLng: LatLng
    private fun drawRoute(location: Location) {
        startLatLng = LatLng(location.latitude, location.longitude)
        endLatLng = LatLng(mCommLat.toDouble(), mCommLong.toDouble())
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.5f))
        val bounds = LatLngBounds.Builder()
            .include(latLng)
            .include(LatLng(mCommLat.toDouble(), mCommLong.toDouble())).build()
        val displaySize = Point()
        windowManager.defaultDisplay.getSize(displaySize)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 800, 30))
        val routing = Routing.Builder()
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .withListener(this)
            .alternativeRoutes(false)
            .waypoints(startLatLng, endLatLng)
            .key(getString(R.string.google_maps_key))
            .build()
        routing.execute()

    }

    private fun bitmapDescriptorFromVector(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
        val background = ContextCompat.getDrawable(this, R.drawable.ic_location_live_tracking)
        background!!.setBounds(
            0,
            0,
            background!!.getIntrinsicWidth(),
            background.getIntrinsicHeight()
        )
        val vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId)
        vectorDrawable!!.setBounds(
            40,
            20,
            vectorDrawable!!.getIntrinsicWidth() + 40,
            vectorDrawable.getIntrinsicHeight() + 20
        )
        val bitmap = Bitmap.createBitmap(
            background.getIntrinsicWidth(),
            background.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
     //      vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MyLocationProvider.LOCATION_PERMISSION_REQUEST_CODE) {
            locationProvider?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyLocationProvider.REQUEST_LOCATION_SETTINGS) {
            locationProvider?.onActivityResult(requestCode, resultCode, data)
        }
    }

}
