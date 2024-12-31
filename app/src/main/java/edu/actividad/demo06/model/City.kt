package edu.actividad.demo06.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Represents a city.
 *
 * @param country The country of the city.
 * @param isCapital If the city is a capital.
 * @param latitude The latitude of the city.
 * @param longitude The longitude of the city.
 * @param name The name of the city.
 * @param population The population of the city.
 * @param visited The number of times the city has been visited.
 * @constructor Creates a City.
 * @author Víctor Lamas
 */
@Parcelize
@Entity(primaryKeys = ["latitude", "longitude"])
data class City(
    @SerializedName("country")
    val country: String?,
    @SerializedName("is_capital")
    val isCapital: Boolean?,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("name")
    val name: String?,
    @SerializedName("population")
    val population: Int?,

    @Ignore
    var visited: Int = 0
) : Parcelable {
    constructor(
        country: String?,
        isCapital: Boolean?,
        latitude: Double,
        longitude: Double,
        name: String?,
        population: Int?
    ) : this(country, isCapital, latitude, longitude, name, population, 0)
}