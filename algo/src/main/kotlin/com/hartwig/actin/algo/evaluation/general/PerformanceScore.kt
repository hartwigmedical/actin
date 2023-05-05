package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.util.ApplicationConfig

internal enum class PerformanceScore {
    LANSKY, KARNOFSKY;

    fun display(): String {
        val locale = ApplicationConfig.LOCALE
        return name.substring(0, 1).uppercase(locale) + name.substring(1).lowercase(locale)
    }
}