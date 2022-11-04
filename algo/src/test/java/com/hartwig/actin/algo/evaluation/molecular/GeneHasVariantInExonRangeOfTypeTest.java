package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;

import org.junit.Test;

public class GeneHasVariantInExonRangeOfTypeTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantInExonRangeOfType function = new GeneHasVariantInExonRangeOfType("gene A", 1, 2, VariantTypeInput.INSERT);

    }
}