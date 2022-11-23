package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class PriorMolecularTestInterpreterTest {

    @Test
    public void canInterpretPriorMolecularTests() {
        PriorMolecularTest textBased1 = create("text 1", null, "test 1", "item 1");
        PriorMolecularTest textBased2 = create("text 1", null, "test 1", "item 2");
        PriorMolecularTest textBased3 = create("text 1", null, "test 2", "item 3");
        PriorMolecularTest valueBased1 = create(null, 1D, "test 1", "item 4");
        PriorMolecularTest valueBased2 = create(null, 2D, "test 2", "item 5");
        PriorMolecularTest invalid = create(null, null, "invalid", "invalid");

        List<PriorMolecularTest> tests = Lists.newArrayList(textBased1, textBased2, textBased3, valueBased1, valueBased2, invalid);

        PriorMolecularTestInterpretation interpretation = PriorMolecularTestInterpreter.interpret(tests);

        assertEquals(2, interpretation.textBasedPriorTests().keySet().size());
        assertEquals(3, interpretation.textBasedPriorTests().values().size());
        assertEquals(2, interpretation.valueBasedPriorTests().size());
    }

    @NotNull
    private static PriorMolecularTest create(@Nullable String scoreText, @Nullable Double scoreValue, @NotNull String test,
            @NotNull String item) {
        return ImmutablePriorMolecularTest.builder()
                .test(test)
                .item(item)
                .scoreText(scoreText)
                .scoreValue(scoreValue)
                .impliesPotentialIndeterminateStatus(false)
                .build();
    }

}