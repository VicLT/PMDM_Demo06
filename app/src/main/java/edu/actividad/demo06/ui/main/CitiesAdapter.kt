package edu.actividad.demo06.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.actividad.demo06.R
import edu.actividad.demo06.databinding.CityItemBinding
import edu.actividad.demo06.model.City

class CitiesAdapter(
    private val onCityClick: (City) -> Unit
) : ListAdapter<City, CitiesAdapter.ViewHolder>(DiffCitiesCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            CityItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = CityItemBinding.bind(view)
        fun bind(city: City) {

            binding.tvName.text = city.name

            binding.tvCountry.text = itemView.context.getString(
                R.string.txt_country,
                city.country
            )

            binding.tvPopulation.text = itemView.context.getString(
                R.string.txt_population,
                city.population
            )

            binding.tvIsCapital.text =
                if (city.isCapital!!) {
                    itemView.context.getString(R.string.txt_is_capital)
                } else {
                    null
                }

            itemView.setOnClickListener {
                onCityClick(city)
            }
        }
    }
}

class DiffCitiesCallback : DiffUtil.ItemCallback<City>() {
    override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem.latitude == newItem.latitude
                && oldItem.longitude == newItem.longitude
    }

    override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem == newItem
    }
}