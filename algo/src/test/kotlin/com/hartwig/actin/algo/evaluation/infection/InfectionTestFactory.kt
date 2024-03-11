package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition

internal object InfectionTestFactory {
    fun withPriorOtherCondition(conditions: PriorOtherCondition): PatientRecord {
        return withPriorOtherConditions(listOf(conditions))
    }

    fun withPriorOtherConditions(conditions: List<PriorOtherCondition>): PatientRecord {
        return TestDataFactory.createMinimalTestPatientRecord().copy(priorOtherConditions = conditions)
    }

    fun priorOtherCondition(name: String = "", doids: Set<String> = emptySet()): PriorOtherCondition {
        return PriorOtherCondition(name = name, category = "", isContraindicationForTherapy = true, doids = doids)
    }
}