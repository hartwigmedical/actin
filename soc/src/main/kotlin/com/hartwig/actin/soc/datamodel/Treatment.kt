package com.hartwig.actin.soc.datamodel

import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import com.hartwig.actin.treatment.datamodel.EligibilityFunction

data class Treatment(
    val name: String,
    val isOptional: Boolean,
    val score: Int,
    val components: Set<TreatmentComponent>,
    val categories: Set<TreatmentCategory>,
    val eligibilityFunctions: Set<EligibilityFunction>,
    val lines: Set<Int>
)