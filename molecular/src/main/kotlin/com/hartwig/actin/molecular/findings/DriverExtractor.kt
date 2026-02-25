package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.finding.FindingRecord
import org.apache.logging.log4j.LogManager

class DriverExtractor private constructor(
    private val variantExtractor: VariantExtractor,
    private val copyNumberExtractor: CopyNumberExtractor,
    private val homozygousDisruptionExtractor: HomozygousDisruptionExtractor,
    private val disruptionExtractor: DisruptionExtractor,
    private val fusionExtractor: FusionExtractor,
    private val virusExtractor: VirusExtractor
) {

    fun extract(record: FindingRecord): Drivers {
        val variants = variantExtractor.extract(record.allSmallVariants())
        LOGGER.info(" Extracted {} variants of which {} reportable", variants.size, reportableCount(variants))

        val copyNumbers = copyNumberExtractor.extract(record.allGainDeletions())
        LOGGER.info(" Extracted {} copy numbers of which {} reportable", copyNumbers.size, reportableCount(copyNumbers))

        val homozygousDisruptions = homozygousDisruptionExtractor.extractHomozygousDisruptions(record.allDisruptions())
        LOGGER.info(
            " Extracted {} homozygous disruptions of which {} reportable",
            homozygousDisruptions.size,
            reportableCount(homozygousDisruptions)
        )

        val disruptions =
            disruptionExtractor.extractDisruptions(record.allDisruptions(), reportableLostGenes(copyNumbers), record.linx().somaticDrivers())
        LOGGER.info(" Extracted {} disruptions of which {} reportable", disruptions.size, reportableCount(disruptions))

        val fusions = fusionExtractor.extract(record.fusions)
        LOGGER.info(" Extracted {} fusions of which {} reportable", fusions.size, reportableCount(fusions))

        val viruses = virusExtractor.extract(record.viruses)
        LOGGER.info(" Extracted {} viruses of which {} reportable", viruses.size, reportableCount(viruses))

        return Drivers(
            variants = variants,
            copyNumbers = copyNumbers,
            homozygousDisruptions = homozygousDisruptions,
            disruptions = disruptions,
            fusions = fusions,
            viruses = viruses
        )
    }

    internal fun reportableLostGenes(copyNumbers: Iterable<CopyNumber>): Set<String> {
        return copyNumbers.filter { copyNumber ->
            copyNumber.isReportable &&
                    (copyNumber.canonicalImpact.type.isDeletion || copyNumber.otherImpacts.any { it.type.isDeletion })
        }
            .map(CopyNumber::gene)
            .toSet()
    }

    internal fun <T : Driver> reportableCount(drivers: Collection<T>): Int {
        return drivers.count(Driver::isReportable)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(DriverExtractor::class.java)

        fun create(geneFilter: GeneFilter): DriverExtractor {
            return DriverExtractor(
                VariantExtractor(geneFilter),
                CopyNumberExtractor(geneFilter),
                HomozygousDisruptionExtractor(geneFilter),
                DisruptionExtractor(geneFilter),
                FusionExtractor(geneFilter),
                VirusExtractor()
            )
        }
    }
}