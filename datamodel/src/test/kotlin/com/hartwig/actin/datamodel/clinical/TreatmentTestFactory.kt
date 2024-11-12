package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.NoTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage

object TreatmentTestFactory {

    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

    fun treatment(
        name: String, isSystemic: Boolean, categories: Set<TreatmentCategory> = emptySet(), types: Set<OtherTreatmentType> = emptySet()
    ): Treatment {
        return OtherTreatment(
            name = name, isSystemic = isSystemic, synonyms = emptySet(), displayOverride = null, categories = categories, types = types
        )
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

    fun noneTreatment(): Treatment {
        return NoTreatment(
            name = "None",
            isSystemic = false,
            synonyms = setOf("none", "no treatment"),
            displayOverride = null
        )
    }

    fun treatmentHistoryEntry(
        treatments: Collection<Treatment> = emptySet(),
        startYear: Int? = null,
        startMonth: Int? = null,
        stopReason: StopReason? = null,
        bestResponse: TreatmentResponse? = null,
        stopYear: Int? = null,
        stopMonth: Int? = null,
        intents: Set<Intent>? = emptySet(),
        isTrial: Boolean = false,
        numCycles: Int? = null,
        bodyLocations: Set<String>? = null,
        bodyLocationCategory: Set<BodyLocationCategory>? = null,
        switchToTreatments: List<TreatmentStage>? = null,
        maintenanceTreatment: TreatmentStage? = null,
        stopReasonDetail: String? = null,
        trialAcronym: String? = null
    ): TreatmentHistoryEntry {
        val treatmentHistoryDetails = if (listOf(
                stopReason,
                bestResponse,
                stopYear,
                stopMonth,
                numCycles,
                bodyLocations,
                bodyLocationCategory,
                switchToTreatments,
                maintenanceTreatment,
                stopReasonDetail
            ).any { it != null }
        ) {
            TreatmentHistoryDetails(
                stopReason = stopReason,
                bestResponse = bestResponse,
                stopYear = stopYear,
                stopMonth = stopMonth,
                cycles = numCycles,
                bodyLocations = bodyLocations,
                bodyLocationCategories = bodyLocationCategory,
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
        return base.copy(oncologicalHistory = treatmentHistory)
    }

    fun withTreatmentsAndMedications(treatmentHistory: List<TreatmentHistoryEntry>, medications: List<Medication>?): PatientRecord {
        return base.copy(oncologicalHistory = treatmentHistory, medications = medications)
    }
}