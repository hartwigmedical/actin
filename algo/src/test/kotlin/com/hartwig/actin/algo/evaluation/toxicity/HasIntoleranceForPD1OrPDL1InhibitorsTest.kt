package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.TestPriorOtherConditionFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

private const val DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM = "0060051"

class HasIntoleranceForPD1OrPDL1InhibitorsTest {
    private val function = HasIntoleranceForPD1OrPDL1Inhibitors(
        TestDoidModelFactory.createWithOneParentChild(
            DoidConstants.AUTOIMMUNE_DISEASE_DOID, DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM
        )
    )

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
    fun `Should warn when patient has prior condition belonging to autoimmune disease doid`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(patient(emptyList(), DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM)))
    }

    @Test
    fun `Should fail when patient has no matching intolerance or autoimmune disease condition`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patient(listOf(ToxicityTestFactory.intolerance(name = "other")), "123"))
        )
    }

    private fun patient(intolerances: List<Intolerance>, priorConditionDoid: String): PatientRecord {
        val priorCondition = TestPriorOtherConditionFactory.createMinimal()
            .copy(doids = setOf(priorConditionDoid), isContraindicationForTherapy = true)
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            intolerances = intolerances,
            priorOtherConditions = listOf(priorCondition)
        )
    }
}