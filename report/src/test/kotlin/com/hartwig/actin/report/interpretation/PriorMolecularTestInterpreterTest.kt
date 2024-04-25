package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ArcherMolecularTest
import com.hartwig.actin.molecular.datamodel.GenericPanelMolecularTest
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorMolecularTestInterpreterTest {

    private val interpreter = PriorMolecularTestInterpreter()

    @Test
    fun `Should interpret IHC test based on score text`() {
        val result = interpreter.interpret(listOf(ihcMolecularTest("HER2", "Positive")))
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                "IHC", listOf(PriorMolecularTestResultInterpretation("Positive", "HER2"))
            )
        )
    }

    @Test
    fun `Should interpret IHC test based score value`() {
        val result = interpreter.interpret(listOf(ihcMolecularTest("HER2", scoreValue = 90.0, scoreValueUnit = "%")))
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                "IHC", listOf(PriorMolecularTestResultInterpretation("HER2", "Score 90%", 1))
            )
        )
    }

    @Test
    fun `Should interpret Archer test based variants and implied negatives`() {
        val result = interpreter.interpret(
            listOf(
                ArcherMolecularTest(
                    result = ArcherPanel(variants = listOf(ArcherVariant("ALK", "c.2240_2254del")), fusions = emptyList())
                )
            )
        )
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                type = "Archer", results = listOf(
                    PriorMolecularTestResultInterpretation(grouping = "ALK", details = "c.2240_2254del"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "ROS1"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "RET"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "MET"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "NTRK1"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "NTRK2"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "NTRK3"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "NRG1")
                )
            )
        )
    }

    @Test
    fun `Should interpret generic panel tests based on implied negatives`() {
        val result = interpreter.interpret(
            listOf(
                GenericPanelMolecularTest(
                    result = GenericPanel(GenericPanelType.AVL)
                )
            )
        )
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                type = "NGS Panel", results = listOf(
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "EGFR"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "BRAF"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "KRAS"),
                )
            )
        )
    }

    @Test
    fun `Should interpret other molecular tests based on their curated test`() {
        val result = interpreter.interpret(
            listOf(
                OtherPriorMolecularTest(
                    result = PriorMolecularTest(
                        test = "Freetext",
                        item = "ALK",
                        scoreText = "Positive",
                        impliesPotentialIndeterminateStatus = false
                    )
                )
            )
        )
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                type = "Other", results = listOf(
                    PriorMolecularTestResultInterpretation(grouping = "Positive", details = "ALK"),
                )
            )
        )
    }

    private fun ihcMolecularTest(gene: String, scoreText: String? = null, scoreValue: Double? = null, scoreValueUnit: String? = null) =
        IHCMolecularTest(
            result = PriorMolecularTest(
                item = gene,
                scoreText = scoreText,
                test = "IHC",
                scoreValue = scoreValue,
                scoreValueUnit = scoreValueUnit,
                impliesPotentialIndeterminateStatus = false
            )
        )
}