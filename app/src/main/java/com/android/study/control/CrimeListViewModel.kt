package com.android.study.control

import androidx.lifecycle.ViewModel
import com.android.study.CrimeRepository
import com.android.study.model.Crime

class CrimeListViewModel: ViewModel() {
    private val crimeRepository = CrimeRepository.get()

    val crimesLiveData = crimeRepository.getCrimes()

    fun deleteCrime(crime: Crime) {
        crimeRepository.deleteCrime(crime)
    }
}