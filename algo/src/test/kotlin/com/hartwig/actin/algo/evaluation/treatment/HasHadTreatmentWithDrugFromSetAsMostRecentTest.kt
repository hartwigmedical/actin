package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadTreatmentWithDrugFromSetAsMostRecentTest {

    @Test
    fun `Should fail for empty treatment history`() {
        evaluateFunctions(
            EvaluationResult.FAIL,
            TreatmentTestFactory.withTreatmentHistory(emptyList()),
            "No prior treatments in provided treatments",
            "No prior treatments in provided treatments"
        )
    }

    @Test
    fun `Should fail for non-drug treatment`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("other treatment", false)))
        )
        evaluateFunctions(
            EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(treatmentHistory),
            "No prior treatments in provided treatments", "No prior treatments in provided treatments"
        )
    }

    @Test
    fun `Should fail for therapy containing other drug`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("other treatment", TREATMENT_CATEGORY)))
        )
        evaluateFunctions(
            EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(treatmentHistory),
            "No match in provided treatments", "No match in provided treatments"
        )
    }

    @Test
    fun `Should fail for therapy containing matching drug but not most recent line`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2021
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Other drug", TREATMENT_CATEGORY)), startYear = 2022
            )
        )
        evaluateFunctions(
            EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(treatmentHistory),
            "match in provided treatments but not as most recent line",
            "match in provided treatments but not as most recent line and hence not currently administered"
        )
    }

    @Test
    fun `Should evaluate to undetermined when multiple treatment entries in history of which one contains the target drug with unknown start date`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = null
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Other drug", TREATMENT_CATEGORY)), startYear = 2021
            )
        )
        evaluateFunctions(
            EvaluationResult.UNDETERMINED, TreatmentTestFactory.withTreatmentHistory(treatmentHistory),
            "match in provided treatments but undetermined if most recent (date unknown)",
            "match in provided treatments but undetermined if most recent (date unknown) and unknown if currently still administered"
        )
    }

    @Test
    fun `Should fail if matching drug is not most recent and history contains other random treatment with unknown start date`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2021
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Other drug", TREATMENT_CATEGORY)), startYear = null
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Drug more recent than target drug", TREATMENT_CATEGORY)), startYear = 2022
            )
        )
        evaluateFunctions(
            EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(treatmentHistory),
            "match in provided treatments but not as most recent line",
            "match in provided treatments but not as most recent line and hence not currently administered"
        )
    }

    @Test
    fun `Should be undetermined if most recent line is trial without a treatment specified`() {
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = emptySet(), isTrial = true))
        evaluateFunctions(
            EvaluationResult.UNDETERMINED, TreatmentTestFactory.withTreatmentHistory(treatmentHistory),
            "Undetermined if treatment from previous trial included match",
            "Undetermined if treatment from previous trial included match and unknown if currently still administered"
        )
    }

    @Test
    fun `Should evaluate to undetermined if most recent line is trial with treatment containing drug of target category`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Unknown drug X", category = TREATMENT_CATEGORY)), isTrial = true
            )
        )
        evaluateFunctions(
            EvaluationResult.UNDETERMINED, TreatmentTestFactory.withTreatmentHistory(treatmentHistory),
            "Undetermined if treatment from previous trial included match",
            "Undetermined if treatment from previous trial included match and unknown if currently still administered"
        )
    }

    @Test
    fun `Should pass if matching treatment is only history entry and start date is unknown and not requiring current administration`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY))
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "match in provided treatments as most recent treatment"
        )
    }

    @Test
    fun `Should be undetermined if matching treatment is only history entry and start date is unknown and requiring current administration`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY))
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_CURRENT_ADMINISTRATION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Has received match as most recent treatment but unknown if currently still administered"
        )
    }

    @Test
    fun `Should pass for therapy containing matching drug if not requiring current administration`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2022
            )
        )

        val evaluation = FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        assertEvaluation(EvaluationResult.PASS, evaluation, "match in provided treatments as most recent treatment")
    }

    @Test
    fun `Should ignore radiotherapy and pass when containing matching drug if not requiring current administration`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2022
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(Radiotherapy(name = "radiotherapy", isInternal = true)), startYear = 2023
            )
        )

        val evaluation = FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        assertEvaluation(EvaluationResult.PASS, evaluation, "match in provided treatments as most recent treatment")
    }


    @Test
    fun `Should be undetermined for therapy containing matching drug if requiring current administration and missing stop date`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2022
            )
        )

        val evaluation = FUNCTION_CURRENT_ADMINISTRATION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Has received match as most recent treatment but unknown if currently still administered"
        )
    }

    @Test
    fun `Should fail for therapy containing matching drug if requiring current administration when there is a stop date`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), stopYear = 2022
            )
        )

        val evaluation = FUNCTION_CURRENT_ADMINISTRATION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        assertEvaluation(EvaluationResult.FAIL, evaluation, "Does not currently receive match (treatment has stopped)")
    }

    private fun evaluateFunctions(
        expected: EvaluationResult, record: PatientRecord, expectedMessage: String, expectedMessageForCurrentAdministration: String
    ) {
        assertEvaluation(expected, FUNCTION.evaluate(record), expectedMessage)
        assertEvaluation(expected, FUNCTION_CURRENT_ADMINISTRATION.evaluate(record), expectedMessageForCurrentAdministration)
    }

    companion object {
        private const val MATCHING_DRUG_NAME = "match"
        private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION = HasHadTreatmentWithDrugFromSetAsMostRecent(
            setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet())), false
        )
        private val FUNCTION_CURRENT_ADMINISTRATION = HasHadTreatmentWithDrugFromSetAsMostRecent(
            setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet())), true
        )
    }
}