package com.example.trackerapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.trackerapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    val totalTimeRun = mainRepository.getTotalResult("duration")
    val totalDistance = mainRepository.getTotalResult("distance")
    val totalCaloriesBurned = mainRepository.getTotalResult("calories")
    val totalAvgSpeed = mainRepository.getTotalResult("speed")

    val runsSortedByDate = mainRepository.getSortedRuns("date")
}