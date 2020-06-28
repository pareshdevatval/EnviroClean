package com.enviroclean.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.enviroclean.BuildConfig
import com.enviroclean.R
import com.enviroclean.api.ApiClient
import com.enviroclean.api.ApiService
import com.enviroclean.databinding.DialogInfoBinding
import com.enviroclean.iterfacea.MessageInterface
import com.enviroclean.iterfacea.ReconnectSocketInterface
import com.enviroclean.scoket.SocketClass
import com.enviroclean.ui.activity.SplashActivity
import com.enviroclean.ui.activity.ValetHomeActivity
import com.enviroclean.utils.filePick.FileUri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.*
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object AppUtils {
    val BROADCAST = "com.enviroclean.android.action.broadcast"
    /**
     * A method which returns the state of internet connectivity of user's phone.
     */
    fun hasInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /**
     * A common method used in whole application to show a snack bar
     */
    fun showSnackBar(v: View, msg: String) {
        val mSnackBar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
        val view = mSnackBar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        //  params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(ContextCompat.getColor(v.context, R.color.colorPrimary))
        val mainTextView =
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        mainTextView.setTextAppearance(v.context, R.style.small_poppins_reg)
        // val mainTextView: TextView = view.findViewById(android.support.d esign.R.id.snackbar_text)
        mainTextView.setTextColor(Color.WHITE)
        /*mainTextView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            v.context.resources.getDimension(R.dimen.small)
        )*/
        //mainTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        mainTextView.maxLines = 4
        mainTextView.gravity = Gravity.CENTER_HORIZONTAL
        mSnackBar.show()
    }

    fun snackBar(v: View, msg: String) {
        val snack = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
        val view = snack.view
        view.setBackgroundColor(ContextCompat.getColor(v.context, R.color.colorPrimary))
        snack.show()

    }
    @SuppressLint("WrongConstant")
    fun showToast(context: Context, message: String, length: Int = 1) {
        Toast.makeText(context, message, length).show()
    }

    /**
     * A common method to logout the user and redirect to login screen
     */
    fun logoutUser(context: Context?) {
        context?.let {
            /*Prefs.getInstance(it)?.userDataModel = null
            Prefs.getInstance(it)?.accessToken = ""
            val i = Intent(context, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            it.startActivity(i)*/
        }
    }

    fun formatDate(year: Int, month: Int, day: Int, opFormat: String): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = 0
        cal.set(year, month, day)
        val date = cal.time
        val sdf = SimpleDateFormat(opFormat, Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDate(date: Date, opFormat: String = AppConstants.DEFAULT_DATE_FORMAT): String {
        return SimpleDateFormat(opFormat, Locale.getDefault()).format(date)
    }

    @JvmStatic
    fun formatDate(date: String?, opFormat: String = AppConstants.DEFAULT_DATE_FORMAT): String {
        return if (date != null && date != "" && date != "0") {
            SimpleDateFormat(opFormat, Locale.getDefault()).format(Date(date.toLong() * 1000))
        } else {
            ""
        }
    }

    @JvmStatic
    fun formatDate(date: String?): String {
        return if (date != null && date != "" && date != "0") {
            SimpleDateFormat(
                AppConstants.DEFAULT_DATE_FORMAT,
                Locale.getDefault()
            ).format(Date(date.toLong() * 1000))
        } else {
            ""
        }
    }

    /**
     * A method to get the external directory of phone, to store the image file
     */
    fun getWorkingDirectory(): File {
        val directory = File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return directory
    }

    /**
     * This method is for making new jpg file.
     * @param activity Instance of activity
     * @param prefix file name prefix
     */
    fun createImageFile(activity: Activity, prefix: String): FileUri? {
        val fileUri = FileUri()

        var image: File? = null
        try {
            image = File.createTempFile(prefix + System.currentTimeMillis().toString(), ".jpg", getWorkingDirectory())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (image != null) {
            fileUri.file = image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri.imageUrl = (FileProvider.getUriForFile(
                    activity,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    image
                ))
            } else {
                fileUri.imageUrl = (Uri.parse("file:" + image.absolutePath))
            }
        }
        return fileUri
    }

    /**
     * A method to show device keyboard for user input
     */
    fun showKeyboard(activity: Activity?, view: AppCompatEditText) {
        try {
            val inputManager =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            Log.e("Exception showKeyboard", e.toString())
        }
    }

    /**
     * A method to hide the device's keyboard
     */
    fun hideKeyboard(activity: Activity) {
        if (activity.currentFocus != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    /*General method to get ApiService in every viewModel class*/
    fun getApiService(context: Context?): ApiService? {
        context?.let {
            return ApiClient.getApiService(it)
        }
        return null
    }


    /*General method to get Prefs in every viewModel class*/
    fun getPrefs(context: Context?): Prefs? {
        context?.let {
            return Prefs.getInstance(it)
        }
        return null
    }

    fun showInfoDialog(
        context: Context?, title: String = "", message: String = "",
        ctaButton: String = ""
    ) {
        context?.let {
            val inflater = LayoutInflater.from(it)
            val binding: DialogInfoBinding = DialogInfoBinding.inflate(inflater)

            val dialog = Dialog(it)
            dialog.setContentView(binding.root)
            dialog.setCanceledOnTouchOutside(false)

            binding.tvTitle.text = title
            binding.tvMessage.text = message
            binding.btnCta.text = ctaButton

            binding.btnCta.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }
    }

    fun startFromRightToLeft(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.trans_left_in,
            R.anim.trans_left_out
        )
    }

    fun finishFromLeftToRight(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.trans_right_in,
            R.anim.trans_right_out
        )
    }

    fun startFromBottomToUp(activity: Activity) {
        activity.overridePendingTransition(R.anim.trans_bottom_up, R.anim.no_animation)
    }
    fun finishFromRightToLeft(activity: Activity) {
        activity.overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out)
    }




    /*[START]load Glide images*/
    fun loadImages(
        context: Context,
        imageView: AppCompatImageView,
        url: String?,
        isServerImages: Boolean = false
    ,isViolation:Boolean=false) {
        var imagesUrl: String = ""
        if (isServerImages) {
            imagesUrl = AppConstants.USER_IMAGES_PATH + "" + url
        } else {
            imagesUrl = url!!
        }
        Glide.with(context)
            .load(imagesUrl)
            .placeholder(if(isViolation){R.drawable.violation_placeholder}else{R.drawable.ic_user_plash_holder})
            .fitCenter()
           // .error(if(isViolation){R.drawable.violation_placeholder}else{R.drawable.ic_user_plash_holder})
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
            .into(imageView)
    }
    /*[END]load Glide images*/


    /*
  * get mobile screen height and width
  * */
    fun getScreenSize(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        (context as AppCompatActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return width
    }

    /*[START] LiveData for selected Billing city index*/
    private val selectedBillingCityIndex: MutableLiveData<Int>by lazy {
        MutableLiveData<Int>()
    }

    fun getSelectedBillingCityIndex(): LiveData<Int> {
        return selectedBillingCityIndex
    }

    /*[START] LiveData for selected shipping city index*/
    private val selectedShippingCityIndex: MutableLiveData<Int>by lazy {
        MutableLiveData<Int>()
    }

    fun getSelectedShippingCityIndex(): LiveData<Int> {
        return selectedShippingCityIndex
    }

    /*[START] LiveData for selected Billing city index*/
    private val selectedBillingStateIndex: MutableLiveData<Int>by lazy {
        MutableLiveData<Int>()
    }

    fun getSelectedBillingStateIndex(): LiveData<Int> {
        return selectedBillingStateIndex
    }

    /*[START] LiveData for selected shipping state index*/
    private val selectedShippingStateIndex: MutableLiveData<Int>by lazy {
        MutableLiveData<Int>()
    }

    fun getSelectedShippingStateIndex(): LiveData<Int> {
        return selectedShippingStateIndex
    }


    fun getVersionName(context: Context): String {
        var version: String = ""
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }

     var mSelectedDate:String=""
    /**
     * Date Picker dialog and set Text
     */
    fun openDatePicker(context: Context, textView: AppCompatTextView) {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                //
                // val myFormat = "yyyy/MMMM/dd" // mention the format you need
                val myFormat = "MMMM dd,yyyy"
                val getValue = "dd-MM-yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                val mDate = SimpleDateFormat(getValue, Locale.US)
                mSelectedDate=mDate.format(c.time)
                textView.text = sdf.format(c.time)

            },
            year,
            month,
            day
        )
        c.set(year, month, day)
        dpd.datePicker.maxDate = c.timeInMillis
        dpd.show()
    }

    fun setMonAndDate(date: Int, month: Int): String {
        var mDate: String = ""
        when (date) {
            1 -> {
                mDate = "1st"
            }
            2 -> {
                mDate = "2nd"
            }
            3 -> {
                mDate = "3th"
            }
            4 -> {
                mDate = "4th"
            }
            5 -> {
                mDate = "5th"
            }
            6 -> {
                mDate = "6th"
            }
            7 -> {
                mDate = "7th"
            }
            8 -> {
                mDate = "8th"
            }
            9 -> {
                mDate = "9th"
            }
            10 -> {
                mDate = "10th"
            }
            11 -> {
                mDate = "11th"
            }
            12 -> {
                mDate = "12th"
            }
            13 -> {
                mDate = "13th"
            }
            14 -> {
                mDate = "14th"
            }
            15 -> {
                mDate = "15th"
            }
            16 -> {
                mDate = "16th"
            }
            17 -> {
                mDate = "17th"
            }
            18 -> {
                mDate = "18th"
            }
            19 -> {
                mDate = "19th"
            }
            20 -> {
                mDate = "20th"
            }
            21 -> {
                mDate = "21th"
            }
            22 -> {
                mDate = "22th"
            }
            23 -> {
                mDate = "23th"
            }
            24 -> {
                mDate = "24th"
            }
            25 -> {
                mDate = "25th"
            }
            26 -> {
                mDate = "26th"
            }
            27 -> {
                mDate = "27th"
            }
            28 -> {
                mDate = "28th"
            }
            29 -> {
                mDate = "29th"
            }
            30 -> {
                mDate = "30th"
            }
            31 -> {
                mDate = "31th"
            }
        }
        var mMonth: String = ""
        when (month) {
            0 -> {
                mMonth = "Jan"
            }
            1 -> {
                mMonth = "Feb"
            }
            2 -> {
                mMonth = "Mar"
            }
            3 -> {
                mMonth = "Apr"
            }
            4 -> {
                mMonth = "May"
            }
            5 -> {
                mMonth = "Jun"
            }
            6 -> {
                mMonth = "Jul"
            }
            7 -> {
                mMonth = "Aug"
            }
            8 -> {
                mMonth = "Sep"
            }
            9 -> {
                mMonth = "Oct"
            }
            10 -> {
                mMonth = "Nov"
            }
            11 -> {
                mMonth = "Dec"
            }
            /* 12->{
                 mMonth="Dec"
             }*/
        }
        return mMonth + " " + mDate
    }

    fun dateConvertAMAndPM(time: String): String {
        val startTime = "2013-02-27 $time"
        val tk = StringTokenizer(startTime)
        val date = tk.nextToken()
        val time = tk.nextToken()
        val sdf = SimpleDateFormat("hh:mm:ss")
        val sdfs = SimpleDateFormat("hh a")
        var dt: Date? = null
        try {
            dt = sdf.parse(time)
            println("Time Display: " + sdfs.format(dt)) // <-- I got result here
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return sdfs.format(dt)
    }


    fun countDownStart(
        time: String,
        tvTime: AppCompatTextView,
        clTime: ConstraintLayout
    ) {
        val instance = Calendar.getInstance()
       val  currentDate=instance.get(Calendar.DATE).toString()+"-"+(instance.get(Calendar.MONTH)+1)+"-"+(instance.get(Calendar.YEAR))
        val handler = Handler()
        var runnable: Runnable? = null
        var leftTime:String=""
        Log.e("TIME","----->"+currentDate+" "+time)
        val EVENT_DATE_TIME = /*currentDate+" "+*/getTime(time,true)

        val DATE_FORMAT = "dd-MM-yyyy HH:mm:ss"
        runnable = object : Runnable {
            override fun run() {
                try {
                    handler.postDelayed(this, 1000)
                    val dateFormat = SimpleDateFormat(DATE_FORMAT)
                    val event_date = dateFormat.parse(EVENT_DATE_TIME)
                    val current_date = Date()
                    if (!current_date.after(event_date)) {
                        clTime.visibility=View.VISIBLE
                        val diff = event_date.getTime() - current_date.getTime()
                       // val Days = diff / (24 * 60 * 60 * 1000)
                        val Hours = diff / (60 * 60 * 1000) % 24
                        val Minutes = diff / (60 * 1000) % 60
                        val Seconds = diff / 1000 % 60

                        leftTime=String.format("%02d", Hours)+":"+String.format("%02d", Minutes)+":"+String.format("%02d", Seconds)
                        tvTime.text=leftTime


                    } else {
                        clTime.visibility=View.GONE
                        handler.removeCallbacks(runnable)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        handler.postDelayed(runnable, 0)
    }


    fun getTime(currentDate: String,getFullTime:Boolean=false,withoutAMORPM:Boolean=false): String {
        if (currentDate.isBlank()) {
            return ""
        }
        val instance = Calendar.getInstance()
        val  mCurrentDate=instance.get(Calendar.YEAR).toString()+"-"+(instance.get(Calendar.MONTH)+1)+"-"+(instance.get(Calendar.DATE))
        val mCurrentTime:String=mCurrentDate+" "+currentDate
        Log.e("TIME_____","--->"+mCurrentTime)
        if(withoutAMORPM){
            return mCurrentTime.toDate().formatTo("HH:mm")
        }else{
            if(!getFullTime){
                return mCurrentTime.toDate().formatTo("hh:mm a")
            }else{
                return mCurrentTime.toDate().formatTo("dd-MM-yyyy HH:mm:ss")
            }
        }

    }

    fun convertUTCtoLocal(currentDate: String,fullTime:Boolean=false): String {
        if (currentDate.isBlank()) {
            return ""
        }
        return if(fullTime){
            currentDate.toDate().formatTo("yyyy-MM-dd hh:mm a")
        }else{
            currentDate.toDate().formatTo("hh:mm a")
        }
    }

    fun getTimeSecond(currentDate: String): String {
        if (currentDate.isBlank()) {
            return ""
        }
        val instance = Calendar.getInstance()
        val  mCurrentDate=instance.get(Calendar.YEAR).toString()+"-"+(instance.get(Calendar.MONTH)+1)+"-"+(instance.get(Calendar.DATE))
        val mCurrentTime:String=mCurrentDate+" "+currentDate
        Log.e("TIME_____","--->"+mCurrentTime)

        val tk = StringTokenizer(mCurrentTime)
        val date = tk.nextToken()
        val time = tk.nextToken()

        val sdf = SimpleDateFormat("hh:mm:ss")
        val sdfs = SimpleDateFormat("hh:mm a")
        var dt: Date?=null
        try {
            dt = sdf.parse(time)
            Log.e("Time_Display: " ,"===>"+ sdfs.format(dt)) // <-- I got result here
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return sdfs.format(dt)
    }
    fun getCurrentWeek() :String{
        val instance = Calendar.getInstance()
        val  mCurrentDate=(instance.get(Calendar.DATE))
        var currentWeek:String=""
        Log.e("currentDate",""+mCurrentDate)
        if(mCurrentDate<=7){
            currentWeek="1"

        }else if(mCurrentDate<=14){
            currentWeek="2"

        }else if(mCurrentDate<=21){
            currentWeek="3"

        }else{
            currentWeek="4"

        }
        Log.e("CURENTWEKK_NO","----->"+currentWeek)
        return currentWeek
    }
    fun String.toDate(
        dateFormat: String = "yyyy-MM-dd HH:mm:ss",
        timeZone: TimeZone = TimeZone.getTimeZone("UTC")
    ): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this)
    }

    fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        return formatter.format(this)
    }

    /*[start]socket connection */
    var client: OkHttpClient? = null
    lateinit var webSocket: WebSocket
    lateinit var socketClass: SocketClass

    fun startSocketConnection(commandName:String,userId:String,context: Context,click: MessageInterface,reconcented:ReconnectSocketInterface) {

        client = OkHttpClient()

        /**method to connect replace xxx.xxx.x.xx this ip address with yours */
        val request = Request.Builder().url(AppConstants.WEB_SOCKET_API_URL).build()

        /**Parameter here are just views */
        socketClass = SocketClass(commandName, userId, context, click,reconcented)

        /*use this ws object to send message on button click or anywhere*/
        webSocket = client!!.newWebSocket(request, socketClass)
        /**end here */

    }
    /*[end]socket connection */


    fun getSelectedViolationId(violationString:String,context: Context):String{
      var  violationId:String=""
        when(violationString){
           context.getString(R.string.lbl_1)->{
                violationId="1"
            }
            context.getString(R.string.lbl_2)->{
                violationId="2"
            }
            context.getString(R.string.lbl_3)->{
                violationId="3"
            }
            context.getString(R.string.lbl_4)->{
                violationId="4"
            }
            context.getString(R.string.lbl_5)->{
                violationId="5"
            }
            context.getString(R.string.lbl_6)->{
                violationId="6"
            }
            context.getString(R.string.lbl_7)->{
                violationId="7"
            }
            context.getString(R.string.lbl_8)->{
                violationId="8"
            }
            context.getString(R.string.lbl_9)->{
                violationId="9"
            }
            context.getString(R.string.lbl_10)->{
                violationId="10"
            }
            context.getString(R.string.lbl_11)->{
                violationId="11"
            }
            context.getString(R.string.lbl_12)->{
                violationId="12"
            }
            context.getString(R.string.lbl_13)->{
                violationId="13"
            }
            context.getString(R.string.lbl_14)->{
                violationId="14"
            }
            context.getString(R.string.lbl_15)->{
                violationId="15"
            }
        }

        return violationId
    }

    fun onTimeSet(commIntime: String, commOuttime: String):String {
        var backgroundColor:String=""

        val hourOut = Integer.parseInt(commOuttime.split(":")[0])
        val minOut = Integer.parseInt(commOuttime.split(":")[1])

        val hourIn = Integer.parseInt(commIntime.split(":")[0])
        val minIn = Integer.parseInt(commIntime.split(":")[1])


        val currentTime = Calendar.getInstance()

        val inTime = Calendar.getInstance()
        inTime.set(Calendar.HOUR_OF_DAY, hourIn)
        inTime.set(Calendar.MINUTE, minIn)


        val outTime = Calendar.getInstance()
        outTime.set(Calendar.HOUR_OF_DAY, hourOut)
        outTime.set(Calendar.MINUTE, minOut)



        if (currentTime.timeInMillis > outTime.timeInMillis  ) {
            Log.e("TIME","->red")
            backgroundColor=AppConstants.RED_BACKGROUND
        } else if(currentTime.timeInMillis > inTime.timeInMillis && currentTime.timeInMillis < outTime.timeInMillis) {
            backgroundColor=AppConstants.GREEN_BACKGROUND
            Log.e("TIME","->green")
        }else{
            backgroundColor=""
            Log.e("TIME","->fffff")
        }
        return backgroundColor
    }

  fun onTimeSetSecond(commIntime: String, commOuttime: String):String {
        var backgroundColor:String=""

        val hourOut = Integer.parseInt(commOuttime.split(":")[0])
        val minOut = Integer.parseInt(commOuttime.split(":")[1])
        val secondOut = Integer.parseInt(commOuttime.split(":")[2])

        val hourIn = Integer.parseInt(commIntime.split(":")[0])
        val minIn = Integer.parseInt(commIntime.split(":")[1])
        val secondInt = Integer.parseInt(commIntime.split(":")[2])


        val currentTime = Calendar.getInstance()

        val inTime = Calendar.getInstance()
        inTime.set(Calendar.HOUR_OF_DAY, hourIn)
        inTime.set(Calendar.MINUTE, minIn)
        inTime.set(Calendar.SECOND, secondInt)


        val outTime = Calendar.getInstance()
        outTime.set(Calendar.HOUR_OF_DAY, hourOut)
        outTime.set(Calendar.MINUTE, minOut)
        outTime.set(Calendar.SECOND, secondOut)



        if (currentTime.timeInMillis > outTime.timeInMillis  ) {
            Log.e("TIME","->red")
            backgroundColor=AppConstants.RED_BACKGROUND
        } else if(currentTime.timeInMillis > inTime.timeInMillis && currentTime.timeInMillis < outTime.timeInMillis) {
            backgroundColor=AppConstants.GREEN_BACKGROUND
            Log.e("TIME","->green")
        }else{
            backgroundColor=""
            Log.e("TIME","->fffff")
        }
        return backgroundColor
    }

     fun checktimings(time:String, endtime:String):Boolean {
        val pattern = "hh:mm"
        val sdf = SimpleDateFormat(pattern)
        try
        {
            val date1 = sdf.parse(time)
            val date2 = sdf.parse(endtime)
            return date1.before(date2)
        }
        catch (e:ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun convertGMTTime(time: String):String{
        val parseFormat = SimpleDateFormat("hh:mm")
        val date1 =parseFormat.parse(time)
        val gmtFormat = SimpleDateFormat("HH:mm")
        val gmtTime = TimeZone.getTimeZone("GMT")
        gmtFormat.timeZone = gmtTime
         Log.e("GMT Time: " ,"--->"+ gmtFormat.format(date1))
        return gmtFormat.format(date1)
    }

    fun localToGMT(time:Date): String {
        Log.e("TIME_SELCTED","2---->"+time)
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")


        Log.e("COnvert time","--->"+ Date(sdf.format(time)))

        val sdf1 = SimpleDateFormat("HH:mm")
        sdf1.format(Date(sdf.format(time)))
        Log.e("COnvert time","--->"+ sdf1.format(Date(sdf.format(time))))
        return sdf1.format(Date(sdf.format(time)))
    }

    fun convert24To12HRS(time:String):String{
        val _24HourSDF = SimpleDateFormat("HH:mm:ss")
        val _12HourSDF = SimpleDateFormat("hh:mm a")
        val _24HourDt = _24HourSDF.parse(time)
        return _12HourSDF.format(_24HourDt)
    }

     fun convertGMTT_Local(ourDate: String): String {
        var ourDate = ourDate
        try {
            val formatter = SimpleDateFormat("HH:mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val value = formatter.parse(ourDate)

            val dateFormatter = SimpleDateFormat(" hh:mm a") //this format changeable
            dateFormatter.timeZone = TimeZone.getDefault()
            ourDate = dateFormatter.format(value)

            //Log.d("ourDate", ourDate);
        } catch (e: Exception) {
            ourDate = "00-00-0000 00:00"
        }

        return ourDate
    }


    fun convertDate(date:String):String{
        /*convert 12-02-195 to february 12 ,1995*/
        val from = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        val to = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

        val res = to.format(from.parse(date)!!)
        return res
    }

    fun convertDate2(date:String):String{
        /*convert 12-02-195 to february 12 ,1995*/
        val from = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        val to = SimpleDateFormat("dd MMM", Locale.ENGLISH)

        val res = to.format(from.parse(date)!!)
        return res
    }

    private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

    fun sendNotification(context: Context, message: String) {
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val name = context.getString(R.string.app_name)
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(channel)
        }

        val intent = SplashActivity.newInstance(context.applicationContext)

        val stackBuilder = TaskStackBuilder.create(context)
            .addParentStack(SplashActivity::class.java)
            .addNextIntent(intent)
        val notificationPendingIntent = stackBuilder
            .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(message)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
          //  .setColor(ContextCompat.getColor(context, R.color.blue_toolbar))
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(getUniqueId(), notification)
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

    fun loge(msg:String,tag:String="ENVIRO_CLEAN_APP"){
        Log.e(tag,"----->"+msg)
    }


    fun isAppOnForeground(context: Context, appPackageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == appPackageName) {
                Log.e("app", appPackageName + "-->Forgraound")
                return true
            } else {
                Log.e("app", appPackageName + "-->Background")
            }
        }
        return false
    }

    fun getFiresafeNotifications(prefs: Prefs): String {
        var token: String = ""
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FIREBASE", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                token = task.result?.token.toString()
                prefs.firebaseToken=token
                Log.e("FIREBASE", "-->" + token)

            })
        return token
    }

    fun showPopup(activity: Activity, msg: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
        builder.setTitle(R.string.app_name)
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            activity.onBackPressed()
        }
        builder.show()
    }


    fun saveImageToExternalStorage(image: Bitmap, imageName: String, mimeType: String) {
        val fullPath =
            Environment.getExternalStorageDirectory().absolutePath + "/EnviroClean"
        try {
            val dir = File(fullPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            var fOut: OutputStream? = null
            val file = File(fullPath, imageName)
            if (file.exists())
                file.delete()
            file.createNewFile()
            fOut = FileOutputStream(file)
            Log.e("SAVE FILE NAME", file.name)
            if (mimeType == "jpg") {
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            } else {
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            }

            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            Log.e("saveToExternalStorage()", e.message!!)
        }
    }

    fun saveVideoToExternalStorage(fileName: String,data: ByteArrayOutputStream) {
        val fullPath =
            Environment.getExternalStorageDirectory().absolutePath + "/EnviroClean"
        try {
            val dir = File(fullPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(fullPath, fileName)
            if (file.exists())
                file.delete()
            file.createNewFile()
            Log.e("SAVE FILE NAME", file.name)
            val fos = FileOutputStream(file)
            data.writeTo(fos)
           // fos.write(data)
            fos.close()
        } catch (e: Exception) {
            Log.e("saveToExternalStorage()", e.message!!)
        }
    }


    fun fileIsSaveOrNot(fileName: String): String {
        val filePath = Environment.getExternalStorageDirectory().absolutePath
        val myFile = File("$filePath/EnviroClean/$fileName")
        return if (myFile.exists()){
            myFile.path
        }else{
            ""
        }
    }

     fun doBounceAnimation(targetView: View,context: Context) {
       val anim = loadAnimation(context, R.anim.bounce)
        targetView.startAnimation(anim)
    }

    fun getSizeLengthFile(size:Long):String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb)
            return df.format(size / sizeKb) + " Kb"
        else if (size < sizeGb)
            return df.format(size / sizeMb) + " Mb"
        else if (size < sizeTerra)
            return df.format(size / sizeGb) + " Gb"
        return ""
    }
}