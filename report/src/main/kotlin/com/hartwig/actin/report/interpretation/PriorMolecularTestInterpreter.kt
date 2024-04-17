package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.ArcherMolecularTest
import com.hartwig.actin.molecular.datamodel.GenericPanelMolecularTest
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTestVisitor
import org.apache.logging.log4j.LogManager

class PriorMolecularTestInterpreter : MolecularTestVisitor {
    private val logger = LogManager.getLogger(PriorMolecularTestInterpreter::class.java)

    private val interpretationBuilder = PriorMolecularTestInterpretationBuilder()

    override fun visit(test: IHCMolecularTest) {
        val scoreText = test.result.scoreText
        if (scoreText != null) {
            interpretationBuilder.addTest(test.result.test, test.result.item ?: "", test.result.scoreText!!)
        } else if (test.result.scoreValue != null) {
            interpretationBuilder.addTest(test.result.test, test.result.item ?: "", test.result.scoreValue.toString())
        } else {
            logger.error("IHC test is neither text-based nor value-based: {}", test.result)
        }
    }

    override fun visit(test: ArcherMolecularTest) {
        test.result.variants.forEach { interpretationBuilder.addTest(test.type.display(), it.gene, it.hgvsCodingImpact) }
    }

    override fun visit(test: GenericPanelMolecularTest) {
        test.result.testedGenes().forEach { interpretationBuilder.addTest(test.type.display(), it, "Negative") }
    }

    fun interpret(priorTests: List<MolecularTest<*>>): PriorMolecularTestInterpretation {
        priorTests.forEach { it.accept(this) }
        return interpretationBuilder.build()
    }
}