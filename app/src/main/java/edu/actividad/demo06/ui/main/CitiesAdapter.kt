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

/**
 * Adapter for the cities list.
 *
 * @param onCityClick The action to perform when a city is clicked.
 * @author Víctor Lamas
 */
class CitiesAdapter(
    private val onCityClick: (City) -> Unit
) : ListAdapter<City, CitiesAdapter.ViewHolder>(DiffCitiesCallback()) {

    /**
     * Creates a view holder.
     *
     * @param parent The parent view group.
     * @param viewType The view type.
     * @return The view holder.
     */
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

    /**
     * Binds the view holder with the city.
     *
     * @param holder The view holder.
     * @param position The position of the city.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Represents a view holder.
     *
     * @param view The view.
     */
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

            if (city.visited > 0) {
                binding.tvVisited.text = String.format(city.visited.toString())
                binding.tvVisited.visibility = View.VISIBLE
                binding.ivVisited.visibility = View.VISIBLE
            } else {
                binding.tvVisited.visibility = View.GONE
                binding.ivVisited.visibility = View.GONE
            }
        }
    }
}

/**
 * Represents a diff callback for the cities.
 */
class DiffCitiesCallback : DiffUtil.ItemCallback<City>() {
    /**
     * Checks if the items are the same.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if the items are the same, false otherwise.
     */
    override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem.latitude == newItem.latitude
                && oldItem.longitude == newItem.longitude
    }

    /**
     * Checks if the contents are the same.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if the contents are the same, false otherwise.
     */
    override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem == newItem
    }
}