package com.hartwig.actin.molecular

import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

data class MolecularInterpreterConfig(
    val clinicalJson: String,
    val orangeJson: String?,
    val serveDirectory: String,
    val doidJson: String,
    val oncoDndsDatabasePath: String,
    val tsgDndsDatabasePath: String,
    val referenceGenomeFastaPath: String,
    val ensemblCachePath: String,
    val driverGenePanelPath: String,
    val knownFusionsPath: String,
    val tempDir: String,
    val outputDirectory: String
) {

    companion object {
        fun createOptions(): Options {
            val options = Options()
            options.addOption(CLINICAL_JSON, true, "The clinical JSON of the patient for which a sample is analyzed")
            options.addOption(ORANGE_JSON, true, "(Optional) path of the ORANGE json to be interpreted")
            options.addOption(SERVE_DIRECTORY, true, "Path towards the SERVE directory containing known and actionable events")
            options.addOption(DOID_JSON, true, "Path to JSON file containing the full DOID tree.")
            options.addOption(ONCO_DNDS_DATABASE_PATH, true, "Path to DNDS values for ONCO genes")
            options.addOption(TSG_DNDS_DATABASE_PATH, true, "Path to DNDS values for TSG genes")
            options.addOption(REFERENCE_GENOME_FASTA_PATH, true, "Path to reference genome fasta file")
            options.addOption(ENSEMBL_CACHE_PATH, true, "Path to ensemble data cache directory")
            options.addOption(DRIVER_GENE_PANEL_PATH, true, "Path to driver gene panel file")
            options.addOption(KNOWN_FUSIONS_PATH, true, "Path to file containing known fusion reference data")
            options.addOption(TEMP_DIR, false, "if set, path to temp dir to use for intermediate files, otherwise system temp dir is used")
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where molecular data output will be written to")
            options.addOption(LOG_DEBUG, false, "If set, debug logging gets enabled")
            return options
        }

        fun createConfig(cmd: CommandLine): MolecularInterpreterConfig {
            if (cmd.hasOption(LOG_DEBUG)) {
                Configurator.setRootLevel(Level.DEBUG)
                LOGGER.debug("Switched root level logging to DEBUG")
            }
            return MolecularInterpreterConfig(
                clinicalJson = ApplicationConfig.nonOptionalFile(cmd, CLINICAL_JSON),
                orangeJson = ApplicationConfig.optionalFile(cmd, ORANGE_JSON),
                serveDirectory = ApplicationConfig.nonOptionalDir(cmd, SERVE_DIRECTORY),
                doidJson = ApplicationConfig.nonOptionalFile(cmd, DOID_JSON),
                oncoDndsDatabasePath = ApplicationConfig.nonOptionalFile(cmd, ONCO_DNDS_DATABASE_PATH),
                tsgDndsDatabasePath = ApplicationConfig.nonOptionalFile(cmd, TSG_DNDS_DATABASE_PATH),
                referenceGenomeFastaPath = ApplicationConfig.nonOptionalFile(cmd, REFERENCE_GENOME_FASTA_PATH),
                ensemblCachePath = ApplicationConfig.nonOptionalDir(cmd, ENSEMBL_CACHE_PATH),
                driverGenePanelPath = ApplicationConfig.nonOptionalFile(cmd, DRIVER_GENE_PANEL_PATH),
                knownFusionsPath = ApplicationConfig.nonOptionalFile(cmd, KNOWN_FUSIONS_PATH),
                tempDir = ApplicationConfig.optionalDir(cmd, TEMP_DIR) ?: System.getProperty("java.io.tmpdir"),
                outputDirectory = ApplicationConfig.nonOptionalDir(cmd, OUTPUT_DIRECTORY)
            )
        }

        private val LOGGER: Logger = LogManager.getLogger(MolecularInterpreterConfig::class.java)

        private const val CLINICAL_JSON: String = "clinical_json"
        private const val ORANGE_JSON: String = "orange_json"
        private const val SERVE_DIRECTORY: String = "serve_directory"
        private const val DOID_JSON: String = "doid_json"
        private const val ONCO_DNDS_DATABASE_PATH: String = "onco_dnds_database_path"
        private const val TSG_DNDS_DATABASE_PATH: String = "tsg_dnds_database_path"
        private const val REFERENCE_GENOME_FASTA_PATH = "ref_genome_fasta_file"
        private const val ENSEMBL_CACHE_PATH = "ensembl_data_dir"
        private const val DRIVER_GENE_PANEL_PATH = "driver_gene_panel"
        private const val KNOWN_FUSIONS_PATH = "known_fusions_file"
        private const val TEMP_DIR: String = "temp"
        private const val OUTPUT_DIRECTORY: String = "output_directory"
        private const val LOG_DEBUG: String = "log_debug"
    }
}
