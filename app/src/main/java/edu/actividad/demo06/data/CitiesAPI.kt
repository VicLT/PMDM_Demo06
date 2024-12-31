package edu.actividad.demo06.data

import edu.actividad.demo06.BuildConfig
import edu.actividad.demo06.data.CitiesAPI.Companion.BASE_URL
import edu.actividad.demo06.model.City
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Responsible for making the request to the cities API.
 * It uses Retrofit2 to make the request.
 *
 * @property BASE_URL The base URL of the API.
 * @author Víctor Lamas
 */
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

/**
 * Defines the methods to make the request to the cities API.
 *
 * @author Víctor Lamas
 */
interface CitiesAPIInterface {
    /**
     * Gets a list of cities from the API.
     *
     * @param minPopulation The minimum population of the cities to get.
     * @param limiter The maximum number of cities to get.
     * @return The list of cities.
     */
    @Headers("X-Api-Key: ${BuildConfig.API_KEY}")
    @GET("v1/city")
    suspend fun getCities(
        @Query("min_population") minPopulation: Int = 1,
        @Query("limit") limiter: Int = 30
    ): List<City>

    /**
     * Gets a list of cities from the API that match the name.
     *
     * @param name The name of the cities to get.
     * @param limit The maximum number of cities to get.
     * @return The list of cities.
     */
    @Headers("X-Api-Key: ${BuildConfig.API_KEY}")
    @GET("v1/city")
    suspend fun getCitiesByName(
        @Query("name") name: String,
        @Query("limit") limit: Int = 30
    ): List<City>
}