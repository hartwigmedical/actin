package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasSpecificInfectionTest {
    private val targetCodes = setOf(IcdConstants.ACUTE_HEPATITIS_B_CODE, IcdConstants.CHRONIC_HEPATITIS_B_CODE).map { IcdCode(it) }.toSet()
    private val function = HasSpecificInfection(TestIcdFactory.createTestModel(), targetCodes, "hepatitis B virus")

    @Test
    fun `Should fail with no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with prior conditions but wrong ICD code`() {
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.CYTOMEGALOVIRAL_DISEASE_CODE)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withOtherCondition(condition))
        )
    }

    @Test
    fun `Should evaluate to undetermined with infection matching main code but unknown extension code`() {
        val function = HasSpecificInfection(
            TestIcdFactory.createTestModel(),
            setOf(IcdCode(IcdConstants.ACUTE_HEPATITIS_B_CODE, "extension")),
            "hepatitis B virus"
        )
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.ACUTE_HEPATITIS_B_CODE)
        val evaluation = function.evaluate(ComorbidityTestFactory.withOtherCondition(condition))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to undetermined with active infection but no description in infectionStatus`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord()
            .copy(clinicalStatus = ClinicalStatus(infectionStatus = InfectionStatus(true, null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should pass for prior condition with correct ICD code`() {
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = targetCodes.first().mainCode)
        val evaluation = function.evaluate(ComorbidityTestFactory.withOtherCondition(condition))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessagesStrings()).containsExactly("Hepatitis B virus infection in history")
    }

    @Test
    fun `Should pass with active infection and matching description in infectionStatus`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord()
            .copy(clinicalStatus = ClinicalStatus(infectionStatus = InfectionStatus(true, "hepatitis B")))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }
}