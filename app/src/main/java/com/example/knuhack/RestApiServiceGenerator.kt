package com.example.knuhack

import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


object RestApiServiceGenerator {
    private val logger = Logger.getLogger(RestApiServiceGenerator.javaClass.name)

    private val LOCAL_URL = "http://10.0.2.2:8080/"
    private val EC2_URL = "http://52.79.203.88:8080/"

    fun <S> createService(serviceClass: Class<S>): S {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        val myCookieJar = JavaNetCookieJar(cookieManager)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .cookieJar(myCookieJar)
            .retryOnConnectionFailure(true)
            .build()

        val gson = GsonBuilder().setLenient().create()

        val builder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(EC2_URL)
            .client(okHttpClient)
        val retrofit = builder.build()
        return retrofit.create(serviceClass)
    }

}