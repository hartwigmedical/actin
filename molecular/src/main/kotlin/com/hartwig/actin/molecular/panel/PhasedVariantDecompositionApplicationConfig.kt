package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options


data class PhasedVariantDecompositionApplicationConfig(
    val gene: String,
    val transcript: String?,
    val originalVariant: String,
    val decomposedVariants: List<String>,
    val refGenomeVersion: RefGenomeVersion,
    val referenceGenomeFastaPath: String,
    val ensemblCachePath: String,
    val driverGenePanelPath: String,
    val tempDir: String,
) {

    companion object {
        private const val GENE = "gene"
        private const val TRANSCRIPT = "transcript"
        private const val ORIGINAL_CODING_HGVS = "original_coding_hgvs"
        private const val DECOMPOSED_CODING_HGVS = "decomposed_coding_hgvs"
        private const val REF_GENOME_VERSION = "ref_genome_version"
        private const val REF_GENOME_FASTA_PATH = "ref_genome_fasta_file"
        private const val ENSEMBL_CACHE_PATH = "ensembl_data_dir"
        private const val DRIVER_GENE_PANEL_PATH = "driver_gene_panel"
        private const val TEMP_DIR = "temp_dir"

        fun createOptions(): Options {
            val options = Options()
            options.addOption(GENE, true, "Gene symbol for the variant(s)")
            options.addOption(TRANSCRIPT, true, "Transcript for the variant(s)")
            options.addOption(ORIGINAL_CODING_HGVS, true, "Original HGVS coding impact")
            options.addOption(DECOMPOSED_CODING_HGVS, true, "Decomposed HGVS coding impacts (comma-separated)")
            options.addOption(REF_GENOME_VERSION, true, "Reference genome version (V37 or V38)")
            options.addOption(REF_GENOME_FASTA_PATH, true, "Path to reference genome fasta file")
            options.addOption(ENSEMBL_CACHE_PATH, true, "Path to ensembl data cache directory")
            options.addOption(DRIVER_GENE_PANEL_PATH, true, "Path to driver gene panel file")
            options.addOption(TEMP_DIR, true, "Optional path to temp dir for intermediate files")
            return options
        }

        fun createConfig(cmd: CommandLine): PhasedVariantDecompositionApplicationConfig {
            val decomposedVariants = ApplicationConfig.optionalValue(cmd, DECOMPOSED_CODING_HGVS)
                ?.split(",")
                ?.map(String::trim)
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            val refGenomeVersion = toRefGenomeVersion(ApplicationConfig.nonOptionalValue(cmd, REF_GENOME_VERSION))

            return PhasedVariantDecompositionApplicationConfig(
                gene = ApplicationConfig.nonOptionalValue(cmd, GENE),
                transcript = ApplicationConfig.optionalValue(cmd, TRANSCRIPT),
                originalVariant = ApplicationConfig.nonOptionalValue(cmd, ORIGINAL_CODING_HGVS),
                decomposedVariants = decomposedVariants,
                refGenomeVersion = refGenomeVersion,
                referenceGenomeFastaPath = ApplicationConfig.nonOptionalFile(cmd, REF_GENOME_FASTA_PATH),
                ensemblCachePath = ApplicationConfig.nonOptionalDir(cmd, ENSEMBL_CACHE_PATH),
                driverGenePanelPath = ApplicationConfig.nonOptionalFile(cmd, DRIVER_GENE_PANEL_PATH),
                tempDir = ApplicationConfig.optionalDir(cmd, TEMP_DIR) ?: System.getProperty("java.io.tmpdir"),
            )
        }

        private fun toRefGenomeVersion(refGenomeVersion: String): RefGenomeVersion {
            return try {
                RefGenomeVersion.valueOf(refGenomeVersion.uppercase())
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid ref genome version '$refGenomeVersion', expected one of ${RefGenomeVersion.entries}")
            }
        }
    }
}
