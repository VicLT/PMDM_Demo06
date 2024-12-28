package edu.actividad.demo06.data

import edu.actividad.demo06.model.City

class LocalDataSource(private val db: CitiesDao) {
    suspend fun insertCities(cities: List<City>) = db.insertCity(cities)

    suspend fun getCities(): List<City> {
        return db.getCities()
    }

    suspend fun getCitiesByName(name: String): List<City> {
        return db.getCitiesByName(name)
    }
}