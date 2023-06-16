package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.apache.logging.log4j.util.Strings

internal object TreatmentTestFactory {

    fun builder(): ImmutablePriorTumorTreatment.Builder {
        return ImmutablePriorTumorTreatment.builder().isSystemic(false).name(Strings.EMPTY)
    }

    fun withPriorTumorTreatment(priorTumorTreatment: PriorTumorTreatment): PatientRecord {
        return withPriorTumorTreatments(listOf(priorTumorTreatment))
    }

    fun withPriorTumorTreatments(priorTumorTreatments: List<PriorTumorTreatment>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .priorTumorTreatments(priorTumorTreatments)
                    .build()
            )
            .build()
    }

    fun priorSecondPrimaryBuilder(): ImmutablePriorSecondPrimary.Builder {
        return ImmutablePriorSecondPrimary.builder()
            .tumorLocation(Strings.EMPTY)
            .tumorSubLocation(Strings.EMPTY)
            .tumorType(Strings.EMPTY)
            .tumorSubType(Strings.EMPTY)
            .treatmentHistory(Strings.EMPTY)
            .isActive(false)
    }
}