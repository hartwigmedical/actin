package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.datamodel.molecular.sort.driver.CopyNumberComparator
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleRecord

private val AMP_DRIVERS = setOf(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP)
private val DEL_DRIVERS = setOf(PurpleDriverType.DEL)

internal class CopyNumberExtractor(private val geneFilter: GeneFilter) {

    fun extract(purple: PurpleRecord): List<CopyNumber> {
        val drivers = DriverExtractor.relevantPurpleDrivers(purple)
        return purple.allSomaticGeneCopyNumbers()
            .map { geneCopyNumber ->
                Pair(geneCopyNumber, findCopyNumberDriver(drivers, geneCopyNumber.gene()))
            }
            .filter { (geneCopyNumber, drivers) ->
                val geneIncluded = geneFilter.include(geneCopyNumber.gene())
                if (!geneIncluded && drivers.isNotEmpty()) {
                    throw IllegalStateException(
                        "Filtered a reported copy number through gene filtering: ${geneCopyNumber.gene()}."
                                + " Please make sure ${geneCopyNumber.gene()} is configured as a known gene."
                    )
                }
                geneIncluded
            }
            .map { (geneCopyNumber, drivers) ->
                val canonicalDriver = drivers.firstOrNull { it.isCanonical }
                val otherGainLosses =
                    drivers.filter { it != canonicalDriver }.map { driver -> findGainLoss(purple.allSomaticGainsLosses(), driver) }
                if (canonicalDriver != null) {
                    val canonicalGainLoss = findGainLoss(purple.allSomaticGainsLosses(), canonicalDriver)
                    val event = DriverEventFactory.gainLossEvent(canonicalGainLoss)
                    CopyNumber(
                        gene = geneCopyNumber.gene(),
                        geneRole = GeneRole.UNKNOWN,
                        proteinEffect = ProteinEffect.UNKNOWN,
                        isAssociatedWithDrugResistance = null,
                        isReportable = true,
                        event = event,
                        driverLikelihood = DriverLikelihood.HIGH,
                        evidence = ClinicalEvidenceFactory.createNoEvidence(),
                        canonicalImpact = TranscriptCopyNumberImpact(
                            canonicalGainLoss.transcript(),
                            determineType(canonicalGainLoss.interpretation()),
                            Math.round(canonicalGainLoss.minCopies()).toInt(),
                            Math.round(canonicalGainLoss.maxCopies()).toInt()
                        ),
                        otherImpacts = otherGainLosses.map { gainLoss ->
                            TranscriptCopyNumberImpact(
                                gainLoss.transcript(),
                                determineType(gainLoss.interpretation()),
                                Math.round(gainLoss.minCopies()).toInt(),
                                Math.round(gainLoss.maxCopies()).toInt()
                            )
                        }.toSet(),
                    )
                } else {
                    val event = DriverEventFactory.geneCopyNumberEvent(geneCopyNumber)
                    CopyNumber(
                        gene = geneCopyNumber.gene(),
                        geneRole = GeneRole.UNKNOWN,
                        proteinEffect = ProteinEffect.UNKNOWN,
                        isAssociatedWithDrugResistance = null,
                        isReportable = false, // Question for mr. Duyvesteyn: Is this correct when CDKN2A has no canonical impact but does have a non-canonical impact?
                        event = event,
                        driverLikelihood = null,
                        evidence = ClinicalEvidenceFactory.createNoEvidence(),
                        canonicalImpact = TranscriptCopyNumberImpact(
                            transcriptId = "",
                            type = CopyNumberType.NONE,
                            minCopies = Math.round(geneCopyNumber.minCopyNumber()).toInt(),
                            maxCopies = Math.round(geneCopyNumber.maxCopyNumber()).toInt()
                        ),
                        otherImpacts = otherGainLosses.map { gainLoss ->
                            TranscriptCopyNumberImpact(
                                gainLoss.transcript(),
                                determineType(gainLoss.interpretation()),
                                Math.round(gainLoss.minCopies()).toInt(),
                                Math.round(gainLoss.maxCopies()).toInt()
                            )
                        }.toSet()
                    )
                }
            }.sortedWith(CopyNumberComparator())
    }

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

    private fun findCopyNumberDriver(drivers: Set<PurpleDriver>, geneToFind: String): List<PurpleDriver> {
        return drivers.filter { driver ->
            driver.gene() == geneToFind && (DEL_DRIVERS.contains(driver.type()) || AMP_DRIVERS.contains(driver.type()))
        }
    }

    private fun findGainLoss(gainsLosses: List<PurpleGainLoss>, driverToMatch: PurpleDriver): PurpleGainLoss {
        val gainLoss =
            gainsLosses.firstOrNull { gainLoss ->
                gainLoss.gene() == driverToMatch.gene() && gainLoss.isCanonical == driverToMatch.isCanonical
            }

        if (gainLoss != null) {
            return gainLoss
        } else {
            throw IllegalStateException(
                "Copy number driver found but could not find corresponding PurpleGainLoss for driver : '$driverToMatch'."
            )
        }
    }
}