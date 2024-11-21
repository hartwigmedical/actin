package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.datamodel.Displayable

internal enum class PerformanceScore : Displayable {
    LANSKY,
    KARNOFSKY;

    override fun display(): String {
        return name.substring(0, 1).uppercase() + name.substring(1).lowercase()
    }
}