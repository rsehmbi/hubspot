package com.example.hubspot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.hubspot.ratings.RatingsFragment
import com.example.hubspot.schedule.ScheduleFragment
import com.example.hubspot.security.SecurityFragment
import com.example.hubspot.studybuddy.StudyBuddyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is currently logged in, go to login screen if not
        val auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser == null || !currentUser.isEmailVerified) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

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