package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class HasLongQTSyndromeTest {
    @Test
    fun canEvaluate() {
        val function = HasLongQTSyndrome(TestDoidModelFactory.createMinimalTestDoidModel())

        // No conditions
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(conditions)))

        // A different condition
        conditions.add(builder().addDoids("wrong doid").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(conditions)))

        // The correct condition
        conditions.add(builder().addDoids(DoidConstants.LONG_QT_SYNDROME_DOID).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorOtherConditions(conditions)))
    }

    companion object {
        private fun builder(): ImmutablePriorOtherCondition.Builder {
            return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(true)
        }

        private fun withPriorOtherConditions(conditions: List<PriorOtherCondition>): PatientRecord {
            return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(
                    ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .priorOtherConditions(conditions)
                        .build()
                )
                .build()
        }
    }
}