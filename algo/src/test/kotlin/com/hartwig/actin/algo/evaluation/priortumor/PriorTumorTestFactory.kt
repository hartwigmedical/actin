package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus

internal object PriorTumorTestFactory {
    val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

    fun priorSecondPrimary(
        doid: String? = null,
        diagnosedYear: Int? = null,
        diagnosedMonth: Int? = null,
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
            diagnosedMonth = diagnosedMonth,
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
        return base.copy(priorSecondPrimaries = priorSecondPrimaries)
    }
}