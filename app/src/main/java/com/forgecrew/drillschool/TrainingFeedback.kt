package com.forgecrew.drillschool

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** User-selectable, short vibration cues. No persistent background activity. */
enum class FeedbackCue { ENGAGE, CLOCK, LOCATOR, ALERT, COMPLETE }

object TrainingFeedback {
    fun play(context: Context, level: Int, cue: FeedbackCue) {
        if (level == 0) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } ?: return
        if (!vibrator.hasVibrator()) return
        val amplitude = when (level.coerceIn(1, 3)) { 1 -> 50; 2 -> 115; else -> 210 }
        val pattern = when (cue) {
            FeedbackCue.ENGAGE -> longArrayOf(0, 24)
            FeedbackCue.CLOCK -> longArrayOf(0, 34)
            FeedbackCue.LOCATOR -> longArrayOf(0, 45, 75, 45)
            FeedbackCue.ALERT -> longArrayOf(0, 115, 95, 115)
            FeedbackCue.COMPLETE -> longArrayOf(0, 50, 60, 70, 60, 110)
        }
        val powers = IntArray(pattern.size) { index -> if (index % 2 == 0) 0 else amplitude }
        runCatching { vibrator.vibrate(VibrationEffect.createWaveform(pattern, powers, -1)) }
    }
}
