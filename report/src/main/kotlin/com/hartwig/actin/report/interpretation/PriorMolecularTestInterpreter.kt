package com.hartwig.actin.report.interpretation

import com.hartwig.actin.Displayable
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ARCHER_ALWAYS_TESTED_GENES
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

private const val VARIANT_GROUPING = "Variants"
private const val FUSIONS_GROUPING = "Fusions"
private const val EXON_DELETION_GROUPING = "Exon deletions"

class PriorMolecularTestInterpreter {
    private val logger = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()

    fun interpret(history: MolecularHistory): List<PriorMolecularTestInterpretation> {
        history.allIHCTests().forEach(::interpret)
        history.allArcherPanels().forEach(::interpret)
        history.allGenericPanels().forEach(::interpret)
        history.allOtherTests().forEach(::interpret)
        return interpretationBuilder.build()
    }

    private fun interpret(test: PriorMolecularTest) {
        val item = test.item ?: ""
        val type = test.test
        val scoreText = test.scoreText
        val scoreValue = test.scoreValue
        when {
            scoreText != null -> interpretationBuilder.addInterpretation(type, scoreText, item, 0)
            scoreValue != null -> interpretationBuilder.addInterpretation(type, item, formatValueBasedPriorTest(test), 1)
            else -> logger.error("IHC test is neither text-based nor value-based: {}", test)
        }
    }

    private fun interpret(test: ArcherPanelExtraction) {
        test.variants.forEach { interpretationBuilder.addInterpretation(ExperimentType.ARCHER.display(), VARIANT_GROUPING, it.display()) }
        test.fusions.forEach { interpretationBuilder.addInterpretation(ExperimentType.ARCHER.display(), FUSIONS_GROUPING, it.display()) }

        interpretNegatives(
            ExperimentType.ARCHER,
            ARCHER_ALWAYS_TESTED_GENES - (test.genesWithVariants() + test.genesWithFusions())
        )
    }

    private fun interpret(test: GenericPanelExtraction) {
        test.variants.forEach { interpretationBuilder.addInterpretation(test.panelType.display(), VARIANT_GROUPING, it.display()) }
        test.fusions.forEach {
            interpretationBuilder.addInterpretation(
                test.panelType.display(),
                FUSIONS_GROUPING,
                it.display()
            )
        }
        test.exonDeletions.forEach {
            interpretationBuilder.addInterpretation(
                test.panelType.display(),
                EXON_DELETION_GROUPING,
                it.display()
            )
        }
        interpretNegatives(
            test.panelType,
            test.testedGenes() - test.genesHavingResultsInPanel()
        )
    }

    private fun interpret(test: OtherPriorMolecularTest) {
        val scoreText = test.test.scoreText
        val item = test.test.item
        if (scoreText != null && item != null) {
            interpretationBuilder.addInterpretation(test.type.display(), scoreText, item)
        }
    }

    private fun interpretNegatives(type: Displayable, negatives: Set<String> = emptySet()) {
        negatives.forEach { interpretationBuilder.addInterpretation(type.display(), "Negative", it) }
    }

    private fun formatValueBasedPriorTest(valueTest: PriorMolecularTest): String {
        return valueTest.scoreValue?.let {
            return listOfNotNull(
                "Score", valueTest.measure, valueTest.scoreValuePrefix, Formats.twoDigitNumber(it) + valueTest.scoreValueUnit
            ).joinToString(" ")
        } ?: ""
    }
}