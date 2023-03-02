package com.example.trackerapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

//    @Query("""
//        SELECT * FROM running_table
//        ORDER BY
//        CASE WHEN :column = 'timestamp'  THEN timestamp END DESC,
//        CASE WHEN :column = 'timemili' THEN timeInMillis END DESC,
//        CASE WHEN :column = 'calories' THEN caloriesBurned END DESC,
//        CASE WHEN :column = 'speed'  THEN averageSpeedInKMH END DESC,
//        CASE WHEN :column = 'distance' THEN distanceInMeters END DESC,
//    """)
//    suspend fun filterBy(column : String) : LiveData<List<Run>>

    @Query("SELECT * FROM Run ORDER BY :runParams DESC")
    fun getAllRuns(runParams: String): LiveData<List<Run>>

    @Query("SELECT SUM(:runParams) FROM Run")
    fun <T> getTotalResults(runParams: String): LiveData<T>


}