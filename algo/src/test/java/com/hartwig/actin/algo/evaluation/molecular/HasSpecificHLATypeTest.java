package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.TestHlaAlleleFactory;

import org.junit.Test;

public class HasSpecificHLATypeTest {

    @Test
    public void canEvaluate() {
        HlaAllele correct = ImmutableHlaAllele.builder().name("A*02:01").tumorCopyNumber(1D).hasSomaticMutations(false).build();
        HasSpecificHLAType function = new HasSpecificHLAType(correct.name());

        assertMolecularEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withUnreliableMolecularImmunology()));

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHlaAllele(correct)));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHlaAllele(TestHlaAlleleFactory.builder()
                        .from(correct)
                        .tumorCopyNumber(0D)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHlaAllele(TestHlaAlleleFactory.builder()
                        .from(correct)
                        .hasSomaticMutations(true)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withHlaAllele(TestHlaAlleleFactory.builder().from(correct).name("other").build())));
    }
}