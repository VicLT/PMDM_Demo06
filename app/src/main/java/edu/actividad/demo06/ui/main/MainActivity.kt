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
import androidx.lifecycle.lifecycleScope
import edu.actividad.demo06.CityApplication
import edu.actividad.demo06.data.RemoteDataSource
import edu.actividad.demo06.data.Repository
import edu.actividad.demo06.databinding.ActivityMainBinding
import edu.actividad.demo06.ui.maps.DetailMapActivity
import kotlinx.coroutines.flow.catch
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

        populateCities()
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

                    populateCities()
                    return true
                }
            }
        )
    }

    // Obtiene los datos iniciales y realiza la búsqueda
    private fun populateCities() {
        lifecycleScope.launch {
            vm.currentCities.catch {
                Toast.makeText(
                    this@MainActivity,
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }.collect { cities ->
                Log.d(TAG, "onCreate: $cities")

                binding.tvNoInfo.visibility =
                    if (cities.isEmpty()) View.VISIBLE else View.GONE

                adapter.submitList(cities)
            }
        }
    }
}