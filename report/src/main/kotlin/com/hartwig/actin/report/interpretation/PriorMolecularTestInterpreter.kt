package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ArcherMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GenericPanelMolecularTest
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTestVisitor
import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.report.pdf.util.Formats
import org.apache.logging.log4j.LogManager

class PriorMolecularTestInterpreter : MolecularTestVisitor {
    private val logger = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()

    override fun visit(test: IHCMolecularTest) {
        val scoreText = test.result.scoreText
        if (scoreText != null) {
            interpretationBuilder.addInterpretation(test.result.test, test.result.scoreText!!, test.result.item ?: "")
        } else if (test.result.scoreValue != null) {
            interpretationBuilder.addInterpretation(
                test.result.test, test.result.item ?: "", formatValueBasedPriorTest(test.result)
            )
        } else {
            logger.error("IHC test is neither text-based nor value-based: {}", test.result)
        }
    }

    override fun visit(test: ArcherMolecularTest) {
        test.result.variants.forEach { interpretationBuilder.addInterpretation(test.type.display(), it.gene, it.hgvsCodingImpact) }
        interpretImpliedNegatives(test.type, test.result, test.result.genesWithVariants())
    }

    override fun visit(test: GenericPanelMolecularTest) {
        interpretImpliedNegatives(test.type, test.result)
    }

    private fun interpretImpliedNegatives(type: ExperimentType, panel: Panel, tested: Set<String> = emptySet()) {
        (panel.testedGenes() - tested).forEach { interpretationBuilder.addInterpretation(type.display(), "Negative", it) }
    }

    private fun formatValueBasedPriorTest(valueTest: PriorMolecularTest): String {
        return listOfNotNull(
            "Score",
            valueTest.measure,
            valueTest.scoreValuePrefix,
            Formats.twoDigitNumber(valueTest.scoreValue!!) + valueTest.scoreValueUnit
        ).joinToString(" ")
    }

    fun interpret(priorTests: List<MolecularTest<*>>): List<PriorMolecularTestInterpretation> {
        priorTests.forEach { it.accept(this) }
        return interpretationBuilder.build()
    }
}