package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import org.junit.Test

class GeneIsAmplifiedTest {
    private val function = GeneIsAmplified("gene A")

    private val ploidy = 3.0
    private val passingAmp = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isReportable = true,
        type = CopyNumberType.FULL_GAIN,
        minCopies = ploidy.toInt().times(4),
        maxCopies = ploidy.toInt().times(6)
    )

    @Test
    fun `Should return undetermined when molecular record is empty`(){
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord()))
    }

    @Test
    fun `Should fail when molecular record does not contain information on ploidy`(){
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(null, passingAmp)))
    }

    @Test
    fun `Should pass when molecular record contains a reportable amplification with copy number more than 3 times ploidy`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(ploidy, passingAmp)))
    }

    @Test
    fun `Should warn for reportable amplification with copy number above boundary but gene role TSG`(){
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(geneRole = GeneRole.TSG)))
        )
    }

    @Test
    fun `Should warn for reportable amplification with copy number above boundary but protein effect loss of function`(){
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION))
            )
        )
    }

    @Test
    fun `Should warn for unreportable amplification with copy number above boundary`(){
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3.0, passingAmp.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should fail when min and max copy number under boundary for amplification`(){
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(
                3.0, passingAmp.copy(minCopies = ploidy.toInt(), maxCopies = ploidy.toInt()))
            )
        )
    }

    @Test
    fun `Should warn when min copy number under and max copy number above boundary for amplification`(){
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    3.0, passingAmp.copy(minCopies = ploidy.toInt(), maxCopies = ploidy.toInt().times(4))
                )
            )
        )
    }
}