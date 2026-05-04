package com.hartwig.actin.molecular.cancerassociatedvariantcomparison

import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.util.Paths
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.serialization.ServeJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.BufferedWriter
import java.io.FileWriter
import kotlin.system.exitProcess
import com.hartwig.serve.datamodel.RefGenome as ServeRefGenome

class CancerAssociatedVariantComparisonApplication(private val config: CancerAssociatedVariantComparisonConfig) {

    fun run() {
        logger.info { "Running $APPLICATION v$VERSION" }

        val serveJsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)
        logger.info { "Loading SERVE database from $serveJsonFilePath" }
        val serveDatabase = ServeLoader.loadServeDatabase(serveJsonFilePath, false)
        logger.info { "Loaded evidence and known events from SERVE version ${serveDatabase.version()}" }

        val orange = OrangeJson.getInstance().read(config.orangeJson)
        val serveRecord = selectForRefGenomeVersion(serveDatabase, orange.refGenomeVersion())
        val cancerAssociatedVariants = CancerAssociatedVariantEvaluator.annotateCancerAssociatedVariants(orange, serveRecord)

        logger.info { "Cancer-associated variant comparison DONE!" }
        logger.info { "${cancerAssociatedVariants.count { it.isCancerAssociatedVariantOrange }} CAV(s) according to ORANGE. ${cancerAssociatedVariants.count { it.isCancerAssociatedVariantServe }} CAV(s) according to SERVE. ${cancerAssociatedVariants.count { !(it.isCancerAssociatedVariantOrange && it.isCancerAssociatedVariantServe) }} CAV(s) different" }
        write(config.outputDirectory, orange.sampleId(), cancerAssociatedVariants)
    }

    private fun write(directory: String, sampleId: String, cancerAssociatedVariants: List<AnnotatedCancerAssociatedVariant>) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val file = "$path$sampleId.cancerAssociatedVariantComparison"
        logger.info { "Writing cancer-associated variant comparison to $file" }
        BufferedWriter(FileWriter(file)).use { writer ->
            writer.write("gene\tchromosome\tposition\tref\talt\tcodingImpact\tproteinImpact\tisCavOrange\tisCavServe\n")
            cancerAssociatedVariants.forEach { writer.write(toTabSeparatedString(it) + "\n") }
        }
    }

    private fun toTabSeparatedString(cancerAssociatedVariant: AnnotatedCancerAssociatedVariant): String {
        return listOf(
            cancerAssociatedVariant.gene,
            cancerAssociatedVariant.chromosome,
            cancerAssociatedVariant.position,
            cancerAssociatedVariant.ref,
            cancerAssociatedVariant.alt,
            cancerAssociatedVariant.codingImpact,
            cancerAssociatedVariant.proteinImpact,
            cancerAssociatedVariant.isCancerAssociatedVariantOrange,
            cancerAssociatedVariant.isCancerAssociatedVariantServe
        ).joinToString("\t")
    }

    private fun selectForRefGenomeVersion(serveDatabase: ServeDatabase, orangeRefGenomeVersion: OrangeRefGenomeVersion): ServeRecord {
        return serveDatabase.records()[toServeRefGenomeVersion(orangeRefGenomeVersion)]
            ?: throw IllegalStateException("No serve record for orange ref genome version $orangeRefGenomeVersion")
    }

    private fun toServeRefGenomeVersion(orangeRefGenomeVersion: OrangeRefGenomeVersion): ServeRefGenome {
        return when (orangeRefGenomeVersion) {
            OrangeRefGenomeVersion.V37 -> {
                ServeRefGenome.V37
            }

            OrangeRefGenomeVersion.V38 -> {
                ServeRefGenome.V38
            }
        }
    }

    companion object {
        const val APPLICATION: String = "ACTIN Cancer Associated Variant Comparator"

        val logger = KotlinLogging.logger {}
        private val VERSION =
            CancerAssociatedVariantComparisonApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = CancerAssociatedVariantComparisonConfig.createOptions()

    try {
        val config = CancerAssociatedVariantComparisonConfig.createConfig(DefaultParser().parse(options, args))
        CancerAssociatedVariantComparisonApplication(config).run()
    } catch (exception: ParseException) {
        CancerAssociatedVariantComparisonApplication.logger.warn(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(CancerAssociatedVariantComparisonApplication.APPLICATION, options)
        exitProcess(1)
    }
}