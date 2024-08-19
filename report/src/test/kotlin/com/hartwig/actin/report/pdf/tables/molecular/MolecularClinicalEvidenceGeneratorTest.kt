package com.hartwig.actin.report.pdf.tables.molecular

private const val APPROVED = "approved"
private const val ON_LABEL_EXPERIMENTAL = "onLabelExperimental"
private const val OFF_LABEL_EXPERIMENTAL = "offLabelExperimental"
private const val PRE_CLINICAL = "preClinical"
private const val RESISTANT = "resistant"

class MolecularClinicalEvidenceGeneratorTest {

  /*  @Test
    fun `Should create a table with rows for each treatment category`() {
        val evidence =
            ActionableEvidence(
                approvedTreatments = setOf(APPROVED),
                onLabelExperimentalTreatments = setOf(ON_LABEL_EXPERIMENTAL),
                offLabelExperimentalTreatments = setOf(OFF_LABEL_EXPERIMENTAL),
                preClinicalTreatments = setOf(PRE_CLINICAL),
                knownResistantTreatments = setOf(RESISTANT)
            )
        val table = MolecularClinicalEvidenceGenerator(
            molecularHistory(variant(evidence)), 1f
        )
        assertRow(table.contents(), 0, "BRAF V600E", APPROVED, "X", "", "", "", "")
        assertRow(table.contents(), 1, "", ON_LABEL_EXPERIMENTAL, "", "X", "", "", "")
        assertRow(table.contents(), 2, "", OFF_LABEL_EXPERIMENTAL, "", "", "X", "", "")
        assertRow(table.contents(), 3, "", PRE_CLINICAL, "", "", "", "X", "")
        assertRow(table.contents(), 4, "", RESISTANT, "", "", "", "", "X")
    }

    @Test
    fun `Should create 'many' row for categories with more than 2 treatments`() {
        val evidence =
            ActionableEvidence(
                approvedTreatments = setOf("1", "2", "3"),
                onLabelExperimentalTreatments = setOf("1", "2", "3"),
            )
        val table = MolecularClinicalEvidenceGenerator(
            molecularHistory(variant(evidence)), 1f
        )
        assertRow(table.contents(), 0, "BRAF V600E", "<many>", "X", "X", "", "", "")
    }

    private fun molecularHistory(variant: Variant) = MolecularHistory(
        listOf(
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(
                drivers = Drivers(variants = setOf(variant))
            )
        )
    )

    private fun variant(evidence: ActionableEvidence) = TestMolecularFactory.createProperVariant().copy(evidence = evidence)*/
}