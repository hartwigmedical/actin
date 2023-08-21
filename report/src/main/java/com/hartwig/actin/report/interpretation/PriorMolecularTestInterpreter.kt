package com.hartwig.actin.report.interpretation;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class PriorMolecularTestInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(PriorMolecularTestInterpreter.class);

    private PriorMolecularTestInterpreter() {
    }

    @NotNull
    public static PriorMolecularTestInterpretation interpret(@NotNull List<PriorMolecularTest> priorTests) {
        ImmutablePriorMolecularTestInterpretation.Builder builder = ImmutablePriorMolecularTestInterpretation.builder();
        for (PriorMolecularTest priorTest : priorTests) {
            String scoreText = priorTest.scoreText();
            Double scoreValue = priorTest.scoreValue();
            if (scoreText != null) {
                PriorMolecularTestKey key = ImmutablePriorMolecularTestKey.builder().test(priorTest.test()).scoreText(scoreText).build();
                builder.putTextBasedPriorTests(key, priorTest);
            } else if (scoreValue != null) {
                builder.addValueBasedPriorTests(priorTest);
            } else {
                LOGGER.warn("Prior test is neither text-based nor value-based: {}", priorTest);
            }
        }
        return builder.build();
    }
}
