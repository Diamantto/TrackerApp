package com.example.trackerapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.trackerapp.other.TypeResults

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query(
        "SELECT * FROM running_table ORDER BY " +
                "CASE :column " +
                "WHEN 'date' THEN timestamp " +
                "WHEN 'duration' THEN timeInMillis " +
                "WHEN 'calories' THEN caloriesBurned " +
                "WHEN 'speed' THEN avgSpeedInKMH " +
                "WHEN 'distance' THEN distanceInMeters " +
                "END DESC"
    )
    fun getSortedRunsBy(column: String): LiveData<List<Run>>


    @Query(
        "SELECT SUM(CASE WHEN :runParams = 'timemili' THEN timeInMillis ELSE 0 END) as totalTimeInMillis, " +
                "SUM(CASE WHEN :runParams = 'calories' THEN caloriesBurned ELSE 0 END) as totalCaloriesBurned, " +
                "SUM(CASE WHEN :runParams = 'distance' THEN distanceInMeters ELSE 0 END) as totalDistance, " +
                "AVG(CASE WHEN :runParams = 'speed' THEN avgSpeedInKMH ELSE 0 END) as totalAvgSpeed " +
                "FROM running_table"
    )
    fun getTotalResults(runParams: String): LiveData<TypeResults>


}