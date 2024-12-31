package edu.actividad.demo06.data

import android.util.Log
import edu.actividad.demo06.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/**
 * Responsible for managing the data of the application.
 *
 * @param db The database of cities.
 * @param ds The data source of cities.
 * @property TAG The tag for the log.
 * @property localDataSource The local data source of cities.
 * @property fbRepository The repository of Firebase.
 * @author Víctor Lamas
 */
class Repository(db: CitiesRoomDB, private val ds: RemoteDataSource) {
    private val TAG = Repository::class.java.simpleName
    private val localDataSource = LocalDataSource(db.citiesDao())
    private val fbRepository = RepositoryFirebase()

    /**
     * Fetches all the cities from Firebase.
     *
     * @return The list of all together visited cities and their visualizations.
     */
    fun fetchArrayAllCitiesDocs(): Flow<List<Map<Map<String, String>, Int>>> = runBlocking {
        fbRepository.fetchArrayAllCitiesDocs()
    }

    /**
     * Creates a document in Firebase.
     */
    fun createDocument() {
        fbRepository.createDocument()
    }

    /**
     * Add a city to Firebase.
     *
     * @param city The name of the city.
     * @param countryCode The country code of the city.
     */
    fun addCity(city: String, countryCode: String) {
        fbRepository.addCity(city, countryCode)
    }

    /**
     * Retrieves cities from the DB or API, updating the DB if necessary.
     *
     * @return The list of cities.
     */
    fun fetchCities(): Flow<List<City>> {
        return flow {
            var resultDB = emptyList<City>()
            try {
                resultDB = localDataSource.getCities()
                val resultAPI = ds.getCities()

                if (resultDB.containsAll(resultAPI)) {
                    emit(resultDB)
                } else {
                    localDataSource.insertCities(resultAPI)
                }

                resultDB = localDataSource.getCities()
            } catch (e: Exception) {
                Log.e(TAG, "fetchCities: ${e.message}")
            } finally {
                emit(resultDB)
            }
        }
    }

    /**
     * Retrieves cities by name from the DB or API, updating the DB if necessary.
     *
     * @param name The name of the city to search.
     * @return The list of cities that match the name.
     */
    fun fetchCitiesByName(name: String): Flow<List<City>> {
        return flow {
            var resultDB = emptyList<City>()
            try {
                resultDB = localDataSource.getCitiesByName("%$name%")
                val resultAPI = ds.getCitiesByName(name)

                if (resultDB.containsAll(resultAPI)) {
                    emit(resultDB)
                } else {
                    localDataSource.insertCities(resultAPI)
                }

                resultDB = localDataSource.getCitiesByName("%$name%")
            } catch (e: Exception) {
                Log.e(TAG, "fetchCitiesByName: ${e.message}")
            } finally {
                emit(resultDB)
            }
        }
    }
}