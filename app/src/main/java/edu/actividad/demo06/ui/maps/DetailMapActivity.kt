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

class DetailMapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMapBinding
    private val TAG = DetailMapActivity::class.java.simpleName

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

    private fun showCity(city: City) {
        // Se obtiene el mapa de la vista
        val mapbox = binding.mapView.mapboxMap

        // Se configura el estilo del mapa
        mapbox.loadStyle(Style.STANDARD)

        // Se configura la cámara del mapa
        mapbox.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(city.longitude, city.latitude))
                .zoom(8.0)
                .build()
        )

        // Se instancia la API Annotation y se obtiene PointAnnotationManager
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

        // Se ajusta el tamaño de la marca
        pointAnnotationOptions.iconSize = 0.5

        // Se añade el resultado al mapa
        val pntAnnot = pointAnnotationManager.create(pointAnnotationOptions)

        // Se prepara el título para la anotación
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

        // Se infla el layout de la anotación
        AnnotationLayoutBinding.bind(viewAnnotation).apply {
            tvCityName.text = city.name
            tvCityName.append(getString(R.string.txt_country, city.country))
            tvCityInfo.text =
                if (city.isCapital == true) {
                    getString(R.string.txt_is_capital)
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