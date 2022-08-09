package com.example.hubspot.security.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * The Speech Activity displays a user's latest speech as well as the time it was recorded.
 */
class SpeechActivity : AppCompatActivity() {
    private var speech = ""
    private var time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech)

        setSpeechAndTimeVariablesFromExtras()
        initializeTextViews()
        initializeDoneButton()
    }

    private fun initializeDoneButton() {
        val doneButton = findViewById<Button>(R.id.speech_done_button)
        doneButton.setOnClickListener {
            finish()
        }
    }

    private fun initializeTextViews() {
        // Set date and time
        val timeTextView = findViewById<TextView>(R.id.time_text_view)
        // Format millis to date and time
        val formatString = "dd/MM/yyyy hh:mm:ss"
        val formatter = SimpleDateFormat(formatString)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val formattedDateAndTime = formatter.format(calendar.time)
        // Set formatted date and time to time text view
        timeTextView.text = "Date and Time Recorded: $formattedDateAndTime"

        // Set speech
        val speechTextView = findViewById<TextView>(R.id.speech_text_view)
        speechTextView.text = "Speech Text: $speech"
    }

    private fun setSpeechAndTimeVariablesFromExtras() {
        speech = intent.getStringExtra("speech")!!
        time = intent.getLongExtra("time", 0L)
    }
}