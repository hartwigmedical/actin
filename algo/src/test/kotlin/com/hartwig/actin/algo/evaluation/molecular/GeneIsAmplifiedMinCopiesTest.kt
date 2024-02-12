package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import org.junit.Test
import kotlin.math.E

class GeneIsAmplifiedMinCopiesTest {
    private val functionAmp = GeneIsAmplifiedMinCopies("gene A", 5)

    private val passingAmp = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isReportable = true,
        type = CopyNumberType.FULL_GAIN,
        minCopies = 40,
        maxCopies = 40
    )

    private val functionHighCopyNumber = GeneIsAmplifiedMinCopies("gene B", 10)

    private val highGeneCopyNumber = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene B",
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isReportable = false,
        type = CopyNumberType.NONE,
        minCopies = 12,
        maxCopies = 12
    )
    
    @Test
    fun canEvaluate() {
        assertMolecularEvaluation(EvaluationResult.FAIL, functionAmp.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(EvaluationResult.FAIL, functionHighCopyNumber.evaluate(TestDataFactory.createMinimalTestPatientRecord()))

        assertMolecularEvaluation(EvaluationResult.FAIL, functionAmp.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(null, passingAmp)))
        assertMolecularEvaluation(EvaluationResult.FAIL, functionHighCopyNumber.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(null, highGeneCopyNumber)))

        assertMolecularEvaluation(EvaluationResult.PASS, functionAmp.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp)))
        assertMolecularEvaluation(EvaluationResult.PASS, functionHighCopyNumber.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, highGeneCopyNumber)))

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionAmp.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(geneRole = GeneRole.TSG)))
        )
        
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionAmp.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION))
            )
        )
        
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionAmp.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(isReportable = false)))
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionAmp.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(minCopies = 8, maxCopies = 8)))
        )

        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionAmp.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(minCopies = 4, maxCopies = 4)))
        )

        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionAmp.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(minCopies = 3)))
        )
    }
}