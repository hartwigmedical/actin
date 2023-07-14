package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.apache.logging.log4j.util.Strings

object TreatmentTestFactory {

    fun treatment(name: String, isSystemic: Boolean): Treatment {
        return ImmutableOtherTreatment.builder().name(name).isSystemic(isSystemic).build()
    }

    fun drugTherapyWithCategory(name: String, category: TreatmentCategory): Treatment {
        return ImmutableDrugTherapy.builder().name(name).isSystemic(true).addDrugs(
            ImmutableDrug.builder()
                .name(name)
                .category(category)
                .build()
        ).build()
    }

    fun treatmentHistoryEntry(
        treatments: Set<Treatment> = emptySet(),
        startYear: Int? = null,
        startMonth: Int? = null,
        stopReason: StopReason? = null,
        bestResponse: TreatmentResponse? = null,
        stopYear: Int? = null,
        stopMonth: Int? = null
    ): TreatmentHistoryEntry {
        val therapyHistoryDetails = if (stopReason != null || stopYear != null || bestResponse != null) {
            ImmutableTherapyHistoryDetails.builder()
                .stopReason(stopReason)
                .bestResponse(bestResponse)
                .stopYear(stopYear)
                .stopMonth(stopMonth)
                .build()
        } else null
        return ImmutableTreatmentHistoryEntry.builder()
            .treatments(treatments)
            .startYear(startYear)
            .startMonth(startMonth)
            .therapyHistoryDetails(therapyHistoryDetails)
            .build()
    }

    fun builder(): ImmutablePriorTumorTreatment.Builder {
        return ImmutablePriorTumorTreatment.builder().isSystemic(false).name(Strings.EMPTY)
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

    fun withPriorTumorTreatment(priorTumorTreatment: PriorTumorTreatment): PatientRecord {
        return withPriorTumorTreatments(listOf(priorTumorTreatment))
    }

    fun withPriorTumorTreatments(priorTumorTreatments: List<PriorTumorTreatment>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .priorTumorTreatments(priorTumorTreatments)
                    .build()
            )
            .build()
    }

    fun priorSecondPrimaryBuilder(): ImmutablePriorSecondPrimary.Builder {
        return ImmutablePriorSecondPrimary.builder()
            .tumorLocation(Strings.EMPTY)
            .tumorSubLocation(Strings.EMPTY)
            .tumorType(Strings.EMPTY)
            .tumorSubType(Strings.EMPTY)
            .treatmentHistory(Strings.EMPTY)
            .isActive(false)
    }
}