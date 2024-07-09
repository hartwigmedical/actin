package com.hartwig.actin.molecular.paver

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
                gene = "gene",
                chromosome = "1",
                position = 320,
                ref = "T",
                alt = "C",
                mutation = "c.320_321T>C"
            )
        )

        val paver = Paver(paverConfig)

        val response = paver.pave(queries)
        assertThat(response.size).isEqualTo(1)
        assertThat(response.get(0).id).isEqualTo("1")
    }
}