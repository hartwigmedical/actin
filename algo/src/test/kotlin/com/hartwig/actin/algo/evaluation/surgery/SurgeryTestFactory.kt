package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

internal object SurgeryTestFactory {
    private val base = TestDataFactory.createMinimalTestPatientRecord()

    fun surgery(endDate: LocalDate, status: SurgeryStatus = SurgeryStatus.UNKNOWN): Surgery {
        return Surgery(endDate = endDate, status = status)
    }

    fun withSurgery(surgery: Surgery): PatientRecord {
        return withSurgeries(listOf(surgery))
    }

    fun withSurgeries(surgeries: List<Surgery>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(surgeries = surgeries))
    }

    fun withOncologicalHistory(treatments: List<TreatmentHistoryEntry>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(oncologicalHistory = treatments))
    }
}