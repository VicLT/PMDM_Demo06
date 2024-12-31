package edu.actividad.demo06.ui.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import edu.actividad.demo06.CityApplication
import edu.actividad.demo06.data.RemoteDataSource
import edu.actividad.demo06.data.Repository
import edu.actividad.demo06.databinding.ActivityMainBinding
import edu.actividad.demo06.ui.maps.DetailMapActivity
import edu.actividad.demo06.utils.createNotificationChannel
import edu.actividad.demo06.utils.sendNotification
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.simpleName
    private var query: String? = null
    private var lastTotalVisits: Int? = null

    private val vm: MainViewModel by viewModels {
        val db = (application as CityApplication).cityDB
        val ds = RemoteDataSource()
        MainViewModelFactory(Repository(db, ds))
    }

    private val adapter by lazy {
        CitiesAdapter(
            onCityClick =  { city ->
                vm.addCity(city.name!!, city.country!!)
                DetailMapActivity.navigate(this@MainActivity, city)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // Crear el canal de notificaciones
        createNotificationChannel(this)

        // Bloqueo de la rotación
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        binding.mRecycler.setHasFixedSize(true)
        binding.mRecycler.adapter = adapter

        // Disable refresh items animation on RecyclerView
        binding.mRecycler.itemAnimator!!.apply {
            changeDuration = 0
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                populateCities()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Gestión de la búsqueda
        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "onQueryTextChange: $newText")
                    query = newText
                    vm.updateListCities(query!!)

                    lifecycleScope.launch {
                        populateCities()
                    }
                    return true
                }
            }
        )
    }

    // Method responsible for obtaining the initial data and search
    private suspend fun populateCities() {
        combine(vm.currentCities, vm.currentVisitedCities) { cities, visitedCities ->
            Log.i(TAG, "Ciudades actualizadas: ${visitedCities.size} - $visitedCities")

            binding.tvNoInfo.visibility = if (cities.isEmpty()) View.VISIBLE else View.GONE

            // Nueva lista de ciudades con las visitas actualizadas
            val updatedCities = cities.flatMap { city ->
                // Buscar la ciudad en visitedCities por "name" y "countryCode"
                val totalVisits = visitedCities
                    .filter { visitedCity ->
                        val internalMap = visitedCity.keys.firstOrNull()
                        val name = internalMap?.get("name")
                        val countryCode = internalMap?.get("countryCode")
                        name == city.name && countryCode == city.country
                    }
                    .sumOf { visitedCity ->
                        visitedCity.values.firstOrNull() ?: 0 // Sumar las visitas
                    }

                if (totalVisits > 0) {
                    listOf(city.copy(visited = totalVisits))
                } else {
                    listOf(city.copy(visited = 0))
                }
            }

            Log.i(TAG, "populateCities: $updatedCities")
            adapter.submitList(updatedCities)

            // Calcular el total de visitas acumuladas
            val currentTotalVisits = visitedCities.sumOf { it.values.firstOrNull() ?: 0 }

            // Evitar notificación en la primera carga
            if (lastTotalVisits != null && currentTotalVisits > lastTotalVisits!!) {
                sendNotification(this@MainActivity)
            }

            // Actualizar el último total de visitas conocido
            lastTotalVisits = currentTotalVisits
        }.catch {
            Toast.makeText(
                this@MainActivity,
                it.message,
                Toast.LENGTH_SHORT
            ).show()
        }.collect()
    }
}