package edu.actividad.demo06.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import edu.actividad.demo06.model.City

@Database(entities = [City::class], version =  1)
abstract class CitiesRoomDB : RoomDatabase() {
    abstract fun citiesDao(): CitiesDao
}

@Dao
interface CitiesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun  insertCity(cities: List<City>)

    @Query("SELECT * FROM city ORDER BY name ASC")
    suspend fun getCities(): List<City>

    @Query("SELECT * FROM city WHERE name LIKE :name ORDER BY name ASC")
    suspend fun getCitiesByName(name: String): List<City>
}