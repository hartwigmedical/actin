package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import io.github.oshai.kotlinlogging.KotlinLogging

class DriverExtractor private constructor(
    private val variantExtractor: VariantExtractor,
    private val copyNumberExtractor: CopyNumberExtractor,
    private val homozygousDisruptionExtractor: HomozygousDisruptionExtractor,
    private val disruptionExtractor: DisruptionExtractor,
    private val fusionExtractor: FusionExtractor,
    private val virusExtractor: VirusExtractor
) {

    fun extract(record: OrangeRecord): Drivers {
        val variants = variantExtractor.extract(record.purple())
        logger.info { " Extracted ${variants.size} variants of which ${reportableCount(variants)} reportable" }

        val copyNumbers = copyNumberExtractor.extract(record.purple())
        logger.info { " Extracted ${copyNumbers.size} copy numbers of which ${reportableCount(copyNumbers)} reportable" }

        val homozygousDisruptions = homozygousDisruptionExtractor.extractHomozygousDisruptions(record.linx())
        logger.info { " Extracted ${homozygousDisruptions.size} homozygous disruptions of which ${reportableCount(homozygousDisruptions)} reportable" }

        val disruptions =
            disruptionExtractor.extractDisruptions(record.linx(), reportableLostGenes(copyNumbers), record.linx().somaticDrivers())
        logger.info { " Extracted ${disruptions.size} disruptions of which ${reportableCount(disruptions)} reportable" }

        val fusions = fusionExtractor.extract(record.linx())
        logger.info { " Extracted ${fusions.size} fusions of which ${reportableCount(fusions)} reportable" }

        val virusInterpreter = record.virusInterpreter()
        val viruses = if (virusInterpreter != null) virusExtractor.extract(virusInterpreter) else emptyList()
        logger.info { " Extracted ${viruses.size} viruses of which ${reportableCount(viruses)} reportable" }

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
        private val logger = KotlinLogging.logger {}

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

        fun relevantPurpleDrivers(purple: PurpleRecord): Set<PurpleDriver> {
            return listOfNotNull(purple.somaticDrivers(), purple.germlineDrivers()).flatten().toSet()
        }
    }
}