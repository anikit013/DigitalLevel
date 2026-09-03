package com.example.digitallevel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.databinding.ItemMeasurementBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementAdapter(
    private val onItemClick: (MeasurementEntity) -> Unit
) : RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder>() {

    private var items: List<MeasurementEntity> = emptyList()

    fun submitList(newItems: List<MeasurementEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val binding = ItemMeasurementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MeasurementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MeasurementViewHolder(
        private val binding: ItemMeasurementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        fun bind(item: MeasurementEntity) {
            binding.tvItemStatus.text = item.status
            binding.tvItemTilt.text = String.format(Locale.getDefault(), "Overall Tilt: %.1f°", item.overallTilt)
            binding.tvItemLight.text = String.format(Locale.getDefault(), "Light: %.1f lux", item.lightLevel)
            binding.tvItemDateTime.text = dateFormat.format(Date(item.timestamp))

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
