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
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import java.util.*

internal class CopyNumberExtractor(private val geneFilter: GeneFilter, private val evidenceDatabase: EvidenceDatabase) {

    fun extractCopyNumbers(purple: PurpleRecord): SortedSet<CopyNumber> {
        val drivers = VariantExtractor.relevantPurpleDrivers(purple)
        return purple.allSomaticGeneCopyNumbers()
            .map { geneCopyNumber ->
                Pair(geneCopyNumber, findCopyNumberDriver(drivers, geneCopyNumber.gene()))
            }
            .filter { (geneCopyNumber, driver) ->
                val geneIncluded = geneFilter.include(geneCopyNumber.gene())
                if (!geneIncluded && driver != null) {
                    throw IllegalStateException(
                        "Filtered a reported copy number through gene filtering: ${driver}."
                                + " Please make sure ${geneCopyNumber.gene()} is configured as a known gene."
                    )
                }
                geneIncluded
            }
            .map { (geneCopyNumber, driver) ->
                if (driver != null) {
                    val gainLoss = findGainLoss(purple.allSomaticGainsLosses(), geneCopyNumber.gene())
                    val event = DriverEventFactory.gainLossEvent(gainLoss)
                    val alteration =
                        GeneAlterationFactory.convertAlteration(gainLoss.gene(), evidenceDatabase.geneAlterationForGainLoss(gainLoss))
                    CopyNumber(
                        gene = alteration.gene,
                        geneRole = alteration.geneRole,
                        proteinEffect = alteration.proteinEffect,
                        isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
                        isReportable = true,
                        event = event,
                        driverLikelihood = DriverLikelihood.HIGH,
                        evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(gainLoss))!!,
                        type = determineType(gainLoss.interpretation()),
                        minCopies = Math.round(gainLoss.minCopies()).toInt(),
                        maxCopies = Math.round(gainLoss.maxCopies()).toInt()
                    )
                } else {
                    val alteration =
                        GeneAlterationFactory.convertAlteration(geneCopyNumber.gene(),
                            evidenceDatabase.geneAlterationForGeneCopyNumber(geneCopyNumber))
                    val gene = alteration.gene
                    CopyNumber(
                        gene = alteration.gene,
                        geneRole = alteration.geneRole,
                        proteinEffect = alteration.proteinEffect,
                        isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
                        isReportable = false,
                        event = "$gene copy number",
                        driverLikelihood = null,
                        evidence = ActionableEvidenceFactory.createNoEvidence(),
                        type = CopyNumberType.NONE,
                        minCopies = Math.round(geneCopyNumber.minCopyNumber()).toInt(),
                        maxCopies = Math.round(geneCopyNumber.minCopyNumber())
                            .toInt() //TODO: maxCopies should be retrievable from ORANGE datamodel.
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
                (DEL_DRIVERS.contains(driver.type()) || AMP_DRIVERS.contains(driver.type())) && driver.gene() == geneToFind
            }
        }

        private fun findGainLoss(gainsLosses: MutableList<PurpleGainLoss>, geneToFind: String): PurpleGainLoss {
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
