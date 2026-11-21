package com.forgecrew.drillschool

/** Half-hour toolface increments are shared by all three modes. */
enum class RotationSensitivity(val title: String, val stepDelayMs: Long) {
    PRECISION("Precision", 900L),
    NORMAL("Normal", 600L),
    FAST("Fast", 350L)
}
