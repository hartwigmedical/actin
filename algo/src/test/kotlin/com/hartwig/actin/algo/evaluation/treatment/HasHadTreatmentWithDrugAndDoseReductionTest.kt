package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentsAndMedications
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private const val MATCHING_DRUG_NAME = "match"
private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY

class HasHadTreatmentWithDrugAndDoseReductionTest {

    private val function = HasHadTreatmentWithDrugAndDoseReduction(drugWithName(MATCHING_DRUG_NAME))

    fun drugWithName(drugName: String): Drug = Drug(name = drugName, category = TREATMENT_CATEGORY, drugTypes = emptySet())

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for no matching drugs in treatment history`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(drugWithName("other_drug"))
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for no matching drugs in medication`() {
        val treatmentHistory = treatmentHistoryEntry(emptyList())
        val medication = WashoutTestFactory.medication().copy(drug = drugWithName("other_drug"))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentsAndMedications(listOf(treatmentHistory), listOf(medication)))
        )
    }

    @Test
    fun `Should be undetermined if matching drugs in treatment history`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(drugWithName(MATCHING_DRUG_NAME))
                    ),
                    DrugTreatment(
                        "other_treatment",
                        setOf(drugWithName("other_drug"))
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should be undetermined if matching drugs in medication`() {
        val treatmentHistory = treatmentHistoryEntry(emptyList())
        val medication = WashoutTestFactory.medication().copy(drug = drugWithName(MATCHING_DRUG_NAME))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentsAndMedications(listOf(treatmentHistory), listOf(medication)))
        )
    }
}