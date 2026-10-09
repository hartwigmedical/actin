package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasSecondaryGlioblastomaTest {

    private val function =
        HasSecondaryGlioblastoma(TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.GLIOBLASTOMA_DOID, "glioblastoma"))

    @Test
    fun canEvaluate() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(null)),
            "Secondary glioblastoma undetermined (tumor location unknown)"
        )
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.GLIOBLASTOMA_DOID)),
            "Unclear if glioblastoma is considered secondary glioblastoma"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids("arbitrary doid")),
            "No (secondary) glioblastoma"
        )
    }
}
