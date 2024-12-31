package edu.actividad.demo06.ui.maps

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import edu.actividad.demo06.R
import edu.actividad.demo06.databinding.ActivityDetailMapBinding
import edu.actividad.demo06.databinding.AnnotationLayoutBinding
import edu.actividad.demo06.model.City

/**
 * Displays the details of a city on a map.
 *
 * @property binding Reference to the binding of the activity to access the views.
 * @property TAG The tag for the log.
 * @author Víctor Lamas
 */
class DetailMapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMapBinding
    private val TAG = DetailMapActivity::class.java.simpleName

    /**
     * Displays city details if passed through an Intent.
     *
     * @param savedInstanceState The saved instance state.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailMapBinding.inflate(layoutInflater)
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

        val city =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_CITY, City::class.java)
            } else {
                intent.getParcelableExtra(EXTRA_CITY)
            }

        Log.d(TAG, "onCreate: $city")

        if (city != null) {
            showCity(city)
        } else {
            Toast.makeText(
                this@DetailMapActivity,
                "No city data",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val EXTRA_CITY = "city"
        fun navigate(activity: Activity, city: City) {
            val intent = Intent(activity, DetailMapActivity::class.java).apply {
                putExtra(EXTRA_CITY, city)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            activity.startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(activity)
                    .toBundle()
            )
        }
    }

    /**
     * Shows the city on the map.
     *
     * @param city The city to show.
     */
    private fun showCity(city: City) {
        val mapbox = binding.mapView.mapboxMap

        // Configure the map style
        mapbox.loadStyle(Style.STANDARD)

        // Configures the map camera
        mapbox.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(city.longitude, city.latitude))
                .zoom(8.0)
                .build()
        )

        // Instantiate the Annotation API and you get PointAnnotationManager
        val annotationApi = binding.mapView.annotations
        val pointAnnotationManager =
            annotationApi.createPointAnnotationManager()
        val point = Point.fromLngLat(city.longitude, city.latitude)

        // Se configuran las opciones de la anotación
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point) // Se indica el punto de la anotación
            .withIconImage( // Se especifica la imagen asociada a la anotación
                BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.red_marker
                )
            )
            .withIconAnchor(IconAnchor.BOTTOM)

        // Adjusts the size of the mark
        pointAnnotationOptions.iconSize = 0.5

        // Add the result to the map
        val pntAnnot = pointAnnotationManager.create(pointAnnotationOptions)

        // Prepares the title for annotation
        val viewAnnotationManager = binding.mapView.viewAnnotationManager
        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = R.layout.annotation_layout,
            options = viewAnnotationOptions {
                annotationAnchor {
                    geometry(point)
                    anchor(ViewAnnotationAnchor.BOTTOM)
                    offsetY(
                        ((pntAnnot.iconImageBitmap?.height!! * pntAnnot.iconSize!!) + 10)
                    )
                }
            }
        )

        // Inflates the annotation layout
        AnnotationLayoutBinding.bind(viewAnnotation).apply {
            tvCityName.text = city.name
            tvCityName.append(getString(R.string.txt_country, city.country))
            tvCityInfo.text =
                if (city.isCapital!!) {
                    "${getString(R.string.txt_is_capital)}\n"
                } else {
                    ""
                }
            tvCityInfo.append(
                getString(
                    R.string.txt_population,
                    city.population
                )
            )
            tvCityInfo.append("\n")
            tvCityInfo.append(
                getString(
                    R.string.txt_lonlat,
                    city.longitude.toString(),
                    city.latitude.toString()
                )
            )
        }
    }
}