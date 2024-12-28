package edu.actividad.demo06.data

import edu.actividad.demo06.BuildConfig
import edu.actividad.demo06.model.City
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

class CitiesAPI {
    companion object {
        private const val BASE_URL = "https://api.api-ninjas.com/"

        fun getRetrofit2Api(): CitiesAPIInterface {
            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(CitiesAPIInterface::class.java)
        }
    }
}

interface CitiesAPIInterface {
    @Headers("X-Api-Key: ${BuildConfig.API_KEY}")
    @GET("v1/city")
    suspend fun getCities(
        @Query("min_population") minPopulation: Int = 1,
        @Query("limit") limiter: Int = 30
    ): List<City>

    @Headers("X-Api-Key: ${BuildConfig.API_KEY}")
    @GET("v1/city")
    suspend fun getCitiesByName(
        @Query("name") name: String,
        @Query("limit") limit: Int = 30
    ): List<City>
}