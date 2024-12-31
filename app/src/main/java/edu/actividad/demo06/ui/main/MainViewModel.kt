package edu.actividad.demo06.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.actividad.demo06.data.Repository
import kotlinx.coroutines.launch

/**
 * ViewModel for the MainActivity.
 *
 * @param repository The repository to get the cities.
 * @property currentCities The list of cities to display.
 * @property currentVisitedCities The list of visited cities.
 * @author Víctor Lamas
 */
class MainViewModel(private val repository: Repository) : ViewModel() {
    private var _currentCities = repository.fetchCities()
    val currentCities
        get() = _currentCities

    private var _currentVisitedCities = repository.fetchArrayAllCitiesDocs()
    val currentVisitedCities
        get() = _currentVisitedCities

    init {
        repository.createDocument()
    }

    /**
     * Adds a city to the visited cities.
     *
     * @param city The name of the city.
     * @param countryCode The country code of the city.
     */
    fun addCity(city: String, countryCode: String) {
        repository.addCity(city, countryCode)
    }

    /**
     * Updates the list of cities with the query.
     *
     * @param query The query to search for cities.
     */
    fun updateListCities(query: String) {
        viewModelScope.launch {
            _currentCities = if (query.isNotBlank())
                repository.fetchCitiesByName(query)
            else repository.fetchCities()
        }
    }
}

/**
 * Factory for the MainViewModel.
 *
 * @param repository The repository to get the cities.
 * @author Víctor Lamas
 */
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}