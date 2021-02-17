package com.swtecnn.sprinkler.api

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.swtecnn.sprinkler.api.model.CurrentWeatherForecast
import com.swtecnn.sprinkler.api.model.WeatherForecast
import io.reactivex.rxjava3.core.Single

private const val BASE_URL = "https://api.openweathermap.org"

object RetrofitClient {
    private val api: RetrofitService by lazy {
        getClient().create(RetrofitService::class.java)
    }

    private fun getClient(baseUrl: String = BASE_URL): Retrofit {
        val interceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getWeatherForecast(): Single<WeatherForecast> {
        return api.getWeatherForecast()
    }

    fun getCurrentWeather(): Single<CurrentWeatherForecast> {
        return api.getCurrentWeatherForecast()
    }

    fun getImage(imageCode: String): Single<ResponseBody> {
        return api.getWeatherImage(imageCode)
    }
}