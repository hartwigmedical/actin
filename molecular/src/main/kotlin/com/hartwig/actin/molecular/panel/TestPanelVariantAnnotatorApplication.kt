package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.paver.PaveRefGenomeVersion
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.ensemblcache.EnsemblDataLoader
import com.hartwig.actin.tools.transvar.TransvarVariantAnnotatorFactory
import com.hartwig.actin.util.ApplicationConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess
import com.hartwig.actin.tools.ensemblcache.RefGenome as EnsemblRefGenome

data class TestPanelVariantAnnotatorConfig(
    val gene: String,
    val transcript: String?,
    val originalVariant: String,
    val decomposedVariants: List<String>,
    val refGenomeVersion: RefGenomeVersion,
    val referenceGenomeFastaPath: String,
    val ensemblCachePath: String,
    val driverGenePanelPath: String,
    val tempDir: String,
)

class TestPanelVariantAnnotatorApplication(private val config: TestPanelVariantAnnotatorConfig) {

    fun run() {
        val ensemblDataCache = EnsemblDataLoader.load(
            config.ensemblCachePath,
            toEnsemblRefGenomeVersion(config.refGenomeVersion)
        )
        val variantAnnotator =
            TransvarVariantAnnotatorFactory.withRefGenome(
                toEnsemblRefGenomeVersion(config.refGenomeVersion),
                config.referenceGenomeFastaPath,
                ensemblDataCache
            )
        val paver = Paver(
            config.ensemblCachePath,
            config.referenceGenomeFastaPath,
            toPaveRefGenomeVersion(config.refGenomeVersion),
            config.driverGenePanelPath,
            config.tempDir
        )
        val decompositions = VariantDecompositionIndex(
            listOf(
                VariantDecomposition(
                    originalCodingHgvs = config.originalVariant.trim(),
                    decomposedCodingHgvs = config.decomposedVariants.map(String::trim)
                )
            )
        )
        val annotator = PanelVariantAnnotator(variantAnnotator, paver, decompositions)

        val sequencedVariants = buildSequencedVariants(
            config.gene,
            config.transcript,
            config.originalVariant
        )
        val annotated = annotator.annotate(sequencedVariants)

        val mapper = jacksonObjectMapper()
            .findAndRegisterModules()
        println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(annotated))
    }

    private fun buildSequencedVariants(
        gene: String,
        transcript: String?,
        originalVariant: String,
    ): Set<SequencedVariant> {
        val originalSequencedVariant = SequencedVariant(
            gene = gene,
            transcript = transcript,
            hgvsCodingImpact = originalVariant
        )
        return setOf(originalSequencedVariant)
    }
}

private fun toEnsemblRefGenomeVersion(refGenomeVersion: RefGenomeVersion): EnsemblRefGenome {
    return when (refGenomeVersion) {
        RefGenomeVersion.V37 -> {
            EnsemblRefGenome.V37
        }

        RefGenomeVersion.V38 -> {
            EnsemblRefGenome.V38
        }
    }
}

private fun toPaveRefGenomeVersion(refGenomeVersion: RefGenomeVersion): PaveRefGenomeVersion {
    return when (refGenomeVersion) {
        RefGenomeVersion.V37 -> {
            PaveRefGenomeVersion.V37
        }

        RefGenomeVersion.V38 -> {
            PaveRefGenomeVersion.V38
        }
    }
}

private fun toRefGenomeVersion(refGenomeVersion: String): RefGenomeVersion {
    return try {
        RefGenomeVersion.valueOf(refGenomeVersion.uppercase())
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid ref genome version '$refGenomeVersion', expected one of ${RefGenomeVersion.entries}")
    }
}

private const val GENE = "gene"
private const val TRANSCRIPT = "transcript"
private const val ORIGINAL_VARIANT = "original_variant"
private const val DECOMPOSED_VARIANT = "decomposed_variant"
private const val REF_GENOME_VERSION = "ref_genome_version"
private const val REF_GENOME_FASTA_PATH = "ref_genome_fasta_file"
private const val ENSEMBL_CACHE_PATH = "ensembl_data_dir"
private const val DRIVER_GENE_PANEL_PATH = "driver_gene_panel"
private const val TEMP_DIR = "temp_dir"

private fun createOptions(): Options {
    val options = Options()
    options.addOption(GENE, true, "Gene symbol for the variant(s)")
    options.addOption(TRANSCRIPT, true, "Transcript for the variant(s)")
    options.addOption(ORIGINAL_VARIANT, true, "Original variant HGVS coding impact")
    options.addOption(DECOMPOSED_VARIANT, true, "Decomposed variant HGVS coding impact (can be specified multiple times)")
    options.addOption(REF_GENOME_VERSION, true, "Reference genome version (V37 or V38)")
    options.addOption(REF_GENOME_FASTA_PATH, true, "Path to reference genome fasta file")
    options.addOption(ENSEMBL_CACHE_PATH, true, "Path to ensembl data cache directory")
    options.addOption(DRIVER_GENE_PANEL_PATH, true, "Path to driver gene panel file")
    options.addOption(TEMP_DIR, true, "Optional path to temp dir for intermediate files")
    return options
}

private fun createConfig(cmd: CommandLine): TestPanelVariantAnnotatorConfig {
    val decomposedVariants = cmd.getOptionValues(DECOMPOSED_VARIANT)?.toList() ?: emptyList()
    if (decomposedVariants.isEmpty()) {
        throw IllegalArgumentException("At least one --$DECOMPOSED_VARIANT must be provided")
    }

    val refGenomeVersion = toRefGenomeVersion(ApplicationConfig.nonOptionalValue(cmd, REF_GENOME_VERSION))

    return TestPanelVariantAnnotatorConfig(
        gene = ApplicationConfig.nonOptionalValue(cmd, GENE),
        transcript = ApplicationConfig.optionalValue(cmd, TRANSCRIPT),
        originalVariant = ApplicationConfig.nonOptionalValue(cmd, ORIGINAL_VARIANT),
        decomposedVariants = decomposedVariants,
        refGenomeVersion = refGenomeVersion,
        referenceGenomeFastaPath = ApplicationConfig.nonOptionalFile(cmd, REF_GENOME_FASTA_PATH),
        ensemblCachePath = ApplicationConfig.nonOptionalDir(cmd, ENSEMBL_CACHE_PATH),
        driverGenePanelPath = ApplicationConfig.nonOptionalFile(cmd, DRIVER_GENE_PANEL_PATH),
        tempDir = ApplicationConfig.optionalDir(cmd, TEMP_DIR) ?: System.getProperty("java.io.tmpdir"),
    )
}

private val LOGGER: Logger = LogManager.getLogger(TestPanelVariantAnnotatorApplication::class.java)
private const val APPLICATION = "Test Panel Variant Annotator"

fun main(args: Array<String>) {
    val options: Options = createOptions()
    val config: TestPanelVariantAnnotatorConfig
    try {
        config = createConfig(DefaultParser().parse(options, args))
    } catch (exception: Exception) {
        LOGGER.warn(exception.message)
        HelpFormatter().printHelp(APPLICATION, options)
        exitProcess(1)
    }

    TestPanelVariantAnnotatorApplication(config).run()
}
