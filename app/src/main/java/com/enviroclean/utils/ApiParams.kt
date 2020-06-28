package com.enviroclean.utils

/**
 * Created by imobdev-paresh on 27,November,2019
 */
object ApiParams {
        /*[start]register api param*/
        const val DEVICE_TYPE="device_type"
        const val DEVICE_TOKEN="device_token"
        /*[end]register api param*/

        /*[start]login api param*/
        const val U_USERNAME="u_username"
        const val U_PASSWORD="u_password"
        const val U_EMAIL="u_email"
         /*[end]login api param*/

        /*[start]work assignments screen*/
        const val PAGE="page"
        const val TYPE="type"
        const val DATE="date"
        const val WEEK_NO="weekno"
        const val MONTH="month"
        /*[end]work assignments screen*/

        /*[start]checkin screen*/
        const val COMM_ID="comm_id"
        const val SCH_ID="sch_id"
        const val LATITUDE="latitude"
        const val LONGITUDE="longitude"
        const val USER_IMGES="user_image"
        /*[end]checkin screen*/

        /*[start] scan screen*/
        const val SC_COMM_ID="sc_comm_id"
        const val SC_SCH_ID="sc_sch_id"
        const val SC_LATITUDE="sc_latitude"
        const val SC_LONGITUDE="sc_longitude"
        const val SC_QRCODE="sc_qrcode"
        const val QR_ID="qr_id"
        /*[end] scan screen*/


        /*[START]violation*/
        const val VIO_COMM_ID="vio_comm_id"
        const val VIO_SCH_ID="vio_sch_id"
        const val VIO_LATITUDE="vio_latitude"
        const val VIO_LONGITUDE="vio_longitude"
        const val VIO_NAME="vio_name"
        const val VIO_DESC="vio_desc"
        const val VIO_QR_CODE="vio_qrcode"
        const val VIO_UNIT_NUMBER="vio_unit_number"
        const val VIO_REASON="vio_reason"
        const val VIO_IMAGES="vio_images"
        const val VIO_AREA_ID="vio_area_id"
        const val VIO_IDS="vio_ids"
        const val VIO_TITLE="vio_title"
        const val VIO_REASON_ID="vio_reason_id"
        const val VIO_DESCRIPTION="vio_description"
        const val VIO_OTHER_REASON="vio_other_reason"

        /*[END]violation*/
        const val VIO_ID="vio_id"

        /* Create Schedule */
        const val SCH_NAME="sch_name"
        const val SCH_IN_TIME="sch_intime"
        const val SCH_OUT_TIME="sch_outtime"
        const val SCH_DAYS="days"

        const val SEARCH_KEY="searchkey"

        /* Assign valets*/
        const val ASSIGN_U_ID="assign_u_ids"

        /* Tracking*/
        const val TRACKUSERID="track_user_id"

        /*[start]update user details*/
        const val USER_MOBILE="u_mobile_number"
        const val USER_F_NAME="u_first_name"
        const val USER_EMAIL="u_email"
        const val USER_DOB="u_dob"
        const val USER_GENDER="u_gender"
        const val USER_IMAGES="u_image"
        const val USER_ADDRESS="u_location"
        /*[end]update user details*/

        /*[start]change password*/
        const val CURRENT_PASSWORD="current_password"
        const val NEW_PASSWORD="new_password"
        const val CONFIRM_PASSWORD="confirm_password"
        /*[end]change password*/
        const val REASON_ID="reason_id"
        const val AREA_ID="area_id"

        /*Twilio access Token */
        const val KEY_IDENTITY = "identity"
        const val KEY_USER_TO_ID = "u_to_id"
}