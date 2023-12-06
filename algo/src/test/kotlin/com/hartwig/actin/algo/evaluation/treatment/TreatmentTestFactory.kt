package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

object TreatmentTestFactory {

    fun treatment(name: String, isSystemic: Boolean, categories: Set<TreatmentCategory> = emptySet()): Treatment {
        return ImmutableOtherTreatment.builder().name(name).isSystemic(isSystemic).categories(categories).build()
    }

    fun drugTreatment(name: String, category: TreatmentCategory, types: Set<DrugType> = emptySet()): Treatment {
        return ImmutableDrugTreatment.builder().name(name).isSystemic(true).addDrugs(
            ImmutableDrug.builder()
                .name(name)
                .category(category)
                .drugTypes(types)
                .build()
        ).build()
    }

    fun drugTreatmentNoDrugs(name: String): Treatment {
        return ImmutableDrugTreatment.builder().name(name).isSystemic(true).build()
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
        maintenanceTreatment: TreatmentStage? = null
    ): TreatmentHistoryEntry {
        val treatmentHistoryDetails = if (listOf(
                stopReason, bestResponse, stopYear, stopMonth, numCycles, switchToTreatments, maintenanceTreatment
            ).any { it != null }
        ) {
            ImmutableTreatmentHistoryDetails.builder()
                .stopReason(stopReason)
                .bestResponse(bestResponse)
                .stopYear(stopYear)
                .stopMonth(stopMonth)
                .cycles(numCycles)
                .switchToTreatments(switchToTreatments)
                .maintenanceTreatment(maintenanceTreatment)
                .build()
        } else null
        return ImmutableTreatmentHistoryEntry.builder()
            .treatments(treatments)
            .startYear(startYear)
            .startMonth(startMonth)
            .treatmentHistoryDetails(treatmentHistoryDetails)
            .intents(intents)
            .isTrial(isTrial)
            .build()
    }

    fun treatmentStage(treatment: Treatment, startYear: Int? = null, startMonth: Int? = null, cycles: Int? = null): TreatmentStage {
        return ImmutableTreatmentStage.builder()
            .treatment(treatment)
            .startYear(startYear)
            .startMonth(startMonth)
            .cycles(cycles)
            .build()
    }

    fun withTreatmentHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): PatientRecord {
        return withTreatmentHistory(listOf(treatmentHistoryEntry))
    }

    fun withTreatmentHistory(treatmentHistory: List<TreatmentHistoryEntry>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .treatmentHistory(treatmentHistory)
                    .build()
            )
            .build()
    }

}