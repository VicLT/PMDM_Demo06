package edu.actividad.demo06.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import edu.actividad.demo06.model.City

/**
 * Responsible for managing the local database of cities.
 * It uses Room to manage the database.
 *
 * @author Víctor Lamas
 */
@Database(entities = [City::class], version = 1)
abstract class CitiesRoomDB : RoomDatabase() {
    abstract fun citiesDao(): CitiesDao
}

@Dao
interface CitiesDao {
    /**
     * Inserts a list of cities into the database.
     * In case of conflict, it replaces the city.
     *
     * @param cities The list of cities to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(cities: List<City>)

    /**
     * Gets all the cities from the database sorted by name.
     *
     * @return The list of cities.
     */
    @Query("SELECT * FROM city ORDER BY name ASC")
    suspend fun getCities(): List<City>

    /**
     * Gets the cities that match the name from the database sorted by name.
     *
     * @param name The name of the city to search.
     * @return The list of cities that match the name.
     */
    @Query("SELECT * FROM city WHERE name LIKE :name ORDER BY name ASC")
    suspend fun getCitiesByName(name: String): List<City>
}