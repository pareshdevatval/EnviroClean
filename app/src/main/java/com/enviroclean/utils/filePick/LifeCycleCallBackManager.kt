package com.enviroclean.utils.filePick

import android.content.Intent

interface LifeCycleCallBackManager {

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                   grantResults: IntArray)

    fun onDestroy()

    fun onStartActivity()

}