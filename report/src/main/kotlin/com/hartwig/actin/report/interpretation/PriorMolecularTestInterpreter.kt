package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.apache.logging.log4j.LogManager

object PriorMolecularTestInterpreter {
    private val LOGGER = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)

    fun interpret(priorTests: List<PriorMolecularTest>): PriorMolecularTestInterpretation {
        val (textBasedPriorTests, valueBasedPriorTests) = priorTests.map { priorTest ->
            val scoreText = priorTest.scoreText()
            if (scoreText != null) {
                PriorMolecularTestCollection(
                    listOf(PriorMolecularTestKey(test = priorTest.test(), scoreText = scoreText) to priorTest),
                    emptySet()
                )
            } else if (priorTest.scoreValue() != null) {
                PriorMolecularTestCollection(emptyList(), setOf(priorTest))
            } else {
                LOGGER.warn("Prior test is neither text-based nor value-based: {}", priorTest)
                PriorMolecularTestCollection()
            }
        }.fold(PriorMolecularTestCollection(), PriorMolecularTestCollection::combine)
        return PriorMolecularTestInterpretation(textBasedPriorTests.groupBy({ it.first }, { it.second }), valueBasedPriorTests)
    }

    private data class PriorMolecularTestCollection(
        val textBasedPriorMolecularTests: List<Pair<PriorMolecularTestKey, PriorMolecularTest>> = emptyList(),
        val valueBasedPriorTests: Set<PriorMolecularTest> = emptySet()
    ) {

        fun combine(other: PriorMolecularTestCollection): PriorMolecularTestCollection {
            return PriorMolecularTestCollection(
                textBasedPriorMolecularTests + other.textBasedPriorMolecularTests,
                valueBasedPriorTests + other.valueBasedPriorTests
            )
        }
    }
}