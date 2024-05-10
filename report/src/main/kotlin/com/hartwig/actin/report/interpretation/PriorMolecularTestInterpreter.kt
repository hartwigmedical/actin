package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

class PriorMolecularTestInterpreter {
    private val logger = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()


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

    private fun interpret(test: ArcherPanel) {
        test.variants.forEach { interpretationBuilder.addInterpretation(test.type.display(), it.gene, it.hgvsCodingImpact) }
        interpretImpliedNegatives(
            test.type,
            test,
            test.genesWithVariants() + test.genesWithFusions()
        )
    }

    private fun interpret(test: GenericPanel) {
        interpretImpliedNegatives(test.type, test)
    }

    private fun interpret(test: OtherPriorMolecularTest) {
        val scoreText = test.test.scoreText
        val item = test.test.item
        if (scoreText != null && item != null) {
            interpretationBuilder.addInterpretation(test.type.display(), scoreText, item)
        }
    }

    private fun interpretImpliedNegatives(type: ExperimentType, panel: Panel, tested: Set<String> = emptySet()) {
        (panel.testedGenes() - tested).forEach { interpretationBuilder.addInterpretation(type.display(), "Negative", it) }
    }

    private fun formatValueBasedPriorTest(valueTest: PriorMolecularTest): String {
        return valueTest.scoreValue?.let {
            return listOfNotNull(
                "Score", valueTest.measure, valueTest.scoreValuePrefix, Formats.twoDigitNumber(it) + valueTest.scoreValueUnit
            ).joinToString(" ")
        } ?: ""
    }

    fun interpret(history: MolecularHistory): List<PriorMolecularTestInterpretation> {
        history.allIHCTests().forEach { interpret(it) }
        history.allArcherPanels().forEach { interpret(it) }
        history.allGenericPanels().forEach { interpret(it) }
        history.allOtherTests().forEach { interpret(it) }
        return interpretationBuilder.build()
    }
}