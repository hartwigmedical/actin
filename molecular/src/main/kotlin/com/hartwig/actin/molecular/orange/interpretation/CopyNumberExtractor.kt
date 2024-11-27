package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.sort.driver.CopyNumberComparator
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleRecord

internal class CopyNumberExtractor(private val geneFilter: GeneFilter) {

    fun extract(purple: PurpleRecord): Set<CopyNumber> {
        val drivers = DriverExtractor.relevantPurpleDrivers(purple)
        return purple.allSomaticGeneCopyNumbers()
            .map { geneCopyNumber ->
                Pair(geneCopyNumber, findCopyNumberDriver(drivers, geneCopyNumber.gene()))
            }
            .filter { (geneCopyNumber, driver) ->
                val geneIncluded = geneFilter.include(geneCopyNumber.gene())
                if (!geneIncluded && driver != null) {
                    throw IllegalStateException(
                        "Filtered a reported copy number through gene filtering: ${driver.gene()}."
                                + " Please make sure ${geneCopyNumber.gene()} is configured as a known gene."
                    )
                }
                geneIncluded
            }
            .map { (geneCopyNumber, driver) ->
                if (driver != null) {
                    val gainLoss = findGainLoss(purple.allSomaticGainsLosses(), geneCopyNumber.gene())
                    val event = DriverEventFactory.gainLossEvent(gainLoss)
                    CopyNumber(
                        gene = gainLoss.gene(),
                        geneRole = GeneRole.UNKNOWN,
                        proteinEffect = ProteinEffect.UNKNOWN,
                        isAssociatedWithDrugResistance = null,
                        isReportable = true,
                        event = event,
                        driverLikelihood = DriverLikelihood.HIGH,
                        evidence = ClinicalEvidenceFactory.createNoEvidence(),
                        type = determineType(gainLoss.interpretation()),
                        minCopies = Math.round(gainLoss.minCopies()).toInt(),
                        maxCopies = Math.round(gainLoss.maxCopies()).toInt()
                    )
                } else {
                    val gene = geneCopyNumber.gene()
                    val event = DriverEventFactory.geneCopyNumberEvent(geneCopyNumber)
                    CopyNumber(
                        gene = gene,
                        geneRole = GeneRole.UNKNOWN,
                        proteinEffect = ProteinEffect.UNKNOWN,
                        isAssociatedWithDrugResistance = null,
                        isReportable = false,
                        event = event,
                        driverLikelihood = null,
                        evidence = ClinicalEvidenceFactory.createNoEvidence(),
                        type = CopyNumberType.NONE,
                        minCopies = Math.round(geneCopyNumber.minCopyNumber()).toInt(),
                        maxCopies = Math.round(geneCopyNumber.maxCopyNumber()).toInt()
                    )
                }
            }.toSortedSet(CopyNumberComparator())
    }

    companion object {
        private val AMP_DRIVERS = setOf(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP)
        private val DEL_DRIVERS = setOf(PurpleDriverType.DEL)

        internal fun determineType(interpretation: CopyNumberInterpretation): CopyNumberType {
            return when (interpretation) {
                CopyNumberInterpretation.FULL_GAIN -> {
                    CopyNumberType.FULL_GAIN
                }

                CopyNumberInterpretation.PARTIAL_GAIN -> {
                    CopyNumberType.PARTIAL_GAIN
                }

                CopyNumberInterpretation.FULL_LOSS, CopyNumberInterpretation.PARTIAL_LOSS -> {
                    CopyNumberType.LOSS
                }

                else -> {
                    throw IllegalStateException("Could not determine copy number type for purple interpretation: $interpretation")
                }
            }
        }

        private fun findCopyNumberDriver(drivers: Set<PurpleDriver>, geneToFind: String): PurpleDriver? {
            return drivers.find { driver ->
                (DEL_DRIVERS.contains(driver.type()) || AMP_DRIVERS.contains(driver.type())) &&
                        driver.gene() == geneToFind && driver.isCanonical
            }
        }

        private fun findGainLoss(gainsLosses: List<PurpleGainLoss>, geneToFind: String): PurpleGainLoss {
            val gainLoss = gainsLosses.find { gainLoss ->
                (gainLoss.gene() == geneToFind && gainLoss.isCanonical)
            }
            if (gainLoss != null) {
                return gainLoss
            } else {
                throw IllegalStateException(
                    "Copy number driver found but could not find corresponding PurpleGainLoss for gene : '$geneToFind'."
                )
            }
        }
    }
}
