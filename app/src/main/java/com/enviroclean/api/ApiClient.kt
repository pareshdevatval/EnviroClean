package com.enviroclean.api

import android.content.Context
import android.util.Base64
import android.util.Log
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.BuildConfig
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

object ApiClient {
    lateinit var prefs: Prefs
    fun getApiClient(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(httpClient)
            .build()
    }

    fun getHttpClient(context: Context): OkHttpClient {
        prefs = Prefs.getInstance(context)!!
        val TIME = 90

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient().newBuilder()
            .connectTimeout(TIME.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIME.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIME.toLong(), TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
           //     val basic = "Basic " + Base64.encodeToString(CREDENTIALS.toByteArray(), Base64.NO_WRAP)
                val requestBuilder = original.newBuilder().header("Authorization", "")
                requestBuilder.header("Accept", "application/json")
                requestBuilder.method(original.method, original.body)
                if(Prefs.getInstance(context)!!.isLoggedIn){
                    requestBuilder.header("Authorization", "Bearer "+ prefs.accessToken)
                }
                requestBuilder.header("Accept-Language", "en")
                requestBuilder.header("appaccesstoken", BuildConfig.appaccesstoken)
                val request = requestBuilder.build()
                val response = chain.proceed(request)
                if (response.isSuccessful) {
                    val data = response.body!!.string()
                    Log.e("RESPONSE = ", data)
                    return@Interceptor response.newBuilder()
                        .body(data.toResponseBody(response.body!!.contentType())).build()
                } else if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    AppUtils.logoutUser(context)
                }
                return@Interceptor response
            })
            .build()
    }

    fun getApiService(context: Context): ApiService = getApiClient(
        getHttpClient(context)
    ).create(ApiService::class.java)

}