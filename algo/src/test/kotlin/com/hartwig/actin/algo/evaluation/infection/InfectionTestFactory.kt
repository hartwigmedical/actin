package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition

internal object InfectionTestFactory {
    fun withPriorOtherCondition(conditions: PriorOtherCondition): PatientRecord {
        return withPriorOtherConditions(listOf(conditions))
    }

    fun withPriorOtherConditions(conditions: List<PriorOtherCondition>): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(priorOtherConditions = conditions)
    }

    fun priorOtherCondition(name: String = "", doids: Set<String> = emptySet()): PriorOtherCondition {
        return PriorOtherCondition(name = name, category = "", isContraindicationForTherapy = true, doids = doids)
    }
}