package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSpecificHLATypeTest {

    @Test
    public void canEvaluate() {
        String correct = "A*02:01";
        HasSpecificHLAType function = new HasSpecificHLAType(correct);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularImmunology(create(false))));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MolecularTestFactory.withMolecularImmunology(create(false, correct))));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withMolecularImmunology(create(true, correct))));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withMolecularImmunology(create(true, "other"))));
    }

    @NotNull
    private static MolecularImmunology create(boolean isReliable, @NotNull String allele) {
        HlaAllele hlaAllele = ImmutableHlaAllele.builder().name(allele).tumorCopyNumber(0D).hasSomaticMutations(false).build();
        return create(isReliable, hlaAllele);
    }

    @NotNull
    private static MolecularImmunology create(boolean isReliable, @NotNull HlaAllele... alleles) {
        return ImmutableMolecularImmunology.builder().from(create(isReliable)).addHlaAlleles(alleles).build();
    }

    @NotNull
    private static MolecularImmunology create(boolean isReliable) {
        return ImmutableMolecularImmunology.builder().isReliable(isReliable).build();
    }
}