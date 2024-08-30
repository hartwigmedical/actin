package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class ToxicitySource(private val display: String) : Displayable {
    QUESTIONNAIRE("Questionnaire"),
    EHR("EHR");

    override fun display(): String {
        return display
    }
}
