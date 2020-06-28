package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.directions.route.*
import com.enviroclean.R
import com.enviroclean.adapter.CommunityCheckInUserListAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.databinding.ActivityLiveTrackingBinding
import com.enviroclean.model.CheckInUser
import com.enviroclean.model.CommunityCheckInListResponse
import com.enviroclean.model.LiveTrackingResponse
import com.enviroclean.model.TrackingData
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.MyLocationProvider
import com.enviroclean.viewmodel.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*
import kotlin.collections.ArrayList


class LiveTrackingActivity :
    BaseActivity<TrackingViewModel>(LiveTrackingActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener, BaseBindingAdapter.ItemClickListener<CheckInUser>,
    RoutingListener,
    OnMapReadyCallback, MyLocationProvider.MyLocationListener, View.OnClickListener {
    override fun onClick(v: View?) {
        if (isDefualtMapSelected){
            isDefualtMapSelected=false
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            binding.ivMapMode.setImageDrawable(resources.getDrawable(R.drawable.ic_default_view))
        }else{
            binding.ivMapMode.setImageDrawable(resources.getDrawable(R.drawable.ic_satellite_view))
            isDefualtMapSelected=true
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }    }

    override fun onRoutingCancelled() {
    }

    override fun onRoutingStart() {
    }

    override fun onRoutingFailure(p0: RouteException?) {
    }

    override fun onRoutingSuccess(route: java.util.ArrayList<Route>?, p1: Int) {


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
            polyOptions.addAll(route[i].points)
            val polyline = mMap.addPolyline(polyOptions)
            polylines.add(polyline)
            /*  Toast.makeText(
                  getApplicationContext(),
                  "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(
                      i
                  ).getDurationValue(),
                  Toast.LENGTH_SHORT
              ).show()*/
        }
        /*1 -checkin,2 - checkout, 3 clock in and clock out*/
        var options = MarkerOptions()
        for (i in 0 until trackingList.size) {
            if (trackingList[i]!!.ur_type == 1) {
                options.position(
                    LatLng(
                        trackingList[i]!!.ur_latitude.toDouble(),
                        trackingList[i]!!.ur_longitude.toDouble()
                    )
                )
                options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking_green))
                mMap.addMarker(options)
            } else if (trackingList[i]!!.ur_type == 2) {
                options = MarkerOptions()
                options.position(
                    LatLng(
                        trackingList[i]!!.ur_latitude.toDouble(),
                        trackingList[i]!!.ur_longitude.toDouble()
                    )
                )
                options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking_red))
                mMap.addMarker(options)

            } else {
                options = MarkerOptions()
                options.position(
                    LatLng(
                        trackingList[i]!!.ur_latitude.toDouble(),
                        trackingList[i]!!.ur_longitude.toDouble()
                    )
                )

                val instance = Calendar.getInstance()
                val  mCurrentDate=instance.get(Calendar.YEAR).toString()+"-"+(instance.get(Calendar.MONTH)+1)+"-"+(instance.get(
                    Calendar.DATE))

                options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking))
                    .title(trackingList[i]!!.ur_area_name)
                    .snippet("         Clock in -" + AppUtils.convertUTCtoLocal(mCurrentDate+" "+trackingList[i]!!.ur_clockin) + "         \n"+
                            "         Clock out - " + if(trackingList[i]!!.ur_clockout.isNullOrEmpty()){""}else{
                        AppUtils.convertUTCtoLocal(mCurrentDate+" "+trackingList[i]!!.ur_clockout)})

                mMap.addMarker(options)
            }

        }
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker?): View {

                var info = LinearLayout(this@LiveTrackingActivity);
                info.setOrientation(LinearLayout.VERTICAL);
                info.background = resources.getDrawable(R.drawable.background_shape)

                var title = TextView(this@LiveTrackingActivity);
                title.setTextColor(
                    ContextCompat.getColor(
                        this@LiveTrackingActivity,
                        R.color.blue_text
                    )
                )
                title.setGravity(Gravity.CENTER)
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker!!.title

                var snippet = TextView(this@LiveTrackingActivity);
                snippet.setTextColor(
                    ContextCompat.getColor(
                        this@LiveTrackingActivity,
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


            //  drawRoute(location)
            drawPolyline()
            locationProvider?.stopLocationUpdates()
            // When we need to get location again, then call below line
            //locationProvider?.startGettingLocations()
        }
    }

    private fun drawPolyline() {

        for (traking in trackingList) {
            latLongArr.add(
                LatLng(
                    traking!!.ur_latitude.toDouble(),
                    traking!!.ur_longitude.toDouble()
                )
            )
        }

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latLongArr[0].latitude, latLongArr[0].longitude))
            .zoom(17F)
            .bearing(90F)
            .tilt(40F)
            .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        if (latLongArr.size > 1) {

            val bounds = LatLngBounds.Builder()
                .include(latLongArr[0])
                .include(latLongArr[latLongArr.size - 1]).build()
            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30))

            /*1 -checkin,2 - checkout, 3 clock in and clock out*/
            var options = MarkerOptions()
          //  var isOne:Boolean=true
            for (i in 0 until trackingList.size) {
                if( (trackingList.size-1)!=i){
                    if(trackingList[i]!!.ur_type!=2){
                        var line = mMap.addPolyline(PolylineOptions()
                            .add(LatLng(trackingList[i]!!.ur_latitude.toDouble(),
                                trackingList[i]!!.ur_longitude.toDouble()),
                                LatLng(trackingList[i+1]!!.ur_latitude.toDouble(),
                                    trackingList[i+1]!!.ur_longitude.toDouble()))
                            .width(8f)
                            .color(ContextCompat.getColor(this,R.color.blue_toolbar)))
                    }

                }

                when {
                    trackingList[i]!!.ur_type == 1 -> {

                        options.position(
                            LatLng(
                                trackingList[i]!!.ur_latitude.toDouble(),
                                trackingList[i]!!.ur_longitude.toDouble()
                            )
                        )
                        val instance = Calendar.getInstance()
                        val  mCurrentDate=instance.get(Calendar.YEAR).toString()+"-"+(instance.get(Calendar.MONTH)+1)+"-"+(instance.get(
                            Calendar.DATE))
                        options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking_green))
                            .title("Check in")
                            .snippet(AppUtils.convertUTCtoLocal(mCurrentDate+" "+trackingList[i]!!.ur_clockin
                                    )+"\n"+trackingList[i]!!.ur_area_name)
                        mMap.addMarker(options)
                    }
                    trackingList[i]!!.ur_type == 2 -> {
                        options = MarkerOptions()
                        options.position(
                            LatLng(
                                trackingList[i]!!.ur_latitude.toDouble(),
                                trackingList[i]!!.ur_longitude.toDouble()
                            )
                        )
                        val instance = Calendar.getInstance()
                        val  mCurrentDate=instance.get(Calendar.YEAR).toString()+"-"+(instance.get(Calendar.MONTH)+1)+"-"+(instance.get(
                            Calendar.DATE))
                        options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking_red))
                            .title("Check out")
                            .snippet(AppUtils.convertUTCtoLocal(mCurrentDate+" "+trackingList[i]!!.ur_clockin)+"\n"+trackingList[i]!!.ur_area_name)
                        mMap.addMarker(options)


                    }
                    else -> {
                        options = MarkerOptions()
                        options.position(
                            LatLng(
                                trackingList[i]!!.ur_latitude.toDouble(),
                                trackingList[i]!!.ur_longitude.toDouble()
                            )
                        )

                        val instance = Calendar.getInstance()
                        val  mCurrentDate=instance.get(Calendar.YEAR).toString()+"-"+(instance.get(Calendar.MONTH)+1)+"-"+(instance.get(
                            Calendar.DATE))

                        options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking))
                            .title(trackingList[i]!!.ur_area_name)
                            .snippet("         Clock in -" + AppUtils.convertUTCtoLocal(mCurrentDate+" "+trackingList[i]!!.ur_clockin) + "         \n"+
                                    "         Clock out - " + if(trackingList[i]!!.ur_clockout.isNullOrEmpty()){""}else{
                                AppUtils.convertUTCtoLocal(mCurrentDate+" "+trackingList[i]!!.ur_clockout)})

                        mMap.addMarker(options)
                    }
                }

            }

        } else {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latLongArr[0].latitude,
                        latLongArr[0].longitude
                    ), 13f
                )
            )


            val options = MarkerOptions()
            options.position(LatLng( latLongArr[0].latitude,  latLongArr[0].longitude))
            options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking_green))
            mMap.addMarker(options)
        }

        if(::mMap.isInitialized){
            mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoWindow(marker: Marker?): View {

                    var info = LinearLayout(this@LiveTrackingActivity);
                    info.setOrientation(LinearLayout.VERTICAL);
                    info.background = resources.getDrawable(R.drawable.background_shape)

                    var title = TextView(this@LiveTrackingActivity);
                    title.setTextColor(
                        ContextCompat.getColor(
                            this@LiveTrackingActivity,
                            R.color.blue_text
                        )
                    )
                    title.setGravity(Gravity.CENTER)
                    title.setTypeface(null, Typeface.BOLD)
                    title.text = marker!!.title

                    var snippet = TextView(this@LiveTrackingActivity);
                    snippet.setTextColor(
                        ContextCompat.getColor(
                            this@LiveTrackingActivity,
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
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

    }

    override fun onItemClick(view: View, data: CheckInUser?, position: Int) {
        mMap.clear()
        mViewModel.getTrackingData(
            mCommId,
            mSchID,
            mDate,
            data!!.u_id.toString()
        )
        val mSize=(binding.rvValet.adapter as CommunityCheckInUserListAdapter).items.size
        for (i in 0 until mSize){
            (binding.rvValet.adapter as CommunityCheckInUserListAdapter).items[i]!!.isSelected = i==position
        }
        (binding.rvValet.adapter as CommunityCheckInUserListAdapter).notifyDataSetChanged()
    }

    override fun onLeftIconClicked() {
        onBackPressed()
    }
    var isDefualtMapSelected:Boolean=true

    private lateinit var binding: ActivityLiveTrackingBinding
    private lateinit var adapter: CommunityCheckInUserListAdapter
    private lateinit var mMap: GoogleMap
    var locationProvider: MyLocationProvider? = null
    private val mViewModel: TrackingViewModel by lazy {
        ViewModelProviders.of(this).get(TrackingViewModel::class.java)
    }

    override fun getViewModel(): TrackingViewModel {
        return mViewModel
    }

    private val mCommId: String by lazy {
        intent.getStringExtra(AppConstants.COMM_ID)
    }

    private val mSchID: String by lazy {
        intent.getStringExtra(AppConstants.SCHID)
    }

    private val mDate: String by lazy {
        intent.getStringExtra(AppConstants.SELECTED_DATE)
    }

    // private lateinit var trackingList: ArrayList<TrackingData?>
    //private lateinit var latLongArr: ArrayList<LatLng>
    private var latLongArr: ArrayList<LatLng> = ArrayList()
    private var trackingList: ArrayList<TrackingData?> = ArrayList()

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(context: Context, commId: String, schId: String, date: String): Intent {
            val intent = Intent(context, LiveTrackingActivity::class.java)
            intent.putExtra(AppConstants.COMM_ID, commId)
            intent.putExtra(AppConstants.SCHID, schId)
            intent.putExtra(AppConstants.SELECTED_DATE, date)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_tracking)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        init()

    }

    fun init() {
        initToolbar()
        mViewModel.getValetList(mCommId, mSchID, mDate)
        binding.ivMapMode.setOnClickListener(this)
        setObserver()
        setAdapter()
    }

    private fun setMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getLocation()
    }

    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_live_tacking)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
    }

    private fun setAdapter() {
        adapter = CommunityCheckInUserListAdapter()
        binding.rvValet.adapter = adapter
        adapter.itemClickListener = this
    }
    val checkUserList:ArrayList<CheckInUser?> = ArrayList()
    private fun setObserver() {
        mViewModel.getCommuityCheckInListResponse()
            .observe({ this.lifecycle }, { response: CommunityCheckInListResponse? ->
                if (response!!.status) {
                    binding.clValet.visibility = View.VISIBLE
                    binding.clMap.visibility = View.VISIBLE
                    binding.NoData.visibility = View.GONE
                    for (i in 0 until response.result.size){
                        if(i==0){
                            checkUserList.add(CheckInUser(response.result[i]!!.u_first_name,response.result[i]!!.u_id,response.result[i]!!.u_image,
                                response.result[i]!!.u_last_name,true))
                        }else{
                            checkUserList.add(CheckInUser(response.result[i]!!.u_first_name,response.result[i]!!.u_id,response.result[i]!!.u_image,
                                response.result[i]!!.u_last_name,false))
                        }

                    }
                    adapter.setItem(checkUserList)
                    adapter.notifyDataSetChanged()

                    Handler().postDelayed({
                        mViewModel.getTrackingData(
                            mCommId,
                            mSchID,
                            mDate,
                            response.result[0]!!.u_id.toString()
                        )
                    }, 500)
                } else {
                    binding.clValet.visibility = View.GONE
                    binding.clMap.visibility = View.GONE
                    binding.NoData.visibility = View.VISIBLE
                    binding.NoData.text = response.message
                }
            })

        mViewModel.getLiveTrackingResponse()
            .observe({ this.lifecycle }, { response: LiveTrackingResponse? ->
                if (response!!.status) {
                    trackingList.clear()
                    latLongArr.clear()
                    trackingList = response.result
                    setMap()
                } else {
                    AppUtils.showSnackBar(binding.root, response.message)
                }
            })
    }

    private fun getLocation() {
        locationProvider = MyLocationProvider(this, this)
        locationProvider?.init()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }

    override fun onStop() {
        super.onStop()
        locationProvider?.onStop()
    }

    var list: ArrayList<LatLng> = ArrayList()

    private fun drawRoute(location: Location) {

        for (traking in trackingList) {
            latLongArr.add(
                LatLng(
                    traking!!.ur_latitude.toDouble(),
                    traking!!.ur_longitude.toDouble()
                )
            )
        }
        if (latLongArr.size > 1) {
            val routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(latLongArr)
                .key(getString(R.string.google_maps_key))
                .build();
            routing.execute();
            val bounds = LatLngBounds.Builder()
                .include(latLongArr[0])
                .include(latLongArr[latLongArr.size - 1]).build()
            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30))
        } else {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latLongArr[0].latitude,
                        latLongArr[0].longitude
                    ), 13f
                )
            );

            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        latLongArr[0].latitude,
                        latLongArr[0].longitude
                    )
                )      // Sets the center of the map to location user
                .zoom(17F)                   // Sets the zoom
                .bearing(90F)                // Sets the orientation of the camera to east
                .tilt(40F)                   // Sets the tilt of the camera to 30 degrees
                .build()                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            val options = MarkerOptions()
                    options.position(LatLng( latLongArr[0].latitude,  latLongArr[0].longitude))
                    options.icon(bitmapDescriptorFromVector(R.drawable.ic_location_live_tracking_green))
                    mMap.addMarker(options)
        }


    }

    private fun bitmapDescriptorFromVector(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
        val background = ContextCompat.getDrawable(this, vectorDrawableResourceId)
        background!!.setBounds(
            0,
            0,
            background.intrinsicWidth,
            background.intrinsicHeight
        )
        val vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId)
        vectorDrawable!!.setBounds(
            40,
            20,
            vectorDrawable.intrinsicWidth + 40,
            vectorDrawable.intrinsicHeight + 20
        )
        val bitmap = Bitmap.createBitmap(
            background.intrinsicWidth,
            background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        //  vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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
