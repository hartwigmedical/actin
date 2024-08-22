package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.treatment
import com.hartwig.actin.report.pdf.assertRow
import org.junit.Test

private const val APPROVED = "approved"
private const val ON_LABEL_EXPERIMENTAL = "onLabelExperimental"
private const val OFF_LABEL_EXPERIMENTAL = "offLabelExperimental"
private const val PRE_CLINICAL = "preClinical"

class MolecularClinicalEvidenceGeneratorTest {

    @Test
    fun `Should create a table with rows for each treatment category`() {
        val evidence =
            ActionableEvidence(
                actionableTreatments = setOf(
                    treatment(APPROVED, ActinEvidenceCategory.APPROVED),
                    treatment(ON_LABEL_EXPERIMENTAL, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL),
                    treatment(OFF_LABEL_EXPERIMENTAL, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL),
                    treatment(PRE_CLINICAL, ActinEvidenceCategory.PRE_CLINICAL),
                    treatment(APPROVED, ActinEvidenceCategory.KNOWN_RESISTANT)
                )
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
            ActionableEvidence(
                actionableTreatments = setOf(
                    treatment("1", ActinEvidenceCategory.APPROVED),
                    treatment("2", ActinEvidenceCategory.APPROVED),
                    treatment("3", ActinEvidenceCategory.APPROVED),
                    treatment("4", ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL),
                    treatment("5", ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL),
                    treatment("6", ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
                ),
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

    private fun variant(evidence: ActionableEvidence) = TestMolecularFactory.createProperVariant().copy(evidence = evidence)
}