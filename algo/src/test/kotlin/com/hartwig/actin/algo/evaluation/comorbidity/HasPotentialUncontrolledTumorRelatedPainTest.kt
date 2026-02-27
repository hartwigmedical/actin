package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.junit.Test

private val OPIOIDS_ATC_LEVEL = AtcLevel("N02A", "Opioids")
private val PAIN_MEDICATION =
    TestMedicationFactory.createMinimal().copy(atc = AtcTestFactory.atcClassification().copy(pharmacologicalSubGroup = OPIOIDS_ATC_LEVEL))

class HasPotentialUncontrolledTumorRelatedPainTest {

    private val targetCode = IcdConstants.CHRONIC_CANCER_RELATED_PAIN_CODE
    private val otherTargetCode = IcdConstants.ACUTE_PAIN_CODE
    private val targetNode = IcdNode(targetCode, emptyList(), "Cancer-related pain")
    private val childOfTargetNode = IcdNode("childCode", listOf(targetCode), "Child of cancer-related pain")
    private val icdModel = IcdModel.create(listOf(targetNode, childOfTargetNode))
    private val alwaysActiveFunction =
        HasPotentialUncontrolledTumorRelatedPain(MedicationTestFactory.alwaysActive(), setOf(OPIOIDS_ATC_LEVEL), icdModel)
    private val alwaysPlannedFunction =
        HasPotentialUncontrolledTumorRelatedPain(MedicationTestFactory.alwaysPlanned(), setOf(OPIOIDS_ATC_LEVEL), icdModel)

    @Test
    fun `Should warn if patient uses severe pain medication`() {
        assertEvaluation(
            EvaluationResult.WARN,
            alwaysActiveFunction.evaluate(ComorbidityTestFactory.withMedication(PAIN_MEDICATION))
        )
    }

    @Test
    fun `Should warn if patient has planned severe pain medication`() {
        assertEvaluation(
            EvaluationResult.WARN,
            alwaysPlannedFunction.evaluate(ComorbidityTestFactory.withMedication(PAIN_MEDICATION))
        )
    }

    @Test
    fun `Should evaluate to undetermined on other condition with direct or parent match on target icd code`() {
        listOf(targetNode.code, childOfTargetNode.code, otherTargetCode)
            .map { ComorbidityTestFactory.withOtherCondition(ComorbidityTestFactory.otherCondition(icdMainCode = it)) }
            .forEach { assertEvaluation(EvaluationResult.UNDETERMINED, alwaysActiveFunction.evaluate(it)) }
    }


    @Test
    fun `Should fail if patient has no relevant conditions and uses no pain medication`() {
        val noPainMedication = TestMedicationFactory.createMinimal().copy(name = "just some medication")
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(ComorbidityTestFactory.withMedication(noPainMedication)))
    }
}
