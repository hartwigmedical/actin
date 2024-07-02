package com.hartwig.actin.molecular.paver

import com.hartwig.hmftools.common.utils.config.ConfigBuilder
import com.hartwig.hmftools.pave.PaveApplication
import com.hartwig.hmftools.pave.PaveConfig
import htsjdk.tribble.AbstractFeatureReader
import htsjdk.tribble.readers.LineIterator
import htsjdk.variant.variantcontext.VariantContext
import htsjdk.variant.vcf.VCFCodec
import org.apache.logging.log4j.util.Strings

class Paver {
    fun pave() {
        println("Paving the way")
        val configBuilder: ConfigBuilder = ConfigBuilder("Pave")
        PaveConfig.addConfig(configBuilder)

        configBuilder.checkAndParseCommandLine(arrayOf(
            "-vcf_file", "/Users/khalid/hmf/tmp/actin-transvar.vcf.gz",
            "-ensembl_data_dir", "/Users/khalid/hmf/repos/common-resources-public/ensembl_data_cache/37",
            "-ref_genome", "/Users/khalid/biodata/reference_genome/37/Homo_sapiens.GRCh37.GATK.illumina.fasta",
            "-ref_genome_version", "37",
            "-output_dir", "/tmp/kz"
        ))

        val paveApplication = PaveApplication(configBuilder)
        paveApplication.run()
    }

    fun loadPaveVcf(paveVcfFile: String) {
        val reader =
            AbstractFeatureReader.getFeatureReader<VariantContext, LineIterator>(paveVcfFile, VCFCodec(), false)

        for (variant in reader) {
            val inputParts = variant.getAttributeAsString("input", Strings.EMPTY)
                .split("\\|".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()

            val inputGene = inputParts[0]
            println(inputGene)
        }
    }
}