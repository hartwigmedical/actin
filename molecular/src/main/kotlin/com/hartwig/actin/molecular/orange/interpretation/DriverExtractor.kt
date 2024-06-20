package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import org.apache.logging.log4j.LogManager

internal class DriverExtractor private constructor(
    private val variantExtractor: VariantExtractor, private val copyNumberExtractor: CopyNumberExtractor,
    private val homozygousDisruptionExtractor: HomozygousDisruptionExtractor,
    private val disruptionExtractor: DisruptionExtractor, private val fusionExtractor: FusionExtractor,
    private val virusExtractor: VirusExtractor
) {

    fun extract(record: OrangeRecord): Drivers {
        val variants = variantExtractor.extract(record.purple())
        LOGGER.info(" Extracted {} variants of which {} reportable", variants.size, reportableCount(variants))

        val copyNumbers = copyNumberExtractor.extract(record.purple())
        LOGGER.info(" Extracted {} copy numbers of which {} reportable", copyNumbers.size, reportableCount(copyNumbers))

        val homozygousDisruptions = homozygousDisruptionExtractor.extractHomozygousDisruptions(record.linx())
        LOGGER.info(
            " Extracted {} homozygous disruptions of which {} reportable",
            homozygousDisruptions.size,
            reportableCount(homozygousDisruptions)
        )

        val disruptions =
            disruptionExtractor.extractDisruptions(record.linx(), reportableLostGenes(copyNumbers), record.linx().somaticDrivers())
        LOGGER.info(" Extracted {} disruptions of which {} reportable", disruptions.size, reportableCount(disruptions))

        val fusions = fusionExtractor.extract(record.linx())
        LOGGER.info(" Extracted {} fusions of which {} reportable", fusions.size, reportableCount(fusions))

        val virusInterpreter = record.virusInterpreter()
        val viruses = if (virusInterpreter != null) virusExtractor.extract(virusInterpreter) else emptySet()
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

        internal fun reportableLostGenes(copyNumbers: Iterable<CopyNumber>): Set<String> {
            return copyNumbers.filter { copyNumber -> copyNumber.isReportable && copyNumber.type.isLoss }
                .map(CopyNumber::gene)
                .toSet()
        }

        internal fun <T : Driver> reportableCount(drivers: Collection<T>): Int {
            return drivers.count(Driver::isReportable)
        }
    }
}
