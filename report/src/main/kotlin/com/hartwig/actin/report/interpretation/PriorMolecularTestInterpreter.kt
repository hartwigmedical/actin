package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.apache.logging.log4j.LogManager

object PriorMolecularTestInterpreter {
    private val LOGGER = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)
    fun interpret(priorTests: List<PriorMolecularTest>): PriorMolecularTestInterpretation {
        val builder = ImmutablePriorMolecularTestInterpretation.builder()
        for (priorTest in priorTests) {
            val scoreText = priorTest.scoreText()
            val scoreValue = priorTest.scoreValue()
            if (scoreText != null) {
                val key: PriorMolecularTestKey =
                    ImmutablePriorMolecularTestKey.builder().test(priorTest.test()).scoreText(scoreText).build()
                builder.putTextBasedPriorTests(key, priorTest)
            } else if (scoreValue != null) {
                builder.addValueBasedPriorTests(priorTest)
            } else {
                LOGGER.warn("Prior test is neither text-based nor value-based: {}", priorTest)
            }
        }
        return builder.build()
    }
}