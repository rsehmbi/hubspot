package com.example.hubspot.studybuddy.pomodoro

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.R
import com.example.hubspot.studybuddy.friendsMap.services.ShareLocationService
import com.example.hubspot.utils.Dialog
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.*

class PomodoroActivity: AppCompatActivity()  {
    companion object{
        const val STUDY_SESSION = "study"
        const val BREAK_SESSION = "break"
        const val TAG:String = "tag"
    }
    private lateinit var sessionTypeTextView: TextView
    private lateinit var textView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var pauseButton: Button
    private lateinit var timer: CountDownTimer
    private lateinit var progressBar: MaterialProgressBar
    private var initStudyLengthInSeconds = 0L
    private var initBreakLengthInSeconds = 0L
    private var timeLeftInMillis = 0L
    private var endTime = 0L
    private lateinit var sessionType: String
    private var timerIsRunning = false
    private var timerIsPaused = false
    private val dialog = Dialog()
    private val bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)
        sessionTypeTextView = this.findViewById(R.id.sessionTypeTextView)
        textView = this.findViewById(R.id.timerTextView)
        startButton = this.findViewById(R.id.timerStartButton)
        pauseButton = this.findViewById(R.id.timerPauseButton)
        stopButton = this.findViewById(R.id.timerStopButton)
        progressBar = this.findViewById(R.id.materialProgressBar)
        initStudyLengthInSeconds = intent.getLongExtra("studyLength", 0L) * 1000
        initBreakLengthInSeconds = intent.getLongExtra("breakLength", 0L) * 1000
        sessionType = intent.getStringExtra("sessionType")!!
        if (sessionType == STUDY_SESSION) {
            timeLeftInMillis = initStudyLengthInSeconds
            progressBar.max = initStudyLengthInSeconds.toInt()
            sessionTypeTextView.text = getString(R.string.study_session_title)
        } else {
            timeLeftInMillis = initBreakLengthInSeconds
            progressBar.max = initBreakLengthInSeconds.toInt()
            sessionTypeTextView.text = getString(R.string.break_session_title)
        }

        startButton.setOnClickListener{startTimer()}
        pauseButton.setOnClickListener{pauseTimer()}
        stopButton.setOnClickListener{onTimerStop()}

        if (savedInstanceState == null || !savedInstanceState.getBoolean("timerIsRunning")) {
            startTimer()
        }
    }

    private fun startTimer() {
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        endTime = System.currentTimeMillis() + timeLeftInMillis
        timer = object : CountDownTimer(timeLeftInMillis, 10) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timerIsRunning = false
                if (!supportFragmentManager.isDestroyed) {
                    bundle.putLong("studyLength", initStudyLengthInSeconds)
                    bundle.putLong("breakLength", initBreakLengthInSeconds)
                    if (sessionType == STUDY_SESSION) {
                        bundle.putInt(Dialog.DIALOG_KEY, Dialog.POMODORO_START_BREAK_DIALOG)
                    } else if (sessionType == BREAK_SESSION) {
                        bundle.putInt(Dialog.DIALOG_KEY, Dialog.POMODORO_START_STUDY_DIALOG)
                    }
                    dialog.arguments = bundle
                    dialog.show(supportFragmentManager, TAG)
                }
            }
        }.start()
        timerIsRunning = true
        timerIsPaused = false
    }

    private fun pauseTimer() {
        pauseButton.isEnabled = false
        startButton.isEnabled = true
        endTime = System.currentTimeMillis() + timeLeftInMillis
        timer.cancel()
        updateCountDownText()
        timerIsPaused = true
    }

    private fun onTimerStop() {
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        val intentShareLocationService = Intent(applicationContext, ShareLocationService::class.java)
        applicationContext.stopService(intentShareLocationService)
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000).toInt() / 60
        val seconds = (timeLeftInMillis / 1000).toInt() % 60
        val timeLeftFormatted: String =
            java.lang.String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        textView.text = timeLeftFormatted
        progressBar.progress = if (sessionType == STUDY_SESSION) (initStudyLengthInSeconds - timeLeftInMillis).toInt() else (initBreakLengthInSeconds - timeLeftInMillis).toInt()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeLeftInMillis", timeLeftInMillis)
        outState.putBoolean("timerIsRunning", timerIsRunning)
        outState.putBoolean("timerIsPaused", timerIsPaused)
        outState.putLong("endTime", endTime)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis")
        timerIsRunning = savedInstanceState.getBoolean("timerIsRunning")
        timerIsPaused = savedInstanceState.getBoolean("timerIsPaused")
        updateCountDownText()
        if (timerIsRunning && !timerIsPaused) {
            endTime = savedInstanceState.getLong("endTime")
            timeLeftInMillis = endTime - System.currentTimeMillis()
            startTimer()
        }
        if(timerIsPaused) {
            pauseButton.isEnabled = false
        }
    }
}