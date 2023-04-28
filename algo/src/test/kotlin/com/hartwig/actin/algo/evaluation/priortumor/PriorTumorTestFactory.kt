package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.apache.logging.log4j.util.Strings

internal object PriorTumorTestFactory {
    fun builder(): ImmutablePriorSecondPrimary.Builder {
        return ImmutablePriorSecondPrimary.builder()
            .tumorLocation(Strings.EMPTY)
            .tumorSubLocation(Strings.EMPTY)
            .tumorType(Strings.EMPTY)
            .tumorSubType(Strings.EMPTY)
            .treatmentHistory(Strings.EMPTY)
            .isActive(false)
    }

    fun withPriorSecondPrimary(priorSecondPrimary: PriorSecondPrimary): PatientRecord {
        return withPriorSecondPrimaries(listOf(priorSecondPrimary))
    }

    fun withPriorSecondPrimaries(priorSecondPrimaries: List<PriorSecondPrimary>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .priorSecondPrimaries(priorSecondPrimaries)
                    .build()
            )
            .build()
    }
}