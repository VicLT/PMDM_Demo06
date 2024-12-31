package edu.actividad.demo06.data

import edu.actividad.demo06.model.City

/**
 * Responsible for making the request to the cities API.
 *
 * @property api The API to get the cities.
 * @author Víctor Lamas
 */
class RemoteDataSource {
    private val api = CitiesAPI.getRetrofit2Api()

    /**
     * Gets a list of cities from the API.
     *
     * @return The list of cities.
     */
    suspend fun getCities(): List<City> {
        return api.getCities()
    }

    /**
     * Gets a list of cities from the API that match the name.
     *
     * @param name The name of the cities to get.
     * @return The list of cities.
     */
    suspend fun getCitiesByName(name: String): List<City> {
        return api.getCitiesByName(name)
    }
}