package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

object TreatmentTestFactory {

    fun treatment(name: String, isSystemic: Boolean, categories: Set<TreatmentCategory> = emptySet()): Treatment {
        return OtherTreatment(name = name, isSystemic = isSystemic, synonyms = emptySet(), displayOverride = null, categories = categories)
    }

    fun drugTreatment(name: String, category: TreatmentCategory, types: Set<DrugType> = emptySet()): DrugTreatment {
        return DrugTreatment(
            name = name,
            drugs = setOf(Drug(name = name, category = category, drugTypes = types))
        )
    }

    fun drugTreatmentNoDrugs(name: String): Treatment {
        return DrugTreatment(name = name, drugs = emptySet())
    }

    fun treatmentHistoryEntry(
        treatments: Collection<Treatment> = emptySet(),
        startYear: Int? = null,
        startMonth: Int? = null,
        stopReason: StopReason? = null,
        bestResponse: TreatmentResponse? = null,
        stopYear: Int? = null,
        stopMonth: Int? = null,
        intents: Set<Intent> = emptySet(),
        isTrial: Boolean = false,
        numCycles: Int? = null,
        switchToTreatments: List<TreatmentStage>? = null,
        maintenanceTreatment: TreatmentStage? = null,
        stopReasonDetail: String? = null,
        trialAcronym: String? = null
    ): TreatmentHistoryEntry {
        val treatmentHistoryDetails = if (listOf(
                stopReason, bestResponse, stopYear, stopMonth, numCycles, switchToTreatments, maintenanceTreatment, stopReasonDetail
            ).any { it != null }
        ) {
            TreatmentHistoryDetails(
                stopReason = stopReason,
                bestResponse = bestResponse,
                stopYear = stopYear,
                stopMonth = stopMonth,
                cycles = numCycles,
                switchToTreatments = switchToTreatments,
                maintenanceTreatment = maintenanceTreatment,
                stopReasonDetail = stopReasonDetail,
                ongoingAsOf = null,
            )
        } else null
        return TreatmentHistoryEntry(
            treatments = treatments.toSet(),
            startYear = startYear,
            startMonth = startMonth,
            treatmentHistoryDetails = treatmentHistoryDetails,
            intents = intents,
            isTrial = isTrial,
            trialAcronym = trialAcronym
        )
    }

    fun treatmentStage(treatment: Treatment, startYear: Int? = null, startMonth: Int? = null, cycles: Int? = null): TreatmentStage {
        return TreatmentStage(
            treatment = treatment,
            startYear = startYear,
            startMonth = startMonth,
            cycles = cycles
        )
    }

    fun withTreatmentHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): PatientRecord {
        return withTreatmentHistory(listOf(treatmentHistoryEntry))
    }

    fun withTreatmentHistory(treatmentHistory: List<TreatmentHistoryEntry>): PatientRecord {
        return TestDataFactory.createMinimalTestPatientRecord().copy(
            clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(oncologicalHistory = treatmentHistory)
        )
    }
}