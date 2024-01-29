package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus

internal object PriorTumorTestFactory {
    val base = TestDataFactory.createMinimalTestPatientRecord()

    fun priorSecondPrimary(
        doid: String? = null,
        diagnosedYear: Int? = null,
        lastTreatmentYear: Int? = null,
        lastTreatmentMonth: Int? = null,
        status: TumorStatus = TumorStatus.INACTIVE
    ): PriorSecondPrimary {
        return PriorSecondPrimary(
            tumorLocation = "",
            tumorSubLocation = "",
            tumorType = "",
            tumorSubType = "",
            doids = setOfNotNull(doid),
            diagnosedYear = diagnosedYear,
            treatmentHistory = "",
            lastTreatmentYear = lastTreatmentYear,
            lastTreatmentMonth = lastTreatmentMonth,
            status = status
        )
    }

    fun withPriorSecondPrimary(priorSecondPrimary: PriorSecondPrimary): PatientRecord {
        return withPriorSecondPrimaries(listOf(priorSecondPrimary))
    }

    fun withPriorSecondPrimaries(priorSecondPrimaries: List<PriorSecondPrimary>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(priorSecondPrimaries = priorSecondPrimaries))
    }
}