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

/**
 * Displays stored cities and allows searching all of them.
 *
 * @property binding Reference to the binding of the activity to access the views.
 * @property TAG The tag for the log.
 * @property query The query to search for cities.
 * @property lastTotalVisits The last total number of visits.
 * @property vm Instance of the ViewModel to handle the logic of the cities.
 * @property adapter Adapter for displaying cities in the RecyclerView.
 * @author Víctor Lamas
 */
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

    /**
     * Initializes the activity.
     *
     * @param savedInstanceState The saved instance state.
     */
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

        // Create the notification channel.
        createNotificationChannel(this)

        // Rotation lock.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        binding.mRecycler.setHasFixedSize(true)
        binding.mRecycler.adapter = adapter

        // Disable refresh items animation on RecyclerView.
        binding.mRecycler.itemAnimator!!.apply {
            changeDuration = 0
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                populateCities()
            }
        }
    }

    /**
     * Configures the search handling in the SearchView to update the list of
     * cities according to the text entered.
     */
    override fun onStart() {
        super.onStart()

        // Search management.
        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                /**
                 * Called when the user submits the query.
                 *
                 * @param query The query text that is to be submitted
                 * @return true if the query has been handled by the listener,
                 * false to let the SearchView perform the default action.
                 */
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                /**
                 * Called when the query text is changed by the user.
                 *
                 * @param newText the new content of the query text field.
                 * @return false if the SearchView should perform the default
                 * action of showing any suggestions if available. True if the
                 * action was handled by the listener.
                 */
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

    /**
     * Updates cities with visits and displays a notification if number changes.
     *
     * @return The list of cities.
     * @throws Exception If an error occurs.
     */
    private suspend fun populateCities() {
        combine(vm.currentCities, vm.currentVisitedCities) { cities, visitedCities ->
            Log.i(TAG, "Ciudades actualizadas: ${visitedCities.size} - $visitedCities")

            binding.tvNoInfo.visibility = if (cities.isEmpty()) View.VISIBLE else View.GONE

            // Mapping cities with the total number of visitors.
            val updatedCities = cities.flatMap { city ->
                val totalVisits = visitedCities
                    .filter { visitedCity ->
                        // Filter visited cities by name and country code.
                        val internalMap = visitedCity.keys.firstOrNull()
                        val name = internalMap?.get("name")
                        val countryCode = internalMap?.get("countryCode")
                        name == city.name && countryCode == city.country
                    }
                    .sumOf { visitedCity ->
                        // Sum of visits from overlapping cities.
                        visitedCity.values.firstOrNull() ?: 0
                    }

                // Create a city with updated visits.
                if (totalVisits > 0) {
                    listOf(city.copy(visited = totalVisits))
                } else {
                    listOf(city.copy(visited = 0))
                }
            }

            Log.i(TAG, "populateCities: $updatedCities")
            adapter.submitList(updatedCities)

            // Calculates the total number of current visits.
            val currentTotalVisits = visitedCities.sumOf { it.values.firstOrNull() ?: 0 }

            // Sends a notification if visits have increased.
            if (lastTotalVisits != null && currentTotalVisits > lastTotalVisits!!) {
                sendNotification(this@MainActivity)
            }

            // Updates total visits for future comparisons
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