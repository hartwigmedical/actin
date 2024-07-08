package com.hartwig.actin.molecular.paver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class PaverTest {

    @Ignore
    @Test
    fun `Should run paver`() {
        val paverConfig = PaverConfig(
            ensemblDataDir = "/Users/khalid/hmf/repos/common-resources-public/ensembl_data_cache/37",
            refGenomeFasta = "/Users/khalid/biodata/reference_genome/37/Homo_sapiens.GRCh37.GATK.illumina.fasta",
            refGenomeVersion = "37",
            tempDir = "/Users/khalid/hmf/tmp",
        )

        val paver = Paver(paverConfig)
        val queries = listOf(
            PaveQuery(
                id = "1",
                gene = "MTOR",
                chromosome = "1",
                position = 11169361,
                ref = "C",
                alt = "G",
                mutation = "p.R250SP"
            ),
            PaveQuery(
                id = "2",
                gene = "MPL",
                chromosome = "1",
                position = 43814978,
                ref = "A",
                alt = "T",
                mutation = "p.S505C"
            )
        )
        val response = paver.pave(queries)
        assertThat(response.size).isEqualTo(2)
        println(response)
    }
}