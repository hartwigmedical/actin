package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ArcherMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GenericPanelMolecularTest
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTestVisitor
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

class PriorMolecularTestInterpreter : MolecularTestVisitor {
    private val logger = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()

    override fun visit(test: IHCMolecularTest) {
        val result = test.result
        val item = result.item ?: ""
        val type = result.test
        val scoreText = result.scoreText
        val scoreValue = result.scoreValue
        when {
            scoreText != null -> interpretationBuilder.addInterpretation(type, scoreText, item, 0)
            scoreValue != null -> interpretationBuilder.addInterpretation(type, item, formatValueBasedPriorTest(result), 1)
            else -> logger.error("IHC test is neither text-based nor value-based: {}", result)
        }
    }

    override fun visit(test: ArcherMolecularTest) {
        test.result.variants.forEach { interpretationBuilder.addInterpretation(test.type.display(), it.gene, it.hgvsCodingImpact) }
        interpretImpliedNegatives(test.type, test.result, test.result.genesWithVariants())
    }

    override fun visit(test: GenericPanelMolecularTest) {
        interpretImpliedNegatives(test.type, test.result)
    }

    override fun visit(test: OtherPriorMolecularTest) {
        val scoreText = test.result.scoreText
        val item = test.result.item
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

    fun interpret(priorTests: List<MolecularTest<*>>): List<PriorMolecularTestInterpretation> {
        priorTests.forEach { it.accept(this) }
        return interpretationBuilder.build()
    }
}