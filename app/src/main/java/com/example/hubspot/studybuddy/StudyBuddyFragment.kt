package com.example.hubspot.studybuddy

import android.content.Intent
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
import com.example.hubspot.studybuddy.friendsMap.ui.FriendsMapActivity
import com.example.hubspot.studybuddy.pomodoro.PomodoroActivity

class StudyBuddyFragment : Fragment() {
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

    private fun startStudySession(){
        val intent = Intent(requireActivity(), PomodoroActivity::class.java)
        if(studyLength.text.toString() != "" && breakLength.text.toString() != "") {
            if(shareLocationRadioGroup.checkedRadioButtonId == R.id.share_location_yes) {
                shareLocation()
            }
            // for testing purposes, the input is in seconds
            // TODO: multiply both length by 60 so that the input is in minutes
            intent.putExtra("studyLength", studyLength.text.toString().toLong())
            intent.putExtra("breakLength", breakLength.text.toString().toLong())
            intent.putExtra("sessionType", PomodoroActivity.STUDY_SESSION)
            startActivity(intent)
        } else {
            Toast.makeText(
                requireActivity(), "Missing required input",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun shareLocation(){
        //TODO: implement sharing location with friends
        println("debug: share location")
    }
}