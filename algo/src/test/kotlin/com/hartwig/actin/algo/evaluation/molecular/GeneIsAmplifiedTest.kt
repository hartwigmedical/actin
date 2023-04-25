package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;

import org.junit.Test;

public class GeneIsAmplifiedTest {

    @Test
    public void canEvaluate() {
        GeneIsAmplified function = new GeneIsAmplified("gene A");

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        CopyNumber passingAmp = TestCopyNumberFactory.builder()
                .gene("gene A")
                .geneRole(GeneRole.ONCO)
                .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                .isReportable(true)
                .type(CopyNumberType.FULL_GAIN)
                .minCopies(40)
                .maxCopies(40)
                .build();

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(null, passingAmp)));

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3D, passingAmp)));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3D,
                        TestCopyNumberFactory.builder().from(passingAmp).geneRole(GeneRole.TSG).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3D,
                        TestCopyNumberFactory.builder().from(passingAmp).proteinEffect(ProteinEffect.LOSS_OF_FUNCTION).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3D,
                        TestCopyNumberFactory.builder().from(passingAmp).isReportable(false).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3D,
                        TestCopyNumberFactory.builder().from(passingAmp).minCopies(3).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3D,
                        TestCopyNumberFactory.builder().from(passingAmp).minCopies(8).maxCopies(8).build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withPloidyAndCopyNumber(3D,
                        TestCopyNumberFactory.builder().from(passingAmp).minCopies(4).maxCopies(4).build())));
    }
}