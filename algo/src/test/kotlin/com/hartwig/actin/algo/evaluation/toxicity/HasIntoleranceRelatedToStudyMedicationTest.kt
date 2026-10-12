package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.junit.jupiter.api.Test

class HasIntoleranceRelatedToStudyMedicationTest {

    private val icdModel = IcdModel.create(
        listOf(
            IcdNode(
                IcdConstants.NIVOLUMAB_CODE,
                listOf(IcdConstants.ANTI_NEOPLASTIC_AGENTS, IcdConstants.DRUG_ALLERGY_CODE),
                "Nivolumab"
            )
        )
    )
    private val function = HasIntoleranceRelatedToStudyMedication(icdModel)
    private val matchingIcdCodes = IcdConstants.DRUG_ALLERGY_SET

    @Test
    fun `Should fail when no comorbidities in history`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList())),
            "Has no intolerances to study medication"
        )
    }

    @Test
    fun `Should fail when intolerance has wrong ICD code`() {
        val intolerance = ComorbidityTestFactory.intolerance(icdMainCode = "wrong")
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)),
            "Has no intolerances to study medication"
        )
    }

    @Test
    fun `Should fail when toxicity with matching ICD code has grade below 2 or null`() {
        listOf(1, null).flatMap { grade ->
            listOf(
                ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, grade, matchingIcdCodes.first(), IcdConstants.NIVOLUMAB_CODE),
                ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, grade, IcdConstants.DRUG_ALLERGY_CODE)
            )
        }.forEach { match ->
            assertEvaluation(
                EvaluationResult.FAIL,
                function.evaluate(ComorbidityTestFactory.withComorbidity(match)),
                "Has no intolerances to study medication"
            )
        }
    }

    @Test
    fun `Should fail when intolerance has generic hypersensitivity ICD code without extension`() {
        val intolerance = ComorbidityTestFactory.intolerance(icdMainCode = IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_BLOCK)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)),
            "Has no intolerances to study medication"
        )
    }

    @Test
    fun `Should fail when allergy is not to anti-cancer agent`() {
        val intolerance = ComorbidityTestFactory.intolerance(
            icdMainCode = matchingIcdCodes.first(),
            icdExtensionCode = IcdConstants.CEFAMYCIN_ANTIBIOTIC
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)),
            "Has no intolerances to study medication"
        )
    }

    @Test
    fun `Should evaluate to undetermined when intolerance has matching ICD code`() {
        val intolerance = ComorbidityTestFactory.intolerance(
            name = "intolerance",
            icdMainCode = matchingIcdCodes.first(),
            icdExtensionCode = IcdConstants.NIVOLUMAB_CODE
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)),
            "Has medication-related allergies (${intolerance.name}) - undetermined if allergy to study medication"
        )
    }

    @Test
    fun `Should evaluate to undetermined when any other comorbidity has matching ICD code`() {
        val icdMainCode = matchingIcdCodes.first()
        listOf(
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, 2, icdMainCode, IcdConstants.NIVOLUMAB_CODE),
            ComorbidityTestFactory.otherCondition("condition", icdMainCode = icdMainCode, icdExtensionCode = IcdConstants.NIVOLUMAB_CODE)
        ).forEach { match ->
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function.evaluate(ComorbidityTestFactory.withComorbidity(match)),
                "Has medication-related allergies (${match.name}) - undetermined if allergy to study medication"
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when any comorbidity has a drug specific ICD code without extension`() {
        val icdMainCode = IcdConstants.DRUG_ALLERGY_CODE
        listOf(
            ComorbidityTestFactory.intolerance("drug allergy", icdMainCode = icdMainCode),
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, 2, icdMainCode),
            ComorbidityTestFactory.otherCondition("condition", icdMainCode = icdMainCode)
        ).forEach { match ->
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function.evaluate(ComorbidityTestFactory.withComorbidity(match)),
                "Has drug allergies (${match.name}) - undetermined if allergy to study medication (drug type unknown)"
            )
        }
    }
}
