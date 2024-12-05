package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.junit.Test

class HasPotentialUncontrolledTumorRelatedPainTest {

    private val targetCode = IcdConstants.CHRONIC_CANCER_RELATED_PAIN_ICD
    private val otherTargetCode = IcdConstants.ACUTE_PAIN_ICD
    private val targetNode = IcdNode(targetCode, emptyList(), "Cancer-related pain")
    private val childOfTargetNode = IcdNode("childCode", listOf(targetCode), "Child of cancer-related pain")
    private val icdModel = IcdModel.create(listOf(targetNode, childOfTargetNode))

    @Test
    fun `Should evaluate to undetermined on complication or non oncological history entry with direct or parent match on target icd code`() {
        val function = HasPotentialUncontrolledTumorRelatedPain(medicationStatusInterpreter, icdModel)
        val (complicationWithDirectMatch, complicationWithParentMatch) =
            listOf(targetNode.code, childOfTargetNode.code).map { ComplicationTestFactory.complication(icdCode = it) }
        val (historyWithDirectMatch, historyWithParentMatch) =
            listOf(targetNode.code, childOfTargetNode.code).map { OtherConditionTestFactory.priorOtherCondition(icdCode = it) }

        listOf(
            listOf(complicationWithDirectMatch, complicationWithParentMatch).map { ComplicationTestFactory.withComplication(it) },
            listOf(historyWithDirectMatch, historyWithParentMatch, OtherConditionTestFactory.priorOtherCondition(icdCode = otherTargetCode))
                .map { OtherConditionTestFactory.withPriorOtherCondition(it) }
        ).flatten().forEach { assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(it)) }
    }

    @Test
    fun `Should evaluate on medication`() {
        val function = HasPotentialUncontrolledTumorRelatedPain(medicationStatusInterpreter, icdModel)
        val wrong: Medication = TestMedicationFactory.createMinimal().copy(name = "just some medication")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withMedication(wrong)))
        val match: Medication =
            TestMedicationFactory.createMinimal().copy(name = HasPotentialUncontrolledTumorRelatedPain.SEVERE_PAIN_MEDICATION)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withMedication(match)))
    }

    companion object {
        private val medicationStatusInterpreter = object : MedicationStatusInterpreter {
            override fun interpret(medication: Medication): MedicationStatusInterpretation {
                return MedicationStatusInterpretation.ACTIVE
            }
        }
    }
}