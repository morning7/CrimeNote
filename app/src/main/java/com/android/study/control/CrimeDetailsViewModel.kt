package com.android.study.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.android.study.CrimeRepository
import com.android.study.model.Crime
import java.io.File
import java.util.*

class CrimeDetailsViewModel: ViewModel() {

    private val repository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    var crimeLiveData: LiveData<Crime?> = Transformations.switchMap(crimeIdLiveData) { id ->
        repository.getCrime(id)
    }

    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun updateCrime(crime: Crime) {
        repository.updateCrime(crime)
    }

    fun addCrime(crime: Crime) {
        repository.addCrime(crime)
    }

    fun insertOrUpdate(crime: Crime) {
        repository.insertOrUpdate(crime)
    }

    fun getPhotoFile(crime: Crime): File {
        return repository.getPhotoFile(crime)
    }
}