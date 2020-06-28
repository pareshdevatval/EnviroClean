package com.enviroclean.utils

object AppConstants {

    // const val WEB_SOCKET_API_URL="ws://reviewprototypes.com:8081"
    const val VER_NAME="V-0.9.0"
    const val WEB_SOCKET_API_URL = "ws://enviroclean.reviewprototypes.com:8081"
 //    const val WEB_SOCKET_API_URL = "ws://192.168.1.10:8081"

   // const val WEB_SOCKET_API_URL = "ws://192.168.1.12:8081"
    const val SPLASH_TIME: Long = 2000
    const val DEFAULT_DATE_FORMAT: String = "dd/MM/yyyy"
    val PICK_IMAGE_CAMERA_REQUEST_CODE: Int = 10002
    val PICK_IMAGE_GALLERY_REQUEST_CODE: Int = 10003
    val PICK_VIDEO_REQUEST_CODE: Int = 10004
    val STORAGE_PERMISSION:Int=10007
    const val IMAGES_QUALITY = 20
    const val ANDROID_DEVICE_TYPE = "android"
    const val REGISTER_MODEL = "register_model"



    /*Bundle*/
    val KEY_TUTORIAL = "tutorial"

    /* Prefs Constant KEY*/
    const val KEY_REMEMBER_ME = "is_remember_me"
    const val KEY_USER_TYPE = "user_type"// 1- Manager 2-valet

    const val SUCCESSFULLY_CODE = 200
    const val REMAINING = 1
    const val SCAN = 2
    const val VIOLATION = 3

    const val WORK_TYPE_DAY = "1"
    const val WORK_TYPE_WEEK = "2"
    const val COMMUNITY_USER_CHAT = "1"
    const val OTHER_USER_CHAT = "2"
    const val Images_PATH = "/storage/emulated/0/"
    const val COMM_ID = "comm_id"
    const val COMM_SCH_ID = "commSchId"
    const val QR_CODE_ID = "qr_code_id"
    const val AREA_ID = "area_id"
    const val QR_CODE_NAME = "qr_code_name"
    const val QR_TEXT = "qr_text"
    const val WORK_TYPE = "workTypeDay"
    const val SELECTED_DATE = "selectedDate"
    const val WEEK = "week"
    const val IS_CHEKIN = "is_chek_in"
    const val VIOLATION_ID = "violation_id"
    const val SCHID = "sch_id"
    const val VALETDATA = "valetData"

    const val CHACKIN_VALETS = "2"
    const val CHACKIN_MANAGER = "1"
    const val CHECKINDATA = "check_in_data"

    val CHECK_DATA_RESPONSE = "checkDataResponse"

    val USER_IMAGES_PATH = "http://enviroclean.reviewprototypes.com/public/image.php?url="

    val FILTER_ALL = "All"
    val FILTER_PENDDING = "Pending"
    val FILTER_SCAN = "Scanned"
    val FILTER_VIOLARION = "violation"

    const val RED_BACKGROUND = "red"
    const val CALL_RECEVED = "call_receved"
    const val GREEN_BACKGROUND = "green"
    const val LATTITUDE = "latidute"
    const val LONGITUDE = "longitude"
    const val IS_LOGIN_WITH_TOUCH_ID = "is_login_with_touch_id"
    const val IS_FINGER_REMOVE = "is_finger_remove"
    const val SELECTED_AREA_LIST_ID = "selected_area_list_id"
    const val SELECTED_AREA_LIST_NAME = "selected_area_list_name"
    const val SELECTED_VIOLATION_REASON_ID = "selected_violation_reason_id"
    const val SELECTED_VIOLATION_REASON_NAME = "selected_violation_reason_name"
    const val IS_CONNECTED = "is_connected"
    const val NOTIFICATION_COUNT = "notification_count"
    const val AUTO_LOGOUT = "auto_logout"
    const val IS_DES_CONNECTED = "is_desconnected"
    const val REFRESH_FREGMENT="refresh_fragment"
    const val WRONG_QR_CODE="wrong_qr_code_scan"
    const val FORGOTE_EMAIL="forgote_email"
    const val PDF_URL="pdf_url"

    const val CHAT_USER_NAME="chat_user_name"
    const val CHAT_UNIQUE_NAME="chat_uniqueName"
    const val CHAT_USER_ID="chat_user_id"
    const val CHAT_SENDER_NAME="chat_sender_name"
    const val CHAT_RECEIVER_IMAGE="chat_receiver_image"
    const val RECEVER_USER_ID="receiver_id"

    const val MESSAGES_FAREBASE="messages_firebase"
    const val TITLE_FAREBASE="title_firebase"
    const val IS_FIREBASE_NOTIFICATION="is_firebase_notification"

    /* Voice App */
    const val INCOMING_CALL_INVITE = "INCOMING_CALL_INVITE"
    const val CANCELLED_CALL_INVITE = "CANCELLED_CALL_INVITE"
    const val INCOMING_CALL_NOTIFICATION_ID = "INCOMING_CALL_NOTIFICATION_ID"
    const val ACTION_INCOMING_CALL = "ACTION_INCOMING_CALL"
    const val ACTION_CANCEL_CALL = "ACTION_CANCEL_CALL"
    const val ACTION_FCM_TOKEN = "ACTION_FCM_TOKEN"
    const val KEY_TO="call_to"
    const val KEY_FROM="call_from"

    const val VOICE_ACCESS_TOKEN_URL="http://enviroclean.reviewprototypes.com/mycustom_twilio_walky_talky/accessToken.php"

   const val REFRESH_REMAINING_COUNT="refresh_remaining_count"

 const val IS_NOTIFICATION_POP_UP="is_notification_pop_up"
 const val IS_DISPLAY_CKECK_OUT_POP_UP="is_display_check_out_pop_up"
}