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

        private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        fun bind(item: MeasurementEntity) {
            val formattedStatus = when (item.status.uppercase(Locale.getDefault())) {
                "LEVEL", "✓ LEVEL" -> "✓ LEVEL"
                "SLIGHTLY TILTED" -> "SLIGHTLY TILTED"
                "TILTED" -> "TILTED"
                else -> item.status.uppercase(Locale.getDefault())
            }

            binding.tvItemStatus.text = formattedStatus
            binding.tvItemTilt.text = String.format(Locale.getDefault(), "%.2f°", item.overallTilt)
            binding.tvItemXY.text = String.format(Locale.getDefault(), "X: %.2f°   Y: %.2f°", item.angleX, item.angleY)
            binding.tvItemMode.text = item.mode.uppercase(Locale.getDefault())
            binding.tvItemLight.text = String.format(Locale.getDefault(), "%.0f lux", item.lightLevel)
            binding.tvItemDateTime.text = dateFormat.format(Date(item.timestamp))

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
