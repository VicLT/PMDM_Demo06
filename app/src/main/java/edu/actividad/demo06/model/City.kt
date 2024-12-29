package edu.actividad.demo06.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

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