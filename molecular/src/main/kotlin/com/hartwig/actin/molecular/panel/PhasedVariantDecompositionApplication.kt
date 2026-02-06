package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.paver.PaveRefGenomeVersion
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.ensemblcache.EnsemblDataLoader
import com.hartwig.actin.tools.transvar.TransvarVariantAnnotatorFactory
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess
import com.hartwig.actin.tools.ensemblcache.RefGenome as EnsemblRefGenome

class PhasedVariantDecompositionApplication(private val config: PhasedVariantDecompositionApplicationConfig) {

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
        val decompositions = if (config.decomposedVariants.isEmpty()) {
            EMPTY_VARIANT_DECOMPOSITION_TABLE
        } else {
            VariantDecompositionTable(
                listOf(
                    VariantDecomposition(
                        gene = config.gene,
                        transcript = config.transcript,
                        originalCodingHgvs = config.originalVariant.trim(),
                        decomposedCodingHgvs = config.decomposedVariants.map(String::trim)
                    )
                )
            )
        }
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

private val LOGGER: Logger = LogManager.getLogger(PhasedVariantDecompositionApplication::class.java)
private const val APPLICATION = "Phased Variant Decomposition"

fun main(args: Array<String>) {
    val options = PhasedVariantDecompositionApplicationConfig.createOptions()
    val config: PhasedVariantDecompositionApplicationConfig
    try {
        config = PhasedVariantDecompositionApplicationConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: Exception) {
        LOGGER.warn(exception.message)
        HelpFormatter().printHelp(APPLICATION, options)
        exitProcess(1)
    }

    PhasedVariantDecompositionApplication(config).run()
}
