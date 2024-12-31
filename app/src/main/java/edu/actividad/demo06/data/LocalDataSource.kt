package edu.actividad.demo06.data

import edu.actividad.demo06.model.City

/**
 * Responsible for managing the local database of cities.
 *
 * @param db The database of cities.
 * @constructor Creates a LocalDataSource.
 * @author Víctor Lamas
 */
class LocalDataSource(private val db: CitiesDao) {
    /**
     * Inserts a list of cities into the database.
     *
     * @param cities The list of cities to insert.
     */
    suspend fun insertCities(cities: List<City>) = db.insertCity(cities)

    /**
     * Gets all the cities from the database.
     *
     * @return The list of cities.
     */
    suspend fun getCities(): List<City> {
        return db.getCities()
    }

    /**
     * Gets the cities that match the name from the database.
     *
     * @param name The name of the city to search.
     * @return The list of cities that match the name.
     */
    suspend fun getCitiesByName(name: String): List<City> {
        return db.getCitiesByName(name)
    }
}