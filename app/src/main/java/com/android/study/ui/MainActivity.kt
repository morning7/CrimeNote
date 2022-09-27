package com.android.study.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.study.R
import com.android.study.database.migration_1_2
import com.android.study.databinding.MainActivityBinding
import java.util.*

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
    private lateinit var bind: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = MainActivityBinding.inflate(layoutInflater)
        setContentView(bind.root)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, CrimeListFragment.newInstance())
            .commit()
        }
    }

    override fun onCrimeSelected(crimeId: UUID) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, 0)
            .replace(R.id.fragment_container, CrimeDetailsFragment.newInstance(crimeId))
            .addToBackStack(null)
            .commit()
    }
}