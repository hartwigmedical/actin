package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

object TreatmentTestFactory {

    fun treatment(name: String, isSystemic: Boolean, categories: Set<TreatmentCategory> = emptySet()): Treatment {
        return ImmutableOtherTreatment.builder().name(name).isSystemic(isSystemic).categories(categories).build()
    }

    fun drugTherapy(name: String, category: TreatmentCategory, types: Set<DrugType> = emptySet()): Treatment {
        return ImmutableDrugTherapy.builder().name(name).isSystemic(true).addDrugs(
            ImmutableDrug.builder()
                .name(name)
                .category(category)
                .drugTypes(types)
                .build()
        ).build()
    }

    fun drugTherapyNoDrugs(name: String): Treatment {
        return ImmutableDrugTherapy.builder().name(name).isSystemic(true).build()
    }

    fun otherTreatment(name: String, category: TreatmentCategory? = null): Treatment {
        val builder = ImmutableOtherTreatment.builder().name(name).isSystemic(true)
        if (null != category) {
            builder.addCategories(category)
        }
        return builder.build()
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
        numCycles: Int? = null
    ): TreatmentHistoryEntry {
        val therapyHistoryDetails = if (stopReason != null || stopYear != null || bestResponse != null) {
            ImmutableTherapyHistoryDetails.builder()
                .stopReason(stopReason)
                .bestResponse(bestResponse)
                .stopYear(stopYear)
                .stopMonth(stopMonth)
                .cycles(numCycles)
                .build()
        } else null
        return ImmutableTreatmentHistoryEntry.builder()
            .treatments(treatments)
            .startYear(startYear)
            .startMonth(startMonth)
            .therapyHistoryDetails(therapyHistoryDetails)
            .intents(intents)
            .isTrial(isTrial)
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