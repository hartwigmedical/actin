package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AnyGeneFromSetIsNotExpressedTest {

    val function = AnyGeneFromSetIsNotExpressed(setOf("gene a", "gene b", "gene c"))

    @Test
    fun `Should evaluate to undetermined with correct message`() {
        val evaluation = function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).contains("Non-expression of gene a, gene b and gene c in RNA undetermined")
    }
}