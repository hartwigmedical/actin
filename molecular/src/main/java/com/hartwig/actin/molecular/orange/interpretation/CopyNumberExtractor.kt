package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.ImmutableCopyNumber
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleRecord

internal class CopyNumberExtractor(private val geneFilter: GeneFilter, private val evidenceDatabase: EvidenceDatabase) {
    fun extract(purple: PurpleRecord): MutableSet<CopyNumber> {
        val copyNumbers: MutableSet<CopyNumber> = Sets.newTreeSet(CopyNumberComparator())
        val drivers: MutableSet<PurpleDriver> = VariantExtractor.relevantPurpleDrivers(purple)
        for (gainLoss in purple.allSomaticGainsLosses()) {
            val driver = findCopyNumberDriver(drivers, gainLoss.gene())
            val event = DriverEventFactory.gainLossEvent(gainLoss)
            if (geneFilter.include(gainLoss.gene())) {
                copyNumbers.add(ImmutableCopyNumber.builder()
                    .from(GeneAlterationFactory.convertAlteration(gainLoss.gene(),
                        evidenceDatabase.geneAlterationForCopyNumber(gainLoss)))
                    .isReportable(driver != null)
                    .event(event)
                    .driverLikelihood(if (driver != null) DriverLikelihood.HIGH else null)
                    .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(gainLoss)))
                    .type(determineType(gainLoss.interpretation()))
                    .minCopies(Math.round(gainLoss.minCopies()).toInt())
                    .maxCopies(Math.round(gainLoss.maxCopies()).toInt())
                    .build())
            } else check(driver == null) {
                ("Filtered a reported copy number through gene filtering: '" + event + "'. Please make sure '" + gainLoss.gene()
                        + "' is configured as a known gene.")
            }
        }
        return copyNumbers
    }

    companion object {
        private val AMP_DRIVERS: Set<PurpleDriverType> = setOf(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP)
        private val DEL_DRIVERS: Set<PurpleDriverType> = setOf(PurpleDriverType.DEL)

        @VisibleForTesting
        fun determineType(interpretation: CopyNumberInterpretation): CopyNumberType {
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
            for (driver in drivers) {
                if ((DEL_DRIVERS.contains(driver.driver()) || AMP_DRIVERS.contains(driver.driver())) && driver.gene() == geneToFind) {
                    return driver
                }
            }
            return null
        }
    }
}
