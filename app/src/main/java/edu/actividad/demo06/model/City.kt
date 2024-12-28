package edu.actividad.demo06.model

import android.os.Parcelable
import androidx.room.Entity
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
    @SerializedName("region")
    val region: String?
) : Parcelable