package com.hartwig.actin.molecular.hotspotcomparison

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
        val serveRecord = selectForRefGenomeVersion(serveDatabase, orange.refGenomeVersion())
        val hotspots = HotspotComparator.annotateHotspots(orange, serveRecord)

        LOGGER.info("Hotspot comparison DONE!")
        LOGGER.info(
            "{} hotspot(s) according to ORANGE. {} hotspot(s) according to SERVE. {} hotspot(s) different",
            hotspots.count { it.isHotspotOrange },
            hotspots.count { it.isHotspotServe },
            hotspots.count { !(it.isHotspotOrange && it.isHotspotServe) }
        )
        write(config.outputDirectory, orange.sampleId(), hotspots)
    }

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