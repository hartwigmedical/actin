package com.hartwig.actin.molecular.hotspotcomparison

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.panel.isHotspot
import com.hartwig.actin.util.Paths
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.serialization.ServeJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedWriter
import java.io.FileWriter
import kotlin.system.exitProcess
import com.hartwig.serve.datamodel.RefGenome as ServeRefGenome

class HotspotComparisonApplication(private val config: HotspotComparisonConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        val serveJsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)
        LOGGER.info("Loading SERVE database from {}", serveJsonFilePath)
        val serveDatabase = ServeLoader.loadServeDatabase(serveJsonFilePath)
        LOGGER.info("Loaded evidence and known events from SERVE version {}", serveDatabase.version())

        val orange = OrangeJson.getInstance().read(config.orangeJson)
        val serveRecord = selectForRefGenomeVersion(serveDatabase, fromOrangeRefGenomeVersion(orange.refGenomeVersion()))

        val hotspots = orange.purple().allSomaticVariants().mapNotNull { variant ->
            val criteria = createVariantCriteria(variant)
            val knownEventResolver = KnownEventResolverFactory.create(serveRecord.knownEvents())
            val serveGeneAlteration = knownEventResolver.resolveForVariant(criteria)
            val isHotspotServe = isHotspot(serveGeneAlteration)
            val isHotspotOrange = variant.hotspot() == HotspotType.HOTSPOT
            if (isHotspotServe || isHotspotOrange) {
                AnnotatedHotspot(
                    gene = variant.gene(),
                    chromosome = variant.chromosome(),
                    position = variant.position(),
                    ref = variant.ref(),
                    alt = variant.alt(),
                    codingImpact = variant.canonicalImpact().hgvsCodingImpact(),
                    proteinImpact = variant.canonicalImpact().hgvsProteinImpact(),
                    isHotspotOrange = isHotspotOrange,
                    isHotspotServe = isHotspotServe,
                )
            } else {
                null
            }
        }

        LOGGER.info("Hotspot comparison DONE!")
        LOGGER.info(
            "{} hotspot(s) according to ORANGE. {} hotspot(s) according to SERVE. {} hotspot(s) different",
            hotspots.count { it.isHotspotOrange },
            hotspots.count { it.isHotspotServe },
            hotspots.count { !(it.isHotspotOrange && it.isHotspotServe) }
        )
        write(config.outputDirectory, orange.sampleId(), hotspots)
    }

    private fun createVariantCriteria(variant: PurpleVariant) =
        VariantMatchCriteria(
            gene = variant.gene(),
            chromosome = variant.chromosome(),
            position = variant.position(),
            ref = variant.ref(),
            alt = variant.alt(),
            type = null,
            codingEffect = null,
            isReportable = variant.reported()
        )

    private fun write(directory: String, sampleId: String, hotspots: List<AnnotatedHotspot>) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val file = "$path$sampleId.hotspotComparison"
        LOGGER.info("Writing hotspot comparison to {}", file)
        BufferedWriter(FileWriter(file)).use { writer ->
            writer.write("gene\tchromosome\tposition\tref\talt\tcodingImpact\tproteinImpact\tisHotspotOrange\tisHotspotServe\n")
            hotspots.forEach { writer.write(toTabSeparatedString(it) + "\n") }
        }
    }

    private fun toTabSeparatedString(hotspot: AnnotatedHotspot): String {
        return listOf(
            hotspot.gene,
            hotspot.chromosome,
            hotspot.position,
            hotspot.ref,
            hotspot.alt,
            hotspot.codingImpact,
            hotspot.proteinImpact,
            hotspot.isHotspotOrange,
            hotspot.isHotspotServe
        ).joinToString("\t")
    }

    private fun selectForRefGenomeVersion(serveDatabase: ServeDatabase, refGenomeVersion: RefGenomeVersion): ServeRecord {
        return serveDatabase.records()[toServeRefGenomeVersion(refGenomeVersion)]
            ?: throw IllegalStateException("No serve record for ref genome version $refGenomeVersion")
    }

    private fun fromOrangeRefGenomeVersion(orangeRefGenomeVersion: OrangeRefGenomeVersion): RefGenomeVersion {
        return when (orangeRefGenomeVersion) {
            OrangeRefGenomeVersion.V37 -> {
                RefGenomeVersion.V37
            }

            OrangeRefGenomeVersion.V38 -> {
                RefGenomeVersion.V38
            }
        }
    }

    private fun toServeRefGenomeVersion(refGenomeVersion: RefGenomeVersion): ServeRefGenome {
        return when (refGenomeVersion) {
            RefGenomeVersion.V37 -> {
                ServeRefGenome.V37
            }

            RefGenomeVersion.V38 -> {
                ServeRefGenome.V38
            }
        }
    }

    companion object {
        const val APPLICATION: String = "ACTIN Hotspot Comparator"

        val LOGGER: Logger = LogManager.getLogger(HotspotComparisonApplication::class.java)
        private val VERSION = HotspotComparisonApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = HotspotComparisonConfig.createOptions()

    try {
        val config = HotspotComparisonConfig.createConfig(DefaultParser().parse(options, args))
        HotspotComparisonApplication(config).run()
    } catch (exception: ParseException) {
        HotspotComparisonApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(HotspotComparisonApplication.APPLICATION, options)
        exitProcess(1)
    }
}