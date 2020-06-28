package com.enviroclean.utils.filePick

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by imobdev-paresh on 02,January,2020
 */
class CameraPermission :LifeCycleCallBackManager{
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 7
    val REQUEST_IMAGE_CAPTURE = 1
    companion object{
        var mImageBitmap: Bitmap? = null
         var mCurrentPhotoPath: String? = null
    }
    private var fragment: Fragment? = null
    private var activity: Activity? = null
    constructor(activity: Activity,mOnFileChoose: OnFileChoose)
    {
        this.activity=activity
        this.mOnFileChoose = mOnFileChoose

    }
     constructor(fragment: Fragment, mOnFileChoose: OnFileChoose) : super() {
         this.fragment = fragment
         this.activity = fragment.activity!!
         this.mOnFileChoose = mOnFileChoose
     }


    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.packageManager?.let { cameraIntent.resolveActivity(it) } != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.i("TAG", "IOException")
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                if (fragment != null) {
                    fragment!!.startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
                } else if (activity != null) {
                    activity!!.startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val image = File.createTempFile(
            imageFileName, // prefix
            ".jpg", // suffix
            storageDir      // directory
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.d("in fragment on request", "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms: HashMap<String, Int> = HashMap()
                // Initialize the map with both permissions
                perms.put(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    PackageManager.PERMISSION_GRANTED
                )
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED)
                perms.put(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    PackageManager.PERMISSION_GRANTED
                )
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms.put(permissions[i], grantResults[i])
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.CAMERA) === PackageManager.PERMISSION_GRANTED && perms.get(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) === PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(
                            "in fragment on request",
                            "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted"
                        )
                        openCamera()
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(
                            "in fragment on request",
                            "Some permissions are not granted ask again "
                        )
                        //permission is denied (activity is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (activity?.let {
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    it,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            }!! || ActivityCompat.shouldShowRequestPermissionRationale(
                                activity!!,
                                Manifest.permission.CAMERA
                            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                                activity!!,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        ) {
                            showDialogOK("Camera and Storage Permission required for activity app",
                                DialogInterface.OnClickListener { _, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }// proceed with logic by disabling the related features or quit the app.
                                })
                        } else {
                            Toast.makeText(
                                activity,
                                "Go to settings and enable permissions",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }


    override fun onDestroy() {
       
    }

     fun cameraIntent() {
         val builder = StrictMode.VmPolicy.Builder()
         StrictMode.setVmPolicy(builder.build())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkAndRequestPermissions()) {
                openCamera()
            }
        } else {
            openCamera()
        }
    }
    override fun onStartActivity() {
    }

     fun checkAndRequestPermissions(): Boolean {
        val camera = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.CAMERA
            )
        }
        val wtite = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val read = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        val listPermissionsNeeded = arrayListOf<String>()
        if (wtite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    listPermissionsNeeded.toArray(arrayOfNulls<String>(listPermissionsNeeded.size)),
                    REQUEST_ID_MULTIPLE_PERMISSIONS
                )
            }
            openCamera()

            return false
        }
        return true
    }

     fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(
                    activity!!.contentResolver, Uri.parse(mCurrentPhotoPath))
                Log.e("IMAGES_URL","-->"+ mCurrentPhotoPath)
                mCurrentPhotoPath?.let { mOnFileChoose!!.onFileChoose(it,REQUEST_IMAGE_CAPTURE) }

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
    private var mOnFileChoose: OnFileChoose? = null

    interface OnFileChoose {
        fun onFileChoose(fileUri: String, requestCode: Int)
    }
}