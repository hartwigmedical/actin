package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.report.pdf.assertHeader
import com.hartwig.actin.report.pdf.getCellContents
import com.hartwig.actin.report.pdf.tables.CellTestUtil.extractTextFromCell
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
        val generator = createGenerator(displayMode = ImmunologyDisplayMode.SUMMARY, title = "HLA Profile")
        assertThat(generator.title()).isEqualTo("HLA Profile")
    }

    @Test
    fun `Should force keep together`() {
        val generator = createGenerator(displayMode = ImmunologyDisplayMode.DETAILED)
        assertThat(generator.forceKeepTogether()).isTrue
    }

    @Test
    fun `Should show correct headers in detailed mode`() {
        val generator = createGenerator(
            displayMode = ImmunologyDisplayMode.DETAILED,
            hlaAlleles = listOf(createHlaAllele("HLA-A", "01", "01"))
        )
        assertHeader(generator, "HLA gene", "Type", "Tumor copy number", "Mutated in tumor")
    }

    @Test
    fun `Should show gene, allele, copy number and mutation in detailed mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0, hasSomaticMutations = false)
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(getCellContents(table, 0, 0)).isEqualTo("HLA-A")
        assertThat(getCellContents(table, 0, 1)).isEqualTo("HLA-A*01:01")
        assertThat(getCellContents(table, 0, 2)).isEqualTo("1")
        assertThat(getCellContents(table, 0, 3)).isEqualTo("No")
    }

    @Test
    fun `Should show Yes for somatic mutations true in detailed mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0, hasSomaticMutations = true)
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(getCellContents(table, 0, 3)).isEqualTo("Yes")
    }

    @Test
    fun `Should show dash for null somatic mutations in detailed mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", hasSomaticMutations = null)
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(getCellContents(table, 0, 3)).isEqualTo("-")
    }

    @Test
    fun `Should show dash for null tumor copy number in detailed mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = null)
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(getCellContents(table, 0, 2)).isEqualTo("-")
    }

    @Test
    fun `Should format fractional copy number with one decimal in detailed mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 0.5)
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(getCellContents(table, 0, 2)).isEqualTo("0.5")
    }

    @Test
    fun `Should format zero copy number with one decimal in detailed mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 0.0)
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(getCellContents(table, 0, 2)).isEqualTo("0.0")
    }

    @Test
    fun `Should clamp negative copy number to zero in detailed mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = -1.0)
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(getCellContents(table, 0, 2)).isEqualTo("0.0")
    }

    @Test
    fun `Should show no alleles message when alleles list is empty in detailed mode`() {
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = emptyList()).contents()

        assertThat(getCellContents(table, 0, 0)).isEqualTo("HLA-A")
        assertThat(getCellContents(table, 0, 1)).isEqualTo("No HLA-A alleles detected")
        assertThat(getCellContents(table, 0, 2)).isEqualTo("")
        assertThat(getCellContents(table, 0, 3)).isEqualTo("")
    }

    @Test
    fun `Should show unavailable message for null immunology in detailed mode`() {
        val table = createGeneratorWithNullImmunology(ImmunologyDisplayMode.DETAILED).contents()

        assertThat(getCellContents(table, 0, 0)).isEqualTo("HLA-A")
        assertThat(getCellContents(table, 0, 1)).isEqualTo("HLA typing not available")
        assertThat(getCellContents(table, 0, 2)).isEqualTo("")
        assertThat(getCellContents(table, 0, 3)).isEqualTo("")
    }

    @Test
    fun `Should filter out non-HLA-A alleles in detailed mode`() {
        val hlaA = createHlaAllele("HLA-A", "01", "01")
        val hlaB = createHlaAllele("HLA-B", "07", "02")
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaA, hlaB)).contents()

        // Only HLA-A is displayed, so just one data row
        assertThat(table.numberOfRows).isEqualTo(1)
        assertThat(getCellContents(table, 0, 0)).isEqualTo("HLA-A")
        assertThat(getCellContents(table, 0, 1)).isEqualTo("HLA-A*01:01")
    }

    @Test
    fun `Should show no alleles message when only non-HLA-A alleles are present in detailed mode`() {
        val hlaB = createHlaAllele("HLA-B", "07", "02")
        val hlaC = createHlaAllele("HLA-C", "01", "01")
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(hlaB, hlaC)).contents()

        assertThat(getCellContents(table, 0, 0)).isEqualTo("HLA-A")
        assertThat(getCellContents(table, 0, 1)).isEqualTo("No HLA-A alleles detected")
    }

    @Test
    fun `Should sort HLA-A alleles by allele group and protein in detailed mode`() {
        val allele1 = createHlaAllele("HLA-A", "02", "01")
        val allele2 = createHlaAllele("HLA-A", "01", "01")
        val allele3 = createHlaAllele("HLA-A", "01", "02")
        val table = createGenerator(
            ImmunologyDisplayMode.DETAILED,
            hlaAlleles = listOf(allele1, allele2, allele3)
        ).contents()

        // Sorted order: 01:01, 01:02, 02:01
        assertThat(getCellContents(table, 0, 1)).isEqualTo("HLA-A*01:01")
        assertThat(getCellContents(table, 1, 1)).isEqualTo("HLA-A*01:02")
        assertThat(getCellContents(table, 2, 1)).isEqualTo("HLA-A*02:01")
    }

    @Test
    fun `Should show empty gene cell for subsequent alleles in detailed mode`() {
        val allele1 = createHlaAllele("HLA-A", "01", "01")
        val allele2 = createHlaAllele("HLA-A", "02", "01")
        val table = createGenerator(ImmunologyDisplayMode.DETAILED, hlaAlleles = listOf(allele1, allele2)).contents()

        assertThat(getCellContents(table, 0, 0)).isEqualTo("HLA-A")
        assertThat(getCellContents(table, 1, 0)).isEqualTo("")
    }

    @Test
    fun `Should show gene cell and formatted allele text in summary mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0, hasSomaticMutations = false)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("HLA-A")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: 1, Mutated: No")
    }

    @Test
    fun `Should show dash for null copy number in summary mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = null, hasSomaticMutations = true)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: -, Mutated: Yes")
    }

    @Test
    fun `Should show zero copy number in summary mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 0.0, hasSomaticMutations = false)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: 0.0, Mutated: No")
    }

    @Test
    fun `Should clamp negative copy number to zero in summary mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = -1.0, hasSomaticMutations = false)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: 0.0, Mutated: No")
    }

    @Test
    fun `Should show fractional copy number in summary mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 0.5, hasSomaticMutations = null)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: 0.5, Mutated: -")
    }

    @Test
    fun `Should show both copy number and mutation in summary mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 2.0, hasSomaticMutations = true)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: 2, Mutated: Yes")
    }

    @Test
    fun `Should show dash for null somatic mutations in summary mode`() {
        val hlaAllele = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0, hasSomaticMutations = null)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaAllele)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: 1, Mutated: -")
    }

    @Test
    fun `Should show no alleles message in summary mode`() {
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = emptyList()).contents()

        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("HLA-A")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("No HLA-A alleles detected")
    }

    @Test
    fun `Should show no alleles message when only non-HLA-A alleles are present in summary mode`() {
        val hlaB = createHlaAllele("HLA-B", "07", "02")
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(hlaB)).contents()

        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("HLA-A")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("No HLA-A alleles detected")
    }

    @Test
    fun `Should show unavailable message for null immunology in summary mode`() {
        val table = createGeneratorWithNullImmunology(ImmunologyDisplayMode.SUMMARY).contents()

        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("HLA-A")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA typing not available")
    }

    @Test
    fun `Should show empty gene cell for subsequent alleles in summary mode`() {
        val allele1 = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0, hasSomaticMutations = false)
        val allele2 = createHlaAllele("HLA-A", "02", "01", tumorCopyNumber = 2.0, hasSomaticMutations = true)
        val table = createGenerator(ImmunologyDisplayMode.SUMMARY, hlaAlleles = listOf(allele1, allele2)).contents()

        assertThat(table.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("HLA-A")
        assertThat(extractTextFromCell(table.getCell(1, 0))).isEqualTo("")
    }

    @Test
    fun `Should sort alleles and produce one row per allele in summary mode`() {
        val allele1 = createHlaAllele("HLA-A", "01", "01", tumorCopyNumber = 1.0)
        val allele2 = createHlaAllele("HLA-A", "02", "01", tumorCopyNumber = 2.0, hasSomaticMutations = true)
        val allele3 = createHlaAllele("HLA-A", "03", "01", tumorCopyNumber = 1.5)
        val table = createGenerator(
            ImmunologyDisplayMode.SUMMARY,
            hlaAlleles = listOf(allele2, allele3, allele1)  // unsorted input
        ).contents()

        assertThat(table.numberOfRows).isEqualTo(3)
        // Sorted: 01:01, 02:01, 03:01
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("HLA-A*01:01 Copy number: 1, Mutated: -")
        assertThat(extractTextFromCell(table.getCell(1, 1))).isEqualTo("HLA-A*02:01 Copy number: 2, Mutated: Yes")
        assertThat(extractTextFromCell(table.getCell(2, 1))).isEqualTo("HLA-A*03:01 Copy number: 2, Mutated: -")
    }

    private fun createGenerator(
        displayMode: ImmunologyDisplayMode = ImmunologyDisplayMode.DETAILED,
        title: String = "Immunology",
        hlaAlleles: List<HlaAllele> = emptyList()
    ): ImmunologyGenerator {
        val molecular = TestMolecularFactory.createMinimalWholeGenomeTest().copy(
            immunology = MolecularImmunology(isReliable = true, hlaAlleles = hlaAlleles.toSet())
        )
        return ImmunologyGenerator(molecular, displayMode, title, keyWidth, valueWidth)
    }

    private fun createGeneratorWithNullImmunology(displayMode: ImmunologyDisplayMode): ImmunologyGenerator {
        val molecular = TestMolecularFactory.createMinimalWholeGenomeTest().copy(immunology = null)
        return ImmunologyGenerator(molecular, displayMode, "Immunology", keyWidth, valueWidth)
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