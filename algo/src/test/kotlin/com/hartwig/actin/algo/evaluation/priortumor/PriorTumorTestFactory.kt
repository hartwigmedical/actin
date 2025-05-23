package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TumorStatus

internal object PriorTumorTestFactory {
    val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

    fun priorPrimary(
        doid: String? = null,
        diagnosedYear: Int? = null,
        diagnosedMonth: Int? = null,
        lastTreatmentYear: Int? = null,
        lastTreatmentMonth: Int? = null,
        status: TumorStatus = TumorStatus.INACTIVE
    ): PriorPrimary {
        return PriorPrimary(
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

    fun withPriorPrimary(priorPrimary: PriorPrimary): PatientRecord {
        return withPriorPrimaries(listOf(priorPrimary))
    }

    fun withPriorPrimaries(priorPrimaries: List<PriorPrimary>): PatientRecord {
        return base.copy(priorPrimaries = priorPrimaries)
    }
}