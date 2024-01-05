package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleRecord

internal class CopyNumberExtractor(private val geneFilter: GeneFilter, private val evidenceDatabase: EvidenceDatabase) {

    fun extract(purple: PurpleRecord): Set<CopyNumber> {
        val drivers = VariantExtractor.relevantPurpleDrivers(purple)
        return purple.allSomaticGainsLosses()
            .map { gainLoss ->
                Triple(gainLoss, findCopyNumberDriver(drivers, gainLoss.gene()), DriverEventFactory.gainLossEvent(gainLoss))
            }
            .filter { (gainLoss, driver, event) ->
                val geneIncluded = geneFilter.include(gainLoss.gene())
                if (!geneIncluded && driver != null) {
                    throw IllegalStateException(
                        "Filtered a reported copy number through gene filtering: '$event'."
                                + " Please make sure '${gainLoss.gene()}' is configured as a known gene."
                    )
                }
                geneIncluded
            }
            .map { (gainLoss, driver, event) ->
                val alteration = GeneAlterationFactory.convertAlteration(
                    gainLoss.gene(), evidenceDatabase.geneAlterationForCopyNumber(gainLoss)
                )
                CopyNumber(
                    gene = alteration.gene,
                    geneRole = alteration.geneRole,
                    proteinEffect = alteration.proteinEffect,
                    isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
                    isReportable = driver != null,
                    event = event,
                    driverLikelihood = if (driver != null) DriverLikelihood.HIGH else null,
                    evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(gainLoss))!!,
                    type = determineType(gainLoss.interpretation()),
                    minCopies = Math.round(gainLoss.minCopies()).toInt(),
                    maxCopies = Math.round(gainLoss.maxCopies()).toInt()
                )
            }
            .toSortedSet(CopyNumberComparator())
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
                (DEL_DRIVERS.contains(driver.type()) || AMP_DRIVERS.contains(driver.type())) && driver.gene() == geneToFind
            }
        }
    }
}
