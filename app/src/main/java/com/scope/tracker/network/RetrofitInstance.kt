package com.scope.tracker.network

import com.google.gson.GsonBuilder
import com.scope.tracker.util.Constants.Companion.BASE_URL
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class RetrofitInstance {

    companion object {

        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val dispatcher = Dispatcher(Executors.newFixedThreadPool(20))
            dispatcher.maxRequests=20
            dispatcher.maxRequestsPerHost=1

            var client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .dispatcher(dispatcher)
                .connectionPool( ConnectionPool(100, 30, TimeUnit.SECONDS))
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build()

        }

        val api by lazy {
            retrofit.create(VehicleAPI::class.java)
        }
    }

}