package com.hartwig.actin.serve.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.junit.Test;

public class ServeExtractionTest {

    @Test
    public void canExtractGenes() {
        EligibilityFunction geneFunction = ImmutableEligibilityFunction.builder()
                .rule(ServeExtraction.RULES_WITH_GENE_AS_FIRST_PARAM.iterator().next())
                .addParameters("gene")
                .build();

        assertEquals("gene", ServeExtraction.gene(geneFunction));

        EligibilityFunction signature = ImmutableEligibilityFunction.builder().rule(EligibilityRule.HRD_SIGNATURE).build();

        assertNull(ServeExtraction.gene(signature));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnGeneExtractionOfNonMolecularFunction() {
        EligibilityFunction nonMolecularFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build();
        ServeExtraction.gene(nonMolecularFunction);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnInvalidFunctionForGeneExtraction() {
        EligibilityFunction invalidFunction =
                ImmutableEligibilityFunction.builder().rule(ServeExtraction.RULES_WITH_GENE_AS_FIRST_PARAM.iterator().next()).build();
        ServeExtraction.gene(invalidFunction);
    }

    @Test
    public void canExtractMutations() {
        EligibilityFunction mutationFunction = ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y)
                .parameters(Lists.newArrayList("gene", "mutation"))
                .build();
        assertEquals("mutation", ServeExtraction.mutation(mutationFunction));

        EligibilityFunction fusionFunction = ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.SPECIFIC_FUSION_OF_X_TO_Y)
                .parameters(Lists.newArrayList("gene1", "gene2"))
                .build();
        assertEquals("gene1-gene2", ServeExtraction.mutation(fusionFunction));

        EligibilityFunction msiFunction = ImmutableEligibilityFunction.builder().rule(EligibilityRule.MSI_SIGNATURE).build();
        assertEquals("MSI high", ServeExtraction.mutation(msiFunction));

        EligibilityFunction hrdFunction = ImmutableEligibilityFunction.builder().rule(EligibilityRule.HRD_SIGNATURE).build();
        assertEquals("HRD pos", ServeExtraction.mutation(hrdFunction));

        EligibilityFunction tmbHighFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.TMB_OF_AT_LEAST_X).addParameters("20").build();
        assertEquals("TMB >= 20", ServeExtraction.mutation(tmbHighFunction));

        EligibilityFunction tmlHighFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.TML_OF_AT_LEAST_X).addParameters("400").build();
        assertEquals("TML >= 400", ServeExtraction.mutation(tmlHighFunction));

        EligibilityFunction tmlLowFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.TML_OF_AT_MOST_X).addParameters("200").build();
        assertEquals("TML <= 200", ServeExtraction.mutation(tmlLowFunction));

        EligibilityFunction otherFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X).build();
        assertNull(ServeExtraction.mutation(otherFunction));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnMutationExtractionOfNonMolecularFunction() {
        EligibilityFunction nonMolecularFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build();
        ServeExtraction.mutation(nonMolecularFunction);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnInvalidMutationForMutationExtraction() {
        EligibilityFunction invalidFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y).build();
        ServeExtraction.mutation(invalidFunction);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnInvalidFusionForMutationExtraction() {
        EligibilityFunction invalidFunction =
                ImmutableEligibilityFunction.builder().rule(EligibilityRule.SPECIFIC_FUSION_OF_X_TO_Y).build();
        ServeExtraction.mutation(invalidFunction);
    }
}