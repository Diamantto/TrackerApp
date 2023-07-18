package com.example.trackerapp.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackerapp.db.Run
import com.example.trackerapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    val runs = MediatorLiveData<List<Run>>()

    init {
        getSortedRuns("date")
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

    fun getSortedRuns(param: String) {
        runs.addSource(mainRepository.getSortedRuns(param)) { result ->
            result?.let { runs.value = it }
        }
    }

    fun deleteRun(run: Run) = viewModelScope.launch {
        mainRepository.deleteRun(run)
    }
}