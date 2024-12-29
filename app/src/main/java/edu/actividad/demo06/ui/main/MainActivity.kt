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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.simpleName
    private var query: String? = null

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
            Log.e(TAG, "Ciudades actualizadas: ${visitedCities.size} - $visitedCities")

            binding.tvNoInfo.visibility = if (cities.isEmpty()) View.VISIBLE else View.GONE

            // A new list of cities with the visited cities updated
            val updatedCities = cities.map { city ->
                val cityFound = visitedCities.find {
                    it["name"] == city.name && it["countryCode"] == city.country
                }
                if (cityFound != null) {
                    // We use copy to create a new object with the visited field updated
                    city.copy(visited = city.visited + 1)
                } else {
                    city
                }
            }

            Log.i(TAG, "populateCities: $updatedCities")
            adapter.submitList(updatedCities)
        }.catch {
            Toast.makeText(
                this@MainActivity,
                it.message,
                Toast.LENGTH_SHORT
            ).show()
        }.collect()
    }
}