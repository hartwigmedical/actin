package com.hartwig.actin.datamodel.molecular.panel

import java.time.LocalDate

data class PanelTestSpecification(val testName: String, val versionDate: LocalDate? = null) {
    
    override fun toString(): String = "$testName${versionDate?.let { " version $it" } ?: ""}"
}
