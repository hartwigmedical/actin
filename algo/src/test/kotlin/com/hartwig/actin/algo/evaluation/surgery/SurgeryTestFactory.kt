package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

internal object SurgeryTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

    fun surgery(endDate: LocalDate, status: SurgeryStatus = SurgeryStatus.UNKNOWN): Surgery {
        return Surgery(name = "Surgery", endDate = endDate, status = status)
    }

    fun withSurgery(surgery: Surgery): PatientRecord {
        return withSurgeries(listOf(surgery))
    }

    fun withSurgeries(surgeries: List<Surgery>): PatientRecord {
        return base.copy(surgeries = surgeries)
    }

    fun withOncologicalHistory(treatments: List<TreatmentHistoryEntry>): PatientRecord {
        return base.copy(oncologicalHistory = treatments)
    }
}