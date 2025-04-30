package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleGainDeletion
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import kotlin.math.roundToInt

private val AMP_DRIVERS = setOf(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP)
private val DEL_DRIVERS = setOf(PurpleDriverType.DEL)

class CopyNumberExtractor(private val geneFilter: GeneFilter) {

    fun extract(purple: PurpleRecord): List<CopyNumber> {
        val drivers = DriverExtractor.relevantPurpleDrivers(purple)
        return purple.allSomaticGeneCopyNumbers()
            .asSequence()
            .distinctBy { it.gene() } // TODO (CB): Should be changed once PurpleGeneCopyNumber has transcript / isCanonical fields
            .map { geneCopyNumber ->
                Pair(geneCopyNumber, findCopyNumberDrivers(drivers, geneCopyNumber.gene()))
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
                val otherGainDels =
                    drivers.filter { it != canonicalDriver }.map { driver -> findGainDel(purple.allSomaticGainsDels(), driver) }
                if (canonicalDriver != null) {
                    val canonicalGainDel = findGainDel(purple.allSomaticGainsDels(), canonicalDriver)
                    val event = DriverEventFactory.gainDelEvent(canonicalGainDel)
                    CopyNumber(
                        gene = geneCopyNumber.gene(),
                        geneRole = GeneRole.UNKNOWN,
                        proteinEffect = ProteinEffect.UNKNOWN,
                        isAssociatedWithDrugResistance = null,
                        isReportable = true,
                        event = event,
                        driverLikelihood = DriverLikelihood.HIGH,
                        evidence = ExtractionUtil.noEvidence(),
                        canonicalImpact = TranscriptCopyNumberImpact(
                            canonicalGainDel.transcript(),
                            determineType(canonicalGainDel.interpretation()),
                            canonicalGainDel.minCopies().roundToInt(),
                            canonicalGainDel.maxCopies().roundToInt()
                        ),
                        otherImpacts = otherGainDels.map { gainDel ->
                            TranscriptCopyNumberImpact(
                                gainDel.transcript(),
                                determineType(gainDel.interpretation()),
                                gainDel.minCopies().roundToInt(),
                                gainDel.maxCopies().roundToInt()
                            )
                        }.toSet(),
                    )
                } else {
                    val event =
                        if (otherGainDels.isEmpty()) DriverEventFactory.geneCopyNumberEvent(geneCopyNumber) else otherGainDels.first()
                            .let { DriverEventFactory.gainDelEvent(it) }
                    CopyNumber(
                        gene = geneCopyNumber.gene(),
                        geneRole = GeneRole.UNKNOWN,
                        proteinEffect = ProteinEffect.UNKNOWN,
                        isAssociatedWithDrugResistance = null,
                        isReportable = otherGainDels.isNotEmpty(),
                        event = event,
                        driverLikelihood = if (otherGainDels.isNotEmpty()) DriverLikelihood.HIGH else null,
                        evidence = ExtractionUtil.noEvidence(),
                        canonicalImpact = TranscriptCopyNumberImpact(
                            transcriptId = "",
                            type = CopyNumberType.NONE,
                            minCopies = geneCopyNumber.minCopyNumber()
                                .roundToInt(), // TODO (CB): Should be changed once PurpleGeneCopyNumber has transcript / isCanonical fields
                            maxCopies = geneCopyNumber.maxCopyNumber()
                                .roundToInt() // TODO (CB): Should be changed once PurpleGeneCopyNumber has transcript / isCanonical fields
                        ),
                        otherImpacts = otherGainDels.map { gainDel ->
                            TranscriptCopyNumberImpact(
                                gainDel.transcript(),
                                determineType(gainDel.interpretation()),
                                gainDel.minCopies().roundToInt(),
                                gainDel.maxCopies().roundToInt()
                            )
                        }.toSet()
                    )
                }
            }.sorted()
            .toList()
    }

    internal fun determineType(interpretation: CopyNumberInterpretation): CopyNumberType {
        return when (interpretation) {
            CopyNumberInterpretation.FULL_GAIN -> {
                CopyNumberType.FULL_GAIN
            }

            CopyNumberInterpretation.PARTIAL_GAIN -> {
                CopyNumberType.PARTIAL_GAIN
            }

            CopyNumberInterpretation.FULL_DEL, CopyNumberInterpretation.PARTIAL_DEL -> {
                CopyNumberType.DEL
            }

            else -> {
                throw IllegalStateException("Could not determine copy number type for purple interpretation: $interpretation")
            }
        }
    }

    private fun findCopyNumberDrivers(drivers: Set<PurpleDriver>, geneToFind: String): List<PurpleDriver> {
        return drivers.filter { driver ->
            driver.gene() == geneToFind && (DEL_DRIVERS.contains(driver.type()) || AMP_DRIVERS.contains(driver.type()))
        }
    }

    private fun findGainDel(gainsDeles: List<PurpleGainDeletion>, driverToMatch: PurpleDriver): PurpleGainDeletion {
        val gainDel =
            gainsDeles.firstOrNull { gainDel ->
                gainDel.gene() == driverToMatch.gene() && gainDel.isCanonical == driverToMatch.isCanonical
            }

        if (gainDel != null) {
            return gainDel
        } else {
            throw IllegalStateException(
                "Copy number driver found but could not find corresponding PurpleGainDeletion for driver : '$driverToMatch'."
            )
        }
    }
}