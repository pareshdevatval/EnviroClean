package com.enviroclean.utils.filePick

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*

class FilePickUtils : LifeCycleCallBackManager {
    private var mOnFileChoose: OnFileChoose? = null
    private var imageUrl: Uri? = null
    private var requestCode: Int = 0
    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var allowCrop: Boolean = false
    private val allowDelete: Boolean = false

    private val fileUrls = ArrayList<String>()
    private var isFixedRatio: Boolean = false

    constructor(activity: Activity, mOnFileChoose: OnFileChoose) : super() {
        this.activity = activity
        this.mOnFileChoose = mOnFileChoose
    }

     constructor(fragment: Fragment, mOnFileChoose: OnFileChoose) : super() {
         this.fragment = fragment
         this.activity = fragment.activity!!
         this.mOnFileChoose = mOnFileChoose
     }


    fun getCallBackManager(): LifeCycleCallBackManager {
        return this
    }

    fun requestImageGallery(requestCode: Int, allowCrop: Boolean, isFixedRatio: Boolean) {
        this.requestCode = requestCode
        this.allowCrop = allowCrop
        this.isFixedRatio = isFixedRatio
        val hasStoragePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (hasStoragePermission) {
            selectImageFromGallery()
        } else {
            requestPermissionForExternalStorage()
        }
    }

    fun selectImageFromGallery() {
        val pictureActionIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pictureActionIntent, GALLERY_PICTURE)
    }

    fun requestImageCamera(requestCode: Int, allowCrop: Boolean, isFixedRatio: Boolean) {
        this.requestCode = requestCode
        this.allowCrop = allowCrop
        this.isFixedRatio = isFixedRatio
        val hasCameraPermission = checkPermission(Manifest.permission.CAMERA)
        val hasStoragePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (hasCameraPermission && hasStoragePermission) {
            selectImageFromCamera()
        } else if (!hasCameraPermission && !hasStoragePermission) {
            requestPermissionForCameraStorage()
        } else if (!hasCameraPermission) {
            requestPermissionForCamera()
        } else {
            requestPermissionForCameraButStorage()
        }
    }

    fun selectImageFromCamera() {
        var photoFile: File? = null

        var fileUri: FileUri? = null
        if (activity != null) {
            fileUri = AppUtils.createImageFile(activity!!, "CAMERA")
        }
        if (fileUri == null) {
            return
        }
        photoFile = fileUri.file
        imageUrl = fileUri.imageUrl

        if (photoFile != null) {
            /*val photoURI: Uri = FileProvider.getUriForFile(activity!!,
                    BuildConfig.APPLICATION_ID + ".provider", photoFile)*/
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl)//imageUrl
            intent.putExtra(
                MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, CAMERA_PICTURE)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionForCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        requestPermissionWithRationale(permissions, CAMERA_PERMISSION, "Camera")
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionForCameraButStorage() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestPermissionWithRationale(permissions, CAMERA_BUT_STORAGE_PERMISSION, "Storage")
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionForExternalStorage() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestPermissionWithRationale(permissions, STORAGE_PERMISSION_IMAGE, "Storage")
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionForCameraStorage() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission
                .READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestPermissionWithRationale(permissions, STORAGE_PERMISSION_CAMERA, "Camera & Storage")
    }

    //Activity and Fragment Base Methods
    private fun startActivityForResult(intent: Intent, requestCode: Int) {
        if (fragment != null) {
            fragment!!.startActivityForResult(intent, requestCode)
        } else if (activity != null) {
            activity!!.startActivityForResult(intent, requestCode)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(
            activity!!,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestPermissionWithRationale(
        permissions: Array<String>, requestCode: Int,
        rationaleDialogText: String
    ) {
        var showRationale = false
        for (permission in permissions) {
            if (activity!!.shouldShowRequestPermissionRationale(permission)) {
                //showRationale = true  <-- commenting
                showRationale = false
            }
        }

        if (showRationale) {
            val builder = AlertDialog.Builder(activity!!).setPositiveButton(
                "AGREE"
            ) { dialog, which ->
                dialog.dismiss()
                requestPermissions(permissions, requestCode)
            }
                .setNegativeButton("DENY") { dialog, which -> dialog.dismiss() }
                .setMessage(
                    "Allow "
                            + activity!!.getString(R.string.app_name)
                            + " to access "
                            + rationaleDialogText
                            + "?"
                )
            builder.create().show()
        } else {
            requestPermissions(permissions, requestCode)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        if (fragment != null) {
            fragment!!.requestPermissions(permissions, requestCode)
        } else if (activity != null) {
            activity!!.requestPermissions(permissions, requestCode)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSION_IMAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromGallery()
        } else if (requestCode == STORAGE_PERMISSION_CAMERA
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            selectImageFromCamera()
        } else if (requestCode == CAMERA_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromCamera()
        } else if (requestCode == CAMERA_BUT_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromCamera()
        } else {
            /*var i = 0
            val len = permissions.size
            while (i < len) {
                val permission = permissions[i]
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    val showRationale = activity!!.shouldShowRequestPermissionRationale(permission)
                    if (Manifest.permission.CAMERA == permission) {
                        if (!showRationale) {
                            val builder = AlertDialog.Builder(activity!!).setPositiveButton(
                                "GO TO SETTING"
                            ) { dialog, which ->
                                dialog.dismiss()
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", activity!!.packageName, null)
                                intent.data = uri
                                startActivityForResult(intent, 121)
                            }
                                .setNegativeButton("DENY") { dialog, which -> dialog.dismiss() }
                                .setTitle("Permission denied")
                                .setMessage("Without camera permission the app is unable to capture photos from camera. Are you sure want to deny this permission?")
                            builder.create().show()
                        } else
                            activity!!.shouldShowRequestPermissionRationale(permission)
                    } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE == permission || Manifest.permission.READ_EXTERNAL_STORAGE == permission) {
                        if (!showRationale) {
                            showAlertDialog("Without storage permission the app is unable to open gallery or to save photos. Are you sure want to deny this permission?")
                        } else
                            activity!!.shouldShowRequestPermissionRationale(permission)
                    }
                }
                i++
            }*/
        }
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(activity!!).setPositiveButton(
            "GO TO SETTING"
        ) { dialog, which ->
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity!!.packageName, null)
            intent.data = uri
            startActivityForResult(intent, SETTING_SCREEN_FOR_PERMISSION)
        }
            .setNegativeButton("DENY") { dialog, which -> dialog.dismiss() }
            .setTitle("Permission denied")
            .setMessage(message)
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val hasStoragePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && checkPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (resultCode == Activity.RESULT_OK && hasStoragePermission) {
            val uri: Uri?
            when (requestCode) {
                GALLERY_PICTURE -> if (allowCrop) {
                    performCrop(data?.data)
                } else {
                    /*onFileChoose(data.getData().toString());*/
                    performImageProcessing(data?.data!!.toString(), FileType.IMG_FILE)
                }
                CAMERA_PICTURE -> {
                    uri = imageUrl
                    if (allowCrop) {
                        performCrop(uri)
                    } else {
                        /*onFileChoose(uri.getPath());*/
                        performImageProcessing(uri!!.toString(), FileType.IMG_FILE)
                    }
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    performImageProcessing(resultUri.toString(), FileType.IMG_FILE)
                }
            }

        }
    }

    override fun onDestroy() {
        // activity = null
        //  fragment = null
        mOnFileChoose = null
        //To delete Files on exit
        if (!fileUrls.isEmpty() && allowDelete) {
            for (fileUrl in fileUrls) {
                val file = File(fileUrl)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    override fun onStartActivity() {

    }

    //This method is for compress image
    private fun performImageProcessing(imageUrl: String, mFileType: FileType) {

        Single.just(compressImage(imageUrl)).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<String> {
                override fun onSuccess(t: String) {
                    onFileChoose(t)
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                }
            })


    }

    fun compressImage(imageUri: String): String {

        val filePath = getRealPathFromURI(imageUri)
        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        //		by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //		you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)

        /* if (bmp == null) {
            bmp = BitmapFactory.decodeFile(Utility.getWorkingDirectory() + "/" + "CROP" + imageUri.split("CROP")[imageUri.split("CROP").length - 1]);
        }
*/
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        //		max Height and width values of the compressed image is taken as 816x612

        val maxHeight = 616.0f
        val maxWidth = 816.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight
        Log.e("IMGES_SIZE","--->"+maxHeight.toInt())
        //		width and height values are set maintaining the aspect ratio of the image
        Log.d("IMAGE", "actualHeight=" + actualHeight + "actualWidth=" + actualWidth + "")
        if (actualHeight != -1 || actualWidth != -1) {
            Log.e("IMGES_SIZE","---> -1")
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        else {
            actualHeight = maxHeight.toInt()
            actualWidth = maxWidth.toInt()
        }
        //		setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

        //		inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

        //		this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            //			load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp, middleX - bmp.width / 2, middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix, true
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var out: FileOutputStream? = null
        val filename = activity?.let { AppUtils.createImageFile(it, "")!!.file?.absolutePath }
        try {
            out = FileOutputStream(filename)

            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, AppConstants.IMAGES_QUALITY, out)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return filename!!
    }

    private fun getRealPathFromURI(contentURI: String): String {
        val contentUri = Uri.parse(contentURI)
        val cursor = activity?.contentResolver?.query(contentUri, null, null, null, null)
        return (if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            if (index > 0) {
                cursor.getString(index)
            } else "${AppUtils.getWorkingDirectory()}/${cursor.getString(0)}"
        }).toString()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    private fun onFileChoose(uri: String) {
        if (mOnFileChoose != null) {
            mOnFileChoose!!.onFileChoose(uri, requestCode)
        }
    }

    private fun performCrop(uri: Uri?) {
        val cropFile = activity?.let { AppUtils.createImageFile(it, "CROP") }
        if (fragment != null) {
            CropImage.activity(uri).setOutputUri(cropFile?.imageUrl)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .start(activity!!, fragment!!)

        } else {
            CropImage.activity(uri).setOutputUri(cropFile?.imageUrl)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .start(activity!!)
        }
    }

    private enum class FileType {
        IMG_FILE
    }

    interface OnFileChoose {
        fun onFileChoose(fileUri: String, requestCode: Int)
    }

    companion object {

        private const val CAMERA_PICTURE = 10
        private const val GALLERY_PICTURE = 11
        const val STORAGE_PERMISSION_IMAGE = 111
        private const val STORAGE_PERMISSION_CAMERA = 112
        const val CAMERA_PERMISSION = 115
        private const val CAMERA_BUT_STORAGE_PERMISSION = 116
        private const val SETTING_SCREEN_FOR_PERMISSION = 117
    }


}