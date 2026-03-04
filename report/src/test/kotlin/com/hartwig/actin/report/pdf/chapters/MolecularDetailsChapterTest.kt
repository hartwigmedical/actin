package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.MolecularChapterType
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.TrialMatchingChapterType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.assertHeader
import com.hartwig.actin.report.pdf.tables.CellTestUtil
import com.hartwig.actin.report.pdf.tables.molecular.ImmunologyDisplayMode
import com.hartwig.actin.report.pdf.tables.molecular.ImmunologyGenerator
import com.hartwig.actin.report.trial.TrialsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val KEY_WIDTH = 50f
private const val VALUE_WIDTH = 100f

class MolecularDetailsChapterTest {

    private val report = TestReportFactory.createProperTestReport()
    private val testAllele = HlaAllele(
        gene = "HLA-A",
        alleleGroup = "01",
        hlaProtein = "01",
        tumorCopyNumber = 2.0,
        hasSomaticMutations = false,
        evidence = ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet()),
        event = "HLA-A*01:01"
    )

    @Test
    fun `DETAILED_INLINE mode shows allele details per row without table headers`() {
        val generator = ImmunologyGenerator(
            testWithReliableImmunology(), ImmunologyDisplayMode.DETAILED_INLINE, "Immunology", KEY_WIDTH, VALUE_WIDTH
        )
        assertThat(CellTestUtil.extractTextFromCell(generator.contents().getCell(0, 1)))
            .isEqualTo("HLA-A*01:01, tumor copy nr: 2, mutated: No")
    }

    @Test
    fun `DETAILED_TABLE mode shows tabular format with headers`() {
        val generator = ImmunologyGenerator(
            testWithReliableImmunology(), ImmunologyDisplayMode.DETAILED_TABLE, "Immunology", KEY_WIDTH, VALUE_WIDTH
        )
        assertHeader(generator, "HLA gene", "Type", "Tumor copy number", "Mutated in tumor")
    }

    @Test
    fun `ALLELE_ONLY mode shows comma-separated alleles on a single row`() {
        val generator = ImmunologyGenerator(
            testWithReliableImmunology(), ImmunologyDisplayMode.ALLELE_ONLY, "Immunology", KEY_WIDTH, VALUE_WIDTH
        )
        assertThat(CellTestUtil.extractTextFromCell(generator.contents().getCell(0, 1)))
            .isEqualTo("HLA-A*01:01")
    }

    @Test
    fun `Chapter can be created for STANDARD_WITH_PATHOLOGY config`() {
        assertThat(createChapter(MolecularChapterType.STANDARD_WITH_PATHOLOGY)).isNotNull()
    }

    @Test
    fun `Chapter can be created for STANDARD config`() {
        assertThat(createChapter(MolecularChapterType.STANDARD)).isNotNull()
    }

    private fun testWithReliableImmunology(): MolecularTest {
        return TestMolecularFactory.createMinimalWholeGenomeTest().copy(
            immunology = MolecularImmunology(isReliable = true, hlaAlleles = setOf(testAllele))
        )
    }

    private fun createChapter(molecularChapterType: MolecularChapterType): MolecularDetailsChapter {
        val configuration = ReportConfiguration(molecularChapterType = molecularChapterType)
        val trialsProvider = TrialsProvider.create(
            report.patientRecord,
            report.treatmentMatch,
            configuration.countryOfReference,
            TestDoidModelFactory.createMinimalTestDoidModel(),
            configuration.dutchExternalTrialsToExclude,
            configuration.trialMatchingChapterType == TrialMatchingChapterType.DETAILED_ALL_TRIALS,
            configuration.filterOnSOCExhaustionAndTumorType,
        )
        return MolecularDetailsChapter(report, configuration, trialsProvider)
    }
}
