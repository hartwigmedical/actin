package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AnyGeneFromSetIsNotExpressedTest {

    @Test
    fun `Should evaluate to undetermined with correct message`() {
        val function = AnyGeneFromSetIsNotExpressed(LocalDate.of(2024, 11, 6), setOf("gene a", "gene b", "gene c"))
        val evaluation = function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages)
            .contains("Non-expression of gene a, gene b and gene c in RNA undetermined")
    }
}