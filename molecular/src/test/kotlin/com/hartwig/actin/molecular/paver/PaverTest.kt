package com.hartwig.actin.molecular.paver

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private val REF_GENOME_FASTA = resourceOnClasspath("paver/ref_genome/ref_genome.fasta")
private val ENSEMBL_DATA_DIR = resourceOnClasspath("paver/ensembl")

class PaverTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    @Test
    fun `Should run paver`() {
        val paverConfig = PaverConfig(
            ensemblDataDir = ENSEMBL_DATA_DIR,
            refGenomeFasta = REF_GENOME_FASTA,
            refGenomeVersion = "37",
            tempDir = tempDir.root.absolutePath
        )

        val queries = listOf(
            PaveQuery(
                id = "1",
                chromosome = "1",
                position = 14,
                ref = "A",
                alt = "C",
            )
        )

        val paver = Paver(paverConfig)

        val responses = paver.pave(queries)
        assertThat(responses.size).isEqualTo(1)
        val response = responses.get(0)
        assertThat(response.id).isEqualTo("1")
        assertThat(response.impact.gene).isEqualTo("gene1")
        assertThat(response.impact.hgvsCodingImpact).isEqualTo("c.6A>C")
        assertThat(response.impact.hgvsProteinImpact).isEqualTo("p.Lys2Asn")
        assertThat(response.impact.canonicalCodingEffect).isEqualTo(CodingEffect.MISSENSE)
        assertThat(response.impact.spliceRegion).isEqualTo(false)
    }
}