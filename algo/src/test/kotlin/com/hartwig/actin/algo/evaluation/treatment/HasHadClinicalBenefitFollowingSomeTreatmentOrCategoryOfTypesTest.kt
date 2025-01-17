package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypesTest {

    private val TARGET_CATEGORY = TreatmentCategory.CHEMOTHERAPY
    private val TARGET_TYPES = setOf(DrugType.PLATINUM_COMPOUND)
    private val CORRECT_TREATMENT = TreatmentTestFactory.drugTreatment("Correct", TARGET_CATEGORY, TARGET_TYPES)
    private val OTHER_CORRECT_TREATMENT = CORRECT_TREATMENT.copy(name = "Other correct")
    private val SIMILAR_DRUG_TO_TARGET_TREATMENT =
        TreatmentTestFactory.treatment("Other chemo", true, setOf(TreatmentCategory.CHEMOTHERAPY))
    private val DRUG_WITH_SAME_CATEGORY_BUT_OTHER_TYPE =
        TreatmentTestFactory.drugTreatment("Paclitaxel", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.TAXANE))
    private val CORRECT_TREATMENT_WITH_OTHER_CATEGORY_COMBINATION = setOf(CORRECT_TREATMENT, Radiotherapy("Radiotherapy"))
    private val TARGET_TREATMENT_WITH_SAME_CATEGORY_COMBINATION = setOf(
        CORRECT_TREATMENT, TreatmentTestFactory.treatment(
            "Other chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY)
        )
    )
    private val WRONG_SPECIFIC_TREATMENT = TreatmentTestFactory.treatment("Radiotherapy", false, setOf(TreatmentCategory.RADIOTHERAPY))
    private val WRONG_CATEGORY_TREATMENT =
        TreatmentTestFactory.drugTreatment("wrong1", TreatmentCategory.SUPPORTIVE_TREATMENT, TARGET_TYPES)
    private val WRONG_TYPE_TREATMENT = TreatmentTestFactory.drugTreatment("wrong2", TARGET_CATEGORY, setOf(DrugType.TAXANE))
    private val functionWithSpecificTreatments =
        HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(listOf(CORRECT_TREATMENT, OTHER_CORRECT_TREATMENT))
    private val functionWithSpecificCategoryAndType =
        HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(category = TARGET_CATEGORY, types = TARGET_TYPES)
    private val functionWithSpecificCategory = HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(category = TARGET_CATEGORY)

    @Test
    fun `Should throw an illegal state exception when specific treatment and category and type not specified in function`() {
        Assertions.assertThatIllegalStateException().isThrownBy {
            HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(null, null, null)
                .evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        }.withMessage("Treatment or category must be provided")
    }

    @Test
    fun `Should fail if treatment history is empty`() {
        assertForAllFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(emptyList()))
    }

    @Test
    fun `Should fail if treatment history does not contain target treatment`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_SPECIFIC_TREATMENT)),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_CATEGORY_TREATMENT))
        )
        val wrongTypeHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_TYPE_TREATMENT)))
        assertForAllFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(history))
        assertEvaluation(
            EvaluationResult.FAIL, functionWithSpecificCategoryAndType.evaluate(TreatmentTestFactory.withTreatmentHistory(wrongTypeHistory))
        )
    }

    @Test
    fun `Should pass if treatment history contains target treatment with best response complete response`() {
        listOf(CORRECT_TREATMENT, OTHER_CORRECT_TREATMENT).forEach {
            val history = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(it),
                    bestResponse = TreatmentResponse.COMPLETE_RESPONSE
                )
            )
            assertForAllFunctions(EvaluationResult.PASS, TreatmentTestFactory.withTreatmentHistory(history))
        }
    }

    @Test
    fun `Should pass if treatment history contains target therapy combined with other therapy with best response complete response`() {
        val history1 = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                CORRECT_TREATMENT_WITH_OTHER_CATEGORY_COMBINATION,
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        assertForAllFunctions(EvaluationResult.PASS, TreatmentTestFactory.withTreatmentHistory(history1))

        val history2 = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                TARGET_TREATMENT_WITH_SAME_CATEGORY_COMBINATION,
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        assertForAllFunctions(EvaluationResult.PASS, TreatmentTestFactory.withTreatmentHistory(history2))
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response partial response`() {
        listOf(CORRECT_TREATMENT, OTHER_CORRECT_TREATMENT).forEach {
            val history = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(it),
                    bestResponse = TreatmentResponse.PARTIAL_RESPONSE
                )
            )
            assertForAllFunctions(EvaluationResult.PASS, TreatmentTestFactory.withTreatmentHistory(history))
        }
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response remission`() {
        listOf(CORRECT_TREATMENT, OTHER_CORRECT_TREATMENT).forEach {
            val history = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(it),
                    bestResponse = TreatmentResponse.REMISSION
                )
            )
            assertForAllFunctions(EvaluationResult.PASS, TreatmentTestFactory.withTreatmentHistory(history))
        }
    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response mixed response`() {
        listOf(CORRECT_TREATMENT, OTHER_CORRECT_TREATMENT).forEach {
            val history = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(it),
                    bestResponse = TreatmentResponse.MIXED
                )
            )
            assertForAllFunctions(EvaluationResult.WARN, TreatmentTestFactory.withTreatmentHistory(history))
        }
    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response stable disease`() {
        listOf(CORRECT_TREATMENT, OTHER_CORRECT_TREATMENT).forEach {
            val history = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(it),
                    bestResponse = TreatmentResponse.STABLE_DISEASE
                )
            )
            assertForAllFunctions(EvaluationResult.WARN, TreatmentTestFactory.withTreatmentHistory(history))
        }
    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains target therapy but no response specified`() {
        listOf(CORRECT_TREATMENT, OTHER_CORRECT_TREATMENT).forEach {
            val record = TreatmentTestFactory.withTreatmentHistory(
                listOf(
                    TreatmentTestFactory.treatmentHistoryEntry(
                        setOf(it),
                        bestResponse = null
                    )
                )
            )
            assertForAllFunctions(EvaluationResult.UNDETERMINED, record)
            assertThat(functionWithSpecificTreatments.evaluate(record).undeterminedMessages)
                .containsExactly("Undetermined objective benefit from treatment with ${it.display()}")
        }
    }

    @Test
    fun `Should only include treatment(s) present in history in message when outcome is pass or undetermined`() {
        val (pass, undetermined) = listOf(TreatmentResponse.COMPLETE_RESPONSE, null).map {
            TreatmentTestFactory.withTreatmentHistory(
                listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(OTHER_CORRECT_TREATMENT), bestResponse = it))
            )
        }
        val messageEnding = "objective benefit from treatment with ${OTHER_CORRECT_TREATMENT.display()}"
        assertThat(functionWithSpecificTreatments.evaluate(pass).passMessages).containsExactly("Has had $messageEnding")
        assertThat(functionWithSpecificTreatments.evaluate(undetermined).undeterminedMessages).containsExactly("Undetermined $messageEnding")
    }

    @Test
    fun `Should include all target treatments in message if none are present in history`() {
        assertThat(functionWithSpecificTreatments.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())).failMessages)
            .containsExactly("Has not received treatment with ${CORRECT_TREATMENT.display()} or ${OTHER_CORRECT_TREATMENT.display()}")
    }

    @Test
    fun `Should only include the treatment corresponding to the evaluation in the message when multiple target treatments present in history`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(setOf(OTHER_CORRECT_TREATMENT), bestResponse = null),
                TreatmentTestFactory.treatmentHistoryEntry(setOf(CORRECT_TREATMENT), bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
            )
        )
        assertThat(functionWithSpecificTreatments.evaluate(record).undeterminedMessages)
            .containsExactly("Undetermined objective benefit from treatment with ${OTHER_CORRECT_TREATMENT.display()}")
    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains not exact target therapy but similar drug with best response other than PD`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(SIMILAR_DRUG_TO_TARGET_TREATMENT),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithSpecificTreatments.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains similar drug but other type than target drug`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(DRUG_WITH_SAME_CATEGORY_BUT_OTHER_TYPE),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithSpecificTreatments.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains not exact target therapy but similar drug but best response PD`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(SIMILAR_DRUG_TO_TARGET_TREATMENT),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithSpecificTreatments.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains target therapy but best response progressive disease`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_TREATMENT),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            )
        )
        assertForAllFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(history))
    }

    private fun assertForAllFunctions(result: EvaluationResult, record: PatientRecord) {
        assertEvaluation(result, functionWithSpecificTreatments.evaluate(record))
        assertEvaluation(result, functionWithSpecificCategory.evaluate(record))
        assertEvaluation(result, functionWithSpecificCategoryAndType.evaluate(record))
    }
}