package com.example.hubspot.studybuddy.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.R
import com.example.hubspot.utils.Dialog
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

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
    private var secondsUntilFinished = 0L
    private lateinit var timerState: TimerState
    private lateinit var sessionType: String
    private enum class TimerState{
        Paused, Running
    }
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
        initStudyLengthInSeconds = intent.getLongExtra("studyLength", 0L)
        initBreakLengthInSeconds = intent.getLongExtra("breakLength", 0L)
        sessionType = intent.getStringExtra("sessionType")!!
        if (sessionType == STUDY_SESSION) {
            secondsUntilFinished = initStudyLengthInSeconds
            progressBar.max = initStudyLengthInSeconds.toInt()
            sessionTypeTextView.text = getString(R.string.study_session_title)
        } else {
            secondsUntilFinished = initBreakLengthInSeconds
            progressBar.max = initBreakLengthInSeconds.toInt()
            sessionTypeTextView.text = getString(R.string.break_session_title)
        }

        startButton.setOnClickListener{onTimerStart()}
        pauseButton.setOnClickListener{onTimerPause()}
        stopButton.setOnClickListener{onTimerStop()}

        onTimerStart()
        timerState = TimerState.Running
    }

    private fun onTimerStart(){
        startButton.isEnabled = false
        timer = object : CountDownTimer( secondsUntilFinished * 1000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                secondsUntilFinished = millisUntilFinished / 1000
                val minutesUntilFinished = (secondsUntilFinished) / 60
                val secondsInMinuteUntilFinished = secondsUntilFinished - minutesUntilFinished * 60
                val secondsStr = if (secondsInMinuteUntilFinished.toString().length == 2) secondsInMinuteUntilFinished else "0$secondsInMinuteUntilFinished"
                textView.text = getString(R.string.timer, "$minutesUntilFinished:$secondsStr")
                progressBar.progress = if (sessionType == STUDY_SESSION) (initStudyLengthInSeconds - secondsUntilFinished).toInt() else (initBreakLengthInSeconds - secondsUntilFinished).toInt()
            }

            override fun onFinish() {
                bundle.putLong("studyLength", initStudyLengthInSeconds)
                bundle.putLong("breakLength", initBreakLengthInSeconds)
                if(sessionType == STUDY_SESSION) {
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.POMODORO_START_BREAK_DIALOG)
                } else if (sessionType == BREAK_SESSION) {
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.POMODORO_START_STUDY_DIALOG)
                }
                dialog.arguments = bundle
                dialog.show(supportFragmentManager, TAG)
            }
        }.start()
    }

    private fun onTimerPause() {
        timer.cancel()
        timerState = TimerState.Paused
        startButton.isEnabled = true
    }

    private fun onTimerStop(){
        if(timerState == TimerState.Running) {
            timer.cancel()
        }
        this.finish()
    }
}