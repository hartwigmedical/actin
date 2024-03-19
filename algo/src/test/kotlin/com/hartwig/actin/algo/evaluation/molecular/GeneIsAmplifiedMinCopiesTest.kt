package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import org.junit.Test

class GeneIsAmplifiedMinCopiesTest {
    private val function = GeneIsAmplifiedMinCopies("gene A", 5)

    private val passingAmp = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isReportable = true,
        type = CopyNumberType.FULL_GAIN,
        minCopies = 40,
        maxCopies = 40
    )

    @Test
    fun canEvaluate() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord()))

        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(null, passingAmp))
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(geneRole = GeneRole.TSG)))
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION))
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(isReportable = false)))
        )

        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp))
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(minCopies = 8, maxCopies = 8)))
        )

        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(minCopies = 4, maxCopies = 4)))
        )

        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(minCopies = 3)))
        )
    }
}