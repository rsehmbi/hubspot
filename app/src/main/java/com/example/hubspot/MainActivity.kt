package com.example.hubspot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.hubspot.ratings.RatingsFragment
import com.example.hubspot.schedule.ScheduleFragment
import com.example.hubspot.security.ui.SecurityFragment
import com.example.hubspot.studybuddy.StudyBuddyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ratingsFragment = RatingsFragment()
        val scheduleFragment = ScheduleFragment()
        val securityFragment = SecurityFragment()
        val studybuddyFragment = StudyBuddyFragment()

        setCurrentFragment(scheduleFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.schedule_id->setCurrentFragment(scheduleFragment)
                R.id.ratings_id->setCurrentFragment(ratingsFragment)
                R.id.security_id->setCurrentFragment(securityFragment)
                R.id.studybuddy_id->setCurrentFragment(studybuddyFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }
}