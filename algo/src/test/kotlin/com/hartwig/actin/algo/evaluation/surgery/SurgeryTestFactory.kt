package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

internal object SurgeryTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

    fun surgery(endDate: LocalDate?, status: SurgeryStatus = SurgeryStatus.UNKNOWN, treatmentType: OtherTreatmentType = OtherTreatmentType.OTHER_SURGERY): Surgery {
        return Surgery(name = "Surgery", endDate = endDate, status = status, treatmentType = treatmentType)
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

    fun withSurgeriesAndOncologicalHistory(
        treatments: List<TreatmentHistoryEntry>?,
        surgeries: List<Surgery>?,
    ): PatientRecord {
        return base.copy(
            surgeries = surgeries ?: base.surgeries,
            oncologicalHistory = treatments ?: base.oncologicalHistory,
        )
    }
}