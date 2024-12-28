package edu.actividad.demo06.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.actividad.demo06.data.Repository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {
    private var _currentCities = repository.fetchCities()
    val currentCities
        get() = _currentCities

    fun updateListCities(query: String) {
        viewModelScope.launch {
            _currentCities = if (query.isNotBlank())
                repository.fetchCitiesByName(query)
            else repository.fetchCities()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}