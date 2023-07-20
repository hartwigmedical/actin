package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test

class HasExperiencedImmuneRelatedAdverseEventsTest {
    @Test
    fun shouldFailWithNoTreatmentHistory() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailWithTreatmentWithOtherCategory() {
        val treatments = listOf(treatmentHistoryEntry(TreatmentCategory.TARGETED_THERAPY))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldWarnWithImmunotherapyTreatment() {
        val treatments = listOf(treatmentHistoryEntry(TreatmentCategory.IMMUNOTHERAPY))
        assertEvaluation(EvaluationResult.WARN, FUNCTION.evaluate(withTreatmentHistory(treatments)))
    }
    
    companion object {
        val FUNCTION = HasExperiencedImmuneRelatedAdverseEvents()

        private fun withTreatmentHistory(treatmentHistory: List<TreatmentHistoryEntry>): PatientRecord {
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

        private fun treatmentHistoryEntry(category: TreatmentCategory): TreatmentHistoryEntry {
            return ImmutableTreatmentHistoryEntry.builder().addTreatments(treatment(category)).build()
        }

        private fun treatment(category: TreatmentCategory): Treatment {
            return ImmutableDrugTherapy.builder().name("").isSystemic(true).addDrugs(
                ImmutableDrug.builder()
                    .name("")
                    .category(category)
                    .drugTypes(emptySet())
                    .build()
            ).build()
        }
    }
}