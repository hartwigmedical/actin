package com.hartwig.actin.report.interpretation

class PriorIHCTestInterpreterTest {

    private val interpreter = PriorMolecularTestInterpreter()

    /*@Test
    fun `Should interpret IHC test based on score text`() {
        val result = interpreter.interpret(MolecularHistory(listOf(ihcMolecularTest("HER2", "Positive"))))
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                "IHC", listOf(PriorMolecularTestResultInterpretation("Positive", "HER2"))
            )
        )
    }

    @Test
    fun `Should interpret IHC test based score value`() {
        val result = interpreter.interpret(MolecularHistory(listOf(ihcMolecularTest("HER2", scoreValue = 90.0, scoreValueUnit = "%"))))
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                "IHC", listOf(PriorMolecularTestResultInterpretation("HER2", "Score 90%", 1))
            )
        )
    }

    @Test
    fun `Should interpret Archer test based variants and implied negatives`() {
        val result = interpreter.interpret(
            MolecularHistory(
                listOf(
                    TestPanelRecordFactory.empty().copy(
                        panelExtraction = ArcherPanelExtraction(
                            variants = listOf(PanelVariantExtraction("ALK", "c.2240_2254del")),
                            fusions = listOf(ArcherFusionExtraction("ALK")),
                            skippedExons = emptyList()
                        )
                    )
                )
            )
        )
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                type = "Archer", results = listOf(
                    PriorMolecularTestResultInterpretation(grouping = "Variants", details = "ALK c.2240_2254del"),
                    PriorMolecularTestResultInterpretation(grouping = "Fusions", details = "ALK fusion"),
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
    fun `Should interpret generic panel tests based on variants, fusions, exon deletions and implied negatives`() {
        val result = interpreter.interpret(
            MolecularHistory(
                listOf(
                    TestPanelRecordFactory.empty().copy(
                        panelExtraction =
                        GenericPanelExtraction(
                            panelType = AVL_PANEL,
                            variants = listOf(PanelVariantExtraction("ALK", "c.2240_2254del")),
                            fusions = listOf(GenericFusionExtraction("EML4", "ALK")),
                            exonDeletions = listOf(GenericExonDeletionExtraction("EGFR", 19)),
                            genesWithNegativeResults = setOf("RET")
                        )
                    )
                )
            )
        )
        assertThat(result).containsExactly(
            PriorMolecularTestInterpretation(
                type = AVL_PANEL, results = listOf(
                    PriorMolecularTestResultInterpretation(grouping = "Variants", details = "ALK c.2240_2254del"),
                    PriorMolecularTestResultInterpretation(grouping = "Fusions", details = "EML4-ALK fusion"),
                    PriorMolecularTestResultInterpretation(grouping = "Exon deletions", details = "EGFR exon 19 deletion"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "BRAF"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "KRAS"),
                    PriorMolecularTestResultInterpretation(grouping = "Negative", details = "RET"),
                )
            )
        )
    }

    private fun ihcMolecularTest(gene: String, scoreText: String? = null, scoreValue: Double? = null, scoreValueUnit: String? = null) =
        IHCMolecularTest(
            test = PriorIHCTest(
                item = gene,
                scoreText = scoreText,
                test = "IHC",
                scoreValue = scoreValue,
                scoreValueUnit = scoreValueUnit,
                impliesPotentialIndeterminateStatus = false
            )
        )*/
}