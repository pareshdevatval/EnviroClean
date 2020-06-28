package com.enviroclean.api

import com.enviroclean.model.*
import com.enviroclean.services.DownloadService
import com.enviroclean.utils.AppConstants
import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*
import retrofit2.http.Streaming
import retrofit2.http.GET
import retrofit2.Call


interface ApiService {

    /*login api call*/
    @FormUrlEncoded
    @POST("user/login")
    fun loginApi(@FieldMap params: HashMap<String, String?>): Observable<LoginResponse1>

    /*forgot password api call*/
    @FormUrlEncoded
    @POST("password/email")
    fun forgotPasswordApi(@FieldMap params: HashMap<String, String?>): Observable<BaseResponse>

    @GET("work-list")
    fun getValetWorkListApi(@QueryMap params: HashMap<String, Any?>): Observable<WorkListResponse>

    @GET("community-worklist")
    fun getCheckInCommunityWorkListApi(@QueryMap params: HashMap<String, Any?>): Observable<CheckIngResponse1>

    @Multipart
    @POST("user/checkin")
    fun checkinApi(@PartMap params: HashMap<String, RequestBody>): Observable<CheckIngResponse1>


    @FormUrlEncoded
    @POST("scan")
    fun scanApi(@FieldMap params: HashMap<String, String?>): Observable<DustBinScanResponse>

    @Multipart
    @POST("user/checkout")
    fun checkOutApi(@PartMap params: HashMap<String, RequestBody>): Observable<BaseResponse>

    @GET("community/users")
    fun getCurrentCommunityUsers(@QueryMap params: HashMap<String, Any?>): Observable<CurrentCommunityUsers>

    @Multipart
    @POST("violation")
    fun violationApi(@PartMap params: HashMap<String, RequestBody>): Observable<ViolationResponse>


    @GET("violation-list")
    fun getViolationList(@QueryMap params: HashMap<String, Any?>): Observable<ViolationList>

    @GET("violationdetail")
    fun getViolationDetails(@QueryMap params: HashMap<String, Any?>): Observable<ViolationDetailsResponse>


    @FormUrlEncoded
    @POST("violationsend")
    fun sendViolationToClientApi(@FieldMap params: HashMap<String, Any?>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("violationsend")
    fun sendToClientApi(@FieldMap params: HashMap<String, Any?>): Observable<BaseResponse>


    @GET("community/list")
    fun getCommunityList(@QueryMap params: HashMap<String, Any?>): Observable<CommunityListResponse>

    @FormUrlEncoded
    @POST("community/schedule")
    fun createCommunitySchedule(@FieldMap params: HashMap<String, Any?>): Observable<BaseResponse>

    @GET("community/schedule-list")
    fun getListOfScheduleList(@QueryMap params: HashMap<String, Any?>): Observable<ListOfScheduleResponse>

    @GET("community/valets-list")
    fun getListOfValets(@QueryMap params: HashMap<String, Any?>): Observable<ListOfValetsResponse>


    @FormUrlEncoded
    @POST("community/assignvalets")
    fun assignValets(@FieldMap params: HashMap<String, Any?>): Observable<BaseResponse>

    @GET("community/checkin-list")
    fun geCommunityCheckInList(@QueryMap params: HashMap<String, Any?>): Observable<CommunityCheckInListResponse>

    @GET("user")
    fun getUserDetails(@QueryMap params: HashMap<String, Any?>): Observable<UserDetailsResponse>

    @GET("user/payroll-list")
    fun getPayStub(@QueryMap params: HashMap<String, Any?>): Observable<PayStubResponse>

    @Multipart
    @POST("updateprofileinfo")
    fun updateUserDetails(@PartMap params: HashMap<String, RequestBody>): Observable<UserDetailsResponse>


    @FormUrlEncoded
    @POST("user/tracking")
    fun getTrackingList(@FieldMap params: HashMap<String, Any?>): Observable<LiveTrackingResponse>

    @FormUrlEncoded
    @POST("changepassword")
    fun changePassword(@FieldMap params: HashMap<String, Any?>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("user/clockin")
    fun clockInAndOutApiCall(@FieldMap params: HashMap<String, Any?>): Observable<BaseResponse>

    @POST("user/logout")
    fun logOutApi(): Observable<BaseResponse>

    @POST("clearnotifications")
    fun clearAllNotification(): Observable<BaseResponse>

    @Streaming
    @GET(".")
    fun downloadFile(): Call<ResponseBody>

    @GET("twiliotoken")
    fun getTwilioAccessToken(@QueryMap params: HashMap<String, Any?>): Observable<ChatAccessTokenResponse>


    @FormUrlEncoded
    @POST("twiliochannel")
    fun createChannel(@FieldMap params: HashMap<String, Any?>): Observable<CreateChannelResponse>
    @GET("getnotifications")
    fun getNotificationList(@QueryMap params: HashMap<String, Any?>): Observable<NotificationListResponse>


}