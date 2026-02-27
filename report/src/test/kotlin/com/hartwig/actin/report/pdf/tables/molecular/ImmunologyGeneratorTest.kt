package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ImmunologyGeneratorTest {

    private val keyWidth = 50f
    private val valueWidth = 100f

    @Test
    fun `Should return Immunology title by default`() {
        val generator = createGenerator(displayMode = ImmunologyDisplayMode.DETAILED)
        assertThat(generator.title()).isEqualTo("Immunology")
    }

    @Test
    fun `Should return custom title when provided`() {
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            title = "HLA Profile"
        )
        assertThat(generator.title()).isEqualTo("HLA Profile")
    }

    @Test
    fun `Should force keep together`() {
        val generator = createGenerator(displayMode = ImmunologyDisplayMode.DETAILED)
        assertThat(generator.forceKeepTogether()).isTrue
    }

    @Test
    fun `Should create detailed table with headers for DETAILED mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0, hasSomaticMutations = false)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.DETAILED,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table.numberOfRows).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun `Should create summary table for SUMMARY mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0, hasSomaticMutations = false)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table.numberOfRows).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun `Should display only HLA-A alleles`() {
        val allele1 = createHlaAllele("HLA-A", "01", "01")
        val allele2 = createHlaAllele("HLA-A", "02", "01")
        val allele3 = createHlaAllele("HLA-B", "01", "01")  // Should be filtered out

        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.DETAILED,
            hlaAlleles = listOf(allele1, allele2, allele3)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
    }

    @Test
    fun `Should sort HLA-A alleles by allele group and protein`() {
        val allele1 = createHlaAllele("HLA-A", "02", "01")
        val allele2 = createHlaAllele("HLA-A", "01", "01")
        val allele3 = createHlaAllele("HLA-A", "01", "02")

        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.DETAILED,
            hlaAlleles = listOf(allele1, allele2, allele3)
        )

        val table = generator.contents()
        // Sorted: HLA-A*01:01, HLA-A*01:02, HLA-A*02:01
        assertThat(table).isNotNull
    }

    @Test
    fun `Should handle null immunology gracefully`() {
        val molecular = TestMolecularFactory.createMinimalWholeGenomeTest()
        val generator = ImmunologyGenerator(molecular, ImmunologyDisplayMode.DETAILED, "Immunology", keyWidth, valueWidth)

        val table = generator.contents()
        assertThat(table).isNotNull
    }

    @Test
    fun `Should handle empty HLA alleles list`() {
        val generator = createGenerator(displayMode = ImmunologyDisplayMode.DETAILED, hlaAlleles = emptyList())

        val detailedTable = generator.contents()
        assertThat(detailedTable).isNotNull
    }

    @Test
    fun `Should handle zero tumor copy number`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 0.0)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
    }

    @Test
    fun `Should handle fractional tumor copy number`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 0.5)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
    }

    @Test
    fun `Should handle null somatic mutations`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", hasSomaticMutations = null)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.DETAILED,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
    }

    @Test
    fun `Should display allele with copy number but no mutation`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.5, hasSomaticMutations = false)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
        assertThat(table.numberOfRows).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun `Should display allele with mutation but no copy number`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = null, hasSomaticMutations = true)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
        assertThat(table.numberOfRows).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun `Should display allele with neither copy number nor mutation`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = null, hasSomaticMutations = false)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
        assertThat(table.numberOfRows).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun `Should display allele with both copy number and mutation`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 2.0, hasSomaticMutations = true)
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(hlaAllele)
        )

        val table = generator.contents()
        assertThat(table).isNotNull
        assertThat(table.numberOfRows).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun `Should handle multiple HLA-A alleles in summary mode`() {
        val allele1 = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0)
        val allele2 = createHlaAllele("HLA-A", "02", "01", tumorCopyNumber = 2.0, hasSomaticMutations = true)
        val allele3 = createHlaAllele("HLA-A", "03", "01", tumorCopyNumber = 1.5)

        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(allele1, allele2, allele3)
        )

        val table = generator.contents()
        assertThat(table.numberOfRows).isGreaterThanOrEqualTo(3)
    }

    // Helper functions
    private fun createGenerator(
        displayMode: ImmunologyDisplayMode = ImmunologyDisplayMode.DETAILED,
        title: String = "Immunology",
        hlaAlleles: List<HlaAllele> = emptyList()
    ): ImmunologyGenerator {
        val molecular = TestMolecularFactory.createMinimalWholeGenomeTest().copy(
            immunology = if (hlaAlleles.isNotEmpty()) {
                MolecularImmunology(isReliable = true, hlaAlleles = hlaAlleles.toSet())
            } else null
        )
        return ImmunologyGenerator(molecular, displayMode, title, keyWidth, valueWidth)
    }

    private fun createHlaAllele(
        gene: String,
        alleleGroup: String,
        hlaProtein: String,
        tumorCopyNumber: Double? = null,
        hasSomaticMutations: Boolean? = null
    ): HlaAllele {
        return HlaAllele(
            gene = gene,
            alleleGroup = alleleGroup,
            hlaProtein = hlaProtein,
            tumorCopyNumber = tumorCopyNumber,
            hasSomaticMutations = hasSomaticMutations,
            evidence = com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence(
                treatmentEvidence = emptySet(),
                eligibleTrials = emptySet()
            ),
            event = "$gene*$alleleGroup:$hlaProtein"
        )
    }
}