package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.TestPriorOtherConditionFactory
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

private const val DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM = "0060051"
private val MATCHING_ICD_CODE = IcdConstants.AUTOIMMUNE_DISEASE_SET.first()

class HasIntoleranceForPD1OrPDL1InhibitorsTest {
    private val function = HasIntoleranceForPD1OrPDL1Inhibitors(TestIcdFactory.createTestModel())

    @Test
    fun `Should pass when patient has intolerance matching list`() {
        HasIntoleranceForPD1OrPDL1Inhibitors.INTOLERANCE_TERMS.forEach { term: String ->
            val record = patient(
                listOf(ToxicityTestFactory.intolerance(name = "intolerance to " + term.uppercase())),
                DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM
            )
            assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
        }
    }

    @Test
    fun `Should warn when patient has prior condition belonging to autoimmune disease ICD code`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(patient(emptyList(), MATCHING_ICD_CODE)))
    }

    @Test
    fun `Should fail when patient has matching condition but no contraindication for therapy`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patient(listOf(ToxicityTestFactory.intolerance(name = "other")), MATCHING_ICD_CODE, isContraIndication = false))
        )
    }

    @Test
    fun `Should fail when patient has no matching intolerance or autoimmune disease condition`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patient(listOf(ToxicityTestFactory.intolerance(name = "other")), "123"))
        )
    }

    private fun patient(intolerances: List<Intolerance>, icdMainCode: String, isContraIndication: Boolean = true): PatientRecord {
        val priorCondition = TestPriorOtherConditionFactory.createMinimal()
            .copy(icdCode = IcdCode(icdMainCode), isContraindicationForTherapy = isContraIndication)
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            intolerances = intolerances,
            priorOtherConditions = listOf(priorCondition)
        )
    }
}