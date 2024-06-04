package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ARCHER_ALWAYS_TESTED_GENES
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GENERIC_PANEL_ALWAYS_TESTED_GENES
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

class PriorMolecularTestInterpreter {
    private val logger = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()

    fun interpret(history: MolecularHistory): List<PriorMolecularTestInterpretation> {
        history.allIHCTests().forEach(::interpret)
        history.allArcherPanels().forEach(::interpretArcher)
        history.allGenericPanels().forEach(::interpretGeneric)
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

    private fun interpretArcher(test: PanelRecord) {
        val variants = test.events().filterIsInstance<ArcherVariantExtraction>()
        val fusions = test.events().filterIsInstance<ArcherFusionExtraction>()
        variants
            .forEach { interpretationBuilder.addInterpretation(ExperimentType.ARCHER.display(), it.gene, it.hgvsCodingImpact) }
        fusions
            .forEach { interpretationBuilder.addInterpretation(ExperimentType.ARCHER.display(), it.gene, it.display()) }

        interpretImpliedNegatives(
            ExperimentType.ARCHER,
            ARCHER_ALWAYS_TESTED_GENES - (variants.map { it.gene }.toSet() + fusions.map { it.gene }.toSet())
        )
    }

    private fun interpretGeneric(test: PanelRecord) {
        interpretImpliedNegatives(
            ExperimentType.GENERIC_PANEL,
            GENERIC_PANEL_ALWAYS_TESTED_GENES - (test.testedGenes().filter { test.events().any { e -> e.impactsGene(it) } }).toSet()
        )
    }

    private fun interpret(test: OtherPriorMolecularTest) {
        val scoreText = test.test.scoreText
        val item = test.test.item
        if (scoreText != null && item != null) {
            interpretationBuilder.addInterpretation(test.type.display(), scoreText, item)
        }
    }

    private fun interpretImpliedNegatives(type: ExperimentType, negatives: Set<String> = emptySet()) {
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