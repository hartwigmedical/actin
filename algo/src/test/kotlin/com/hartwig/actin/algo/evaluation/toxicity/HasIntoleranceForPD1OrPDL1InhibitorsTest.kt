package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.TestPriorOtherConditionFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.util.ApplicationConfig
import org.junit.Test

class HasIntoleranceForPD1OrPDL1InhibitorsTest {
    @Test
    fun shouldPassWhenPatientHasIntoleranceMatchingList() {
        HasIntoleranceForPD1OrPDL1Inhibitors.INTOLERANCE_TERMS.forEach { term: String ->
            val record = patient(
                listOf(
                    ToxicityTestFactory.intolerance()
                        .name("intolerance to " + term.uppercase(ApplicationConfig.LOCALE))
                        .build()
                ), DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM
            )
            assertEvaluation(EvaluationResult.PASS, function().evaluate(record))
        }
    }

    @Test
    fun shouldWarnWhenPatientHasPriorConditionBelongingToAutoimmuneDiseaseDoid() {
        assertEvaluation(EvaluationResult.WARN, function().evaluate(patient(emptyList(), DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM)))
    }

    @Test
    fun shouldFailWhenPatientHasNoMatchingIntoleranceOrAutoimmuneDiseaseCondition() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(patient(listOf(ToxicityTestFactory.intolerance().name("other").build()), "123"))
        )
    }

    companion object {
        private const val DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM: String = "0060051"

        private fun function(): HasIntoleranceForPD1OrPDL1Inhibitors {
            return HasIntoleranceForPD1OrPDL1Inhibitors(
                TestDoidModelFactory.createWithOneParentChild(
                    DoidConstants.AUTOIMMUNE_DISEASE_DOID,
                    DOID_AUTOIMMUNE_DISEASE_OF_CARDIOVASCULAR_SYSTEM
                )
            )
        }

        private fun patient(intolerances: Iterable<Intolerance>, priorConditionDoid: String): PatientRecord {
            val minimalPatient = TestDataFactory.createMinimalTestPatientRecord()
            val priorCondition: PriorOtherCondition? =
                TestPriorOtherConditionFactory.builder().addDoids(priorConditionDoid).isContraindicationForTherapy(true).build()
            return ImmutablePatientRecord.copyOf(minimalPatient)
                .withClinical(
                    ImmutableClinicalRecord.copyOf(minimalPatient.clinical())
                        .withIntolerances(intolerances)
                        .withPriorOtherConditions(priorCondition)
                )
        }
    }
}