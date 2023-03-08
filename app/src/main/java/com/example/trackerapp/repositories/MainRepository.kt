package com.example.trackerapp.repositories

import com.example.trackerapp.db.Run
import com.example.trackerapp.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao: RunDAO
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getSortedRuns(param: String) = runDao.getSortedRunsBy(param)

    fun getTotalResult(param: String) = runDao.getTotalResults(param)
}