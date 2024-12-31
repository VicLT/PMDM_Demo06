package edu.actividad.demo06

import android.app.Application
import androidx.room.Room
import edu.actividad.demo06.data.CitiesRoomDB

/**
 * Initialize the database.
 *
 * @property cityDB The database of the cities.
 * @author Víctor Lamas
 */
class CityApplication : Application() {
    lateinit var cityDB: CitiesRoomDB
        private set

    override fun onCreate() {
        super.onCreate()

        cityDB = Room.databaseBuilder(
            this,
            CitiesRoomDB::class.java, "cities.db"
        ).build()
    }
}