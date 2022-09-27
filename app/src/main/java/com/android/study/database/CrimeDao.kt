package com.android.study.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.study.model.Crime
import java.util.*

@Dao
interface CrimeDao {

    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>?>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrimeItem(id: UUID): Crime?

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)

    @Delete
    fun deleteCrime(crime: Crime)

    fun insertOrUpdate(crime: Crime) {
        val item = getCrimeItem(crime.id)
        if (item == null) {
            addCrime(crime)
        } else {
            updateCrime(crime)
        }
    }
}