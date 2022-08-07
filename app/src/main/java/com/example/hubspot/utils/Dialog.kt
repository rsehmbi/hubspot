package com.example.hubspot.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.hubspot.R
import com.example.hubspot.studybuddy.pomodoro.PomodoroActivity

class Dialog : DialogFragment(), DialogInterface.OnClickListener{
    companion object{
        const val DIALOG_KEY = "dialog_key"
        const val POMODORO_START_STUDY_DIALOG = 1
        const val POMODORO_START_BREAK_DIALOG = 2
    }

    private var dialogId: Int? = 0
    private lateinit var dialogTextView: TextView
    private var pomodoroStudyLength: Long = 0L
    private var pomodoroBreakLength: Long = 0L

    // Create a dialog that corresponds to one of the exposed dialogIDs
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val bundle = arguments
        dialogId = bundle?.getInt(DIALOG_KEY)
        val builder = AlertDialog.Builder(requireActivity())
        val infoDialogView: View = requireActivity().layoutInflater.inflate(R.layout.dialog_info, null)
        when (dialogId) {
            POMODORO_START_STUDY_DIALOG -> {
                pomodoroStudyLength = bundle?.getLong("studyLength")!! / 1000
                pomodoroBreakLength = bundle.getLong("breakLength") / 1000
                dialogTextView = infoDialogView.findViewById(R.id.infoTextView)
                dialogTextView.text = getString(R.string.ask_continue)
                builder.setView(infoDialogView)
                builder.setTitle(getString(R.string.start_study_dialog_title))
                builder.setPositiveButton(getString(R.string.yes), this)
                builder.setNegativeButton(getString(R.string.end_session), this)
                ret = builder.create()
            }
            POMODORO_START_BREAK_DIALOG -> {
                pomodoroStudyLength = bundle?.getLong("studyLength")!! / 1000
                pomodoroBreakLength = bundle.getLong("breakLength") / 1000
                dialogTextView = infoDialogView.findViewById(R.id.infoTextView)
                dialogTextView.text = getString(R.string.ask_continue)
                builder.setView(infoDialogView)
                builder.setTitle(getString(R.string.start_break_dialog_title))
                builder.setPositiveButton(getString(R.string.yes), this)
                builder.setNegativeButton(getString(R.string.end_session), this)
                ret = builder.create()
            }
        }
        return ret
    }

    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            when (dialogId){
                // save data from dialogs to sharedPref
                POMODORO_START_STUDY_DIALOG -> {
                    dialog.dismiss()
                    requireActivity().finish()
                    val intent = Intent(requireActivity(), PomodoroActivity::class.java)
                    intent.putExtra("studyLength", pomodoroStudyLength)
                    intent.putExtra("breakLength", pomodoroBreakLength)
                    intent.putExtra("sessionType", PomodoroActivity.STUDY_SESSION)
                    startActivity(intent)
                }
                POMODORO_START_BREAK_DIALOG -> {
                    dialog.dismiss()
                    requireActivity().finish()
                    val intent = Intent(requireActivity(), PomodoroActivity::class.java)
                    intent.putExtra("studyLength", pomodoroStudyLength)
                    intent.putExtra("breakLength", pomodoroBreakLength)
                    intent.putExtra("sessionType", PomodoroActivity.BREAK_SESSION)
                    startActivity(intent)
                }
            }
        } else {
            dialog.dismiss()
            requireActivity().finish()
        }
    }
}