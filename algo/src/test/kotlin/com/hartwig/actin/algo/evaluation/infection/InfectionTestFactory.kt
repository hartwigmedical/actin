package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.apache.logging.log4j.util.Strings

internal object InfectionTestFactory {
    fun withPriorOtherCondition(conditions: PriorOtherCondition): PatientRecord {
        return withPriorOtherConditions(listOf(conditions))
    }

    fun withPriorOtherConditions(conditions: List<PriorOtherCondition>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .priorOtherConditions(conditions)
                    .build()
            )
            .build()
    }

    fun builder(): ImmutablePriorOtherCondition.Builder {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(true)
    }
}