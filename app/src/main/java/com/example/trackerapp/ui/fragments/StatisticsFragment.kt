package com.example.trackerapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.trackerapp.R
import com.example.trackerapp.databinding.FragmentStatisticsBinding
import com.example.trackerapp.other.CustomMarkerView
import com.example.trackerapp.other.TrackingUtility
import com.example.trackerapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var binding: FragmentStatisticsBinding

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStatisticsBinding.bind(view)
        subscribeToObservers()
        setupBarChart()

    }

    private fun setupBarChart() {
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }


    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner) {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it.totalTimeInMillis)
                binding.tvTotalTime.text = totalTimeRun
            }
        }
        viewModel.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                val km = it.totalDistance / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                binding.tvTotalDistance.text = totalDistanceString
            }
        }
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeed = round(it.totalAvgSpeed * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km"
                binding.tvAverageSpeed.text = avgSpeedString
            }
        }
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                val totalCalories = "${it.totalCaloriesBurned}"
                binding.tvTotalCalories.text = totalCalories
            }
        }
        viewModel.runsSortedByDate.observe(viewLifecycleOwner) {
            it?.let {
                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                val bardataSet = BarDataSet(allAvgSpeeds, "Avg Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                binding.barChart.data = BarData(bardataSet)
                binding.barChart.marker = CustomMarkerView(it, requireContext(), R.layout.marker_view)
                binding.barChart.invalidate()
            }
        }
    }

}