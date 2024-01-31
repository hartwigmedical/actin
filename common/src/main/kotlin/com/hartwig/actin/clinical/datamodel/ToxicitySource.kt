package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.Displayable

enum class ToxicitySource(private val display: String) : Displayable {
    QUESTIONNAIRE("Questionnaire"),
    EHR("EHR");

    override fun display(): String {
        return display
    }
}
