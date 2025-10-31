// app/src/main/java/com/example/automation/ui/session_log/SessionLogFragment.kt
package com.example.automation.ui.session_log

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.automation.databinding.FragmentSessionLogBinding
import com.example.automation.ui.AppVMFactory
import com.example.automation.ui.SessionLogViewModel
import com.google.android.material.snackbar.Snackbar

class SessionLogFragment : Fragment() {
    private var _vb: FragmentSessionLogBinding? = null
    private val vb get() = _vb!!

    // --- ViewModel (persists the session to Room) ---
    private val vm: SessionLogViewModel by viewModels {
        AppVMFactory(requireActivity().application)
    }

    // --- timer state ---
    private var startMs: Long = 0L      // when the running timer started (ms)
    private var startAt: Long = 0L      // when the session started (ms) used for persistence
    private var running = false
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            if (running) {
                val elapsed = (System.currentTimeMillis() - startMs) / 1000
                val mm = (elapsed / 60).toInt()
                val ss = (elapsed % 60).toInt()
                vb.txtTimer.text = String.format("%02d:%02d", mm, ss)
                handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _vb = FragmentSessionLogBinding.inflate(inflater, container, false)
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Back arrow
        vb.toolbar.setNavigationOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }

        // Nav args
        val lessonId = arguments?.getLong("lessonId") ?: 0L
        val lessonTitle = arguments?.getString("lessonTitle") ?: "Session"
        vb.txtLessonTitle.text = lessonTitle

        // Start
        vb.btnStart.setOnClickListener {
            if (!running) {
                running = true
                startAt = System.currentTimeMillis()       // for DB
                startMs = startAt                           // for UI timer
                handler.post(ticker)
            }
        }

        // Stop (pauses the ticking; you can restart if you want)
        vb.btnStop.setOnClickListener {
            if (running) running = false
        }

        // Save to Room through the VM
        vb.btnSave.setOnClickListener {
            val durationText = vb.txtTimer.text.toString() // "MM:SS"
            val mm = durationText.take(2).toIntOrNull() ?: 0
            val ss = durationText.takeLast(2).toIntOrNull() ?: 0
            val durationSec = (mm * 60 + ss).toLong()
            val notes = vb.edtNotes.text?.toString().orEmpty()

            // If user never hit Start, anchor to "now"
            val started = if (startAt > 0) startAt else System.currentTimeMillis()

            vm.save(lessonId, started, durationSec, notes)
            Snackbar.make(vb.root, "Saved session ($durationText)", Snackbar.LENGTH_LONG).show()

            // optional: reset UI
            vb.txtTimer.text = "00:00"
            vb.edtNotes.setText("")
            running = false
        }
    }

    override fun onDestroyView() {
        running = false
        handler.removeCallbacks(ticker)
        _vb = null
        super.onDestroyView()
    }
}
