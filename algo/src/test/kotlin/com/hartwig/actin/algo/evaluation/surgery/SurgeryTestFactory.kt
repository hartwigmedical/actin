package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import java.time.LocalDate

internal object SurgeryTestFactory {
    fun builder(): ImmutableSurgery.Builder {
        return ImmutableSurgery.builder().endDate(LocalDate.of(2020, 4, 5)).status(SurgeryStatus.UNKNOWN)
    }

    fun withSurgery(surgery: Surgery): PatientRecord {
        return withSurgeries(listOf(surgery))
    }

    fun withSurgeries(surgeries: List<Surgery>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .surgeries(surgeries)
                    .build()
            )
            .build()
    }

    fun withPriorTumorTreatments(treatments: List<PriorTumorTreatment>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .priorTumorTreatments(treatments)
                    .build()
            )
            .build()
    }
}