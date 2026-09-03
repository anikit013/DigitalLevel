package com.example.digitallevel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.digitallevel.databinding.ActivityHistoryBinding
import com.example.digitallevel.ui.HistoryViewModel
import com.example.digitallevel.ui.ViewModelFactory
import com.example.digitallevel.util.Constants
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: MeasurementAdapter

    private val viewModel: HistoryViewModel by viewModels {
        val app = application as DigitalLevelApplication
        ViewModelFactory(app.repository)
    }

    private val detailsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Measurement deleted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MeasurementAdapter { measurement ->
            val intent = Intent(this, MeasurementDetailsActivity::class.java).apply {
                putExtra(Constants.EXTRA_MEASUREMENT_ID, measurement.id)
            }
            detailsLauncher.launch(intent)
        }

        binding.rvMeasurements.layoutManager = LinearLayoutManager(this)
        binding.rvMeasurements.adapter = adapter

        lifecycleScope.launch {
            viewModel.measurements.collect { measurements ->
                if (measurements.isEmpty()) {
                    binding.rvMeasurements.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvMeasurements.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    adapter.submitList(measurements)
                }
            }
        }

        binding.btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all measurements?")
                .setPositiveButton("Clear") { _, _ ->
                    viewModel.deleteAllMeasurements()
                    Toast.makeText(this@HistoryActivity, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
