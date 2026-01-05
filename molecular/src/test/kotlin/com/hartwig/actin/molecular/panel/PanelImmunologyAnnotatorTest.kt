package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedHlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.molecular.util.ExtractionUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class PanelImmunologyAnnotatorTest {

    private val annotator = PanelImmunologyAnnotator()

    @Test
    fun `Should return null when no HLA alleles provided`() {
        assertThat(annotator.annotate(emptySet())).isNull()
    }

    @Test
    fun `Should map HLA alleles to immunology`() {
        val hlaAllele = SequencedHlaAllele(
            name = "A*02:01",
            tumorCopyNumber = null,
            hasSomaticMutations = false
        )

        val immunology = annotator.annotate(setOf(hlaAllele))

        val expectedMolecularHlaAllele = HlaAllele(
            gene = "HLA-A",
            alleleGroup = "02",
            hlaProtein = "01",
            tumorCopyNumber = null,
            hasSomaticMutations = false,
            evidence = ExtractionUtil.noEvidence(),
            event = "HLA-A*02:01"
        )

        assertThat(immunology).isNotNull()
        assertThat(immunology!!.isReliable).isTrue
        assertThat(immunology.hlaAlleles).containsExactly(expectedMolecularHlaAllele)
    }

    @Test
    fun `Should accept HLA prefix and normalize event`() {
        val hlaAllele = SequencedHlaAllele(
            name = "HLA-A*02:01",
            tumorCopyNumber = 1.5,
            hasSomaticMutations = null
        )

        val immunology = annotator.annotate(setOf(hlaAllele))

        val expectedMolecularHlaAllele = HlaAllele(
            gene = "HLA-A",
            alleleGroup = "02",
            hlaProtein = "01",
            tumorCopyNumber = 1.5,
            hasSomaticMutations = null,
            evidence = ExtractionUtil.noEvidence(),
            event = "HLA-A*02:01"
        )

        assertThat(immunology).isNotNull()
        assertThat(immunology!!.hlaAlleles).containsExactly(expectedMolecularHlaAllele)
    }

    @Test
    fun `Should accept additional allele fields`() {
        val hlaAllele = SequencedHlaAllele(
            name = "A*02:01:01",
            tumorCopyNumber = null,
            hasSomaticMutations = null
        )

        val immunology = annotator.annotate(setOf(hlaAllele))

        val expectedMolecularHlaAllele = HlaAllele(
            gene = "HLA-A",
            alleleGroup = "02",
            hlaProtein = "01",
            tumorCopyNumber = null,
            hasSomaticMutations = null,
            evidence = ExtractionUtil.noEvidence(),
            event = "HLA-A*02:01:01"
        )

        assertThat(immunology).isNotNull()
        assertThat(immunology!!.hlaAlleles).containsExactly(expectedMolecularHlaAllele)
    }

    @Test
    fun `Should throw for invalid HLA allele name`() {
        val hlaAllele = SequencedHlaAllele(
            name = "A:01*02",
            tumorCopyNumber = null,
            hasSomaticMutations = null
        )

        assertThatThrownBy { annotator.annotate(setOf(hlaAllele)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("example: A*02:01")
    }
}