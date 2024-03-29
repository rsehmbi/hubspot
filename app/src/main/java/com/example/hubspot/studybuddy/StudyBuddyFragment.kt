package com.example.hubspot.studybuddy

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.example.hubspot.R
import com.example.hubspot.services.LocationService
import com.example.hubspot.studybuddy.friendsMap.services.ShareLocationService
import com.example.hubspot.studybuddy.friendsMap.ui.FriendsMapActivity
import com.example.hubspot.studybuddy.pomodoro.PomodoroActivity

/**
 *  Main fragment for the study buddy feature
 */
class StudyBuddyFragment : Fragment() {
    companion object {
        const val LAST_KNOWN_LOCATION_KEY = "last_know_location_key"
    }
    private lateinit var studyLength: EditText
    private lateinit var breakLength: EditText
    private lateinit var shareLocationRadioGroup: RadioGroup
    private lateinit var startStudySessionButton: Button
    private lateinit var friendsMapButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_study_buddy, container, false)
        studyLength = view.findViewById(R.id.studyLength)
        breakLength = view.findViewById(R.id.breakLength)
        shareLocationRadioGroup = view.findViewById(R.id.share_location_radio_group)
        startStudySessionButton = view.findViewById(R.id.startStudySessionButton)
        friendsMapButton = view.findViewById(R.id.friendsMapButton)
        startStudySessionButton.setOnClickListener{startStudySession()}
        friendsMapButton.setOnClickListener {
            val intent = Intent(requireActivity(), FriendsMapActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    // Start the pomodoro timer and start the ShareLocationService if "Yes" radio button is checked
    private fun startStudySession(){
        val intent = Intent(requireActivity(), PomodoroActivity::class.java)
        if(studyLength.text.toString() != "" && breakLength.text.toString() != "" && shareLocationRadioGroup.checkedRadioButtonId != -1) {
            if(shareLocationRadioGroup.checkedRadioButtonId == R.id.share_location_yes) {
                shareLocation()
            }
            // take the study and break time inputs in minutes and convert to seconds
            intent.putExtra("studyLength", studyLength.text.toString().toLong() * 60)
            intent.putExtra("breakLength", breakLength.text.toString().toLong() * 60)
            intent.putExtra("sessionType", PomodoroActivity.STUDY_SESSION)
            startActivity(intent)
        } else {
            Toast.makeText(
                requireActivity(), "Missing required input",
                Toast.LENGTH_LONG).show()
        }
    }

    // Get the current location and start the ShareLocationService
    private fun shareLocation(){
        LocationService.getCurrentLocation(requireActivity(),
            object : LocationService.LocationCallback {
                override fun onCallback(result: Location?) {
                    val intentShareLocationService = Intent(activity!!.applicationContext, ShareLocationService::class.java)
                    intentShareLocationService.putExtra(LAST_KNOWN_LOCATION_KEY, result)
                    activity!!.applicationContext.startService(intentShareLocationService)
                }
            }
        )
    }
}