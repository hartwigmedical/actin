package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.report.pdf.assertRow
import org.junit.Ignore
import org.junit.Test

private const val APPROVED = "approved"
private const val ON_LABEL_EXPERIMENTAL = "onLabelExperimental"
private const val OFF_LABEL_EXPERIMENTAL = "offLabelExperimental"
private const val PRE_CLINICAL = "preClinical"

@Ignore
class MolecularClinicalEvidenceGeneratorTest {

    @Test
    fun `Should create a table with rows for each treatment category`() {
        val evidence =
            ClinicalEvidence(
                treatmentEvidence = emptySet()
            )
        val table = MolecularClinicalEvidenceGenerator(
            molecularHistory(variant(evidence)), 1f
        )
        assertRow(table.contents(), 0, "BRAF V600E", APPROVED, "X", "", "", "", "X")
        assertRow(table.contents(), 1, "", ON_LABEL_EXPERIMENTAL, "", "X", "", "", "")
        assertRow(table.contents(), 2, "", OFF_LABEL_EXPERIMENTAL, "", "", "X", "", "")
        assertRow(table.contents(), 3, "", PRE_CLINICAL, "", "", "", "X", "")
    }

    @Test
    fun `Should create 'many' row for categories with more than 2 treatments`() {
        val evidence =
            ClinicalEvidence(
                treatmentEvidence = emptySet(),
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

    private fun variant(evidence: ClinicalEvidence) = TestMolecularFactory.createProperVariant().copy(evidence = evidence)
}