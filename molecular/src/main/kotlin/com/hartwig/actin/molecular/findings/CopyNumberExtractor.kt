package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.finding.GainDeletion
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import kotlin.math.roundToInt

private val AMP_DRIVERS = setOf(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP)
private val DEL_DRIVERS = setOf(PurpleDriverType.DEL)

class CopyNumberExtractor(private val geneFilter: GeneFilter) {

    fun extract(gainDeletions: List<GainDeletion>): List<CopyNumber> {
        return gainDeletions
            .asSequence()
            .distinctBy { it.gene() } // TODO (CB): Should be changed once PurpleGeneCopyNumber has transcript / isCanonical fields
            .map { geneCopyNumber ->
                Pair(geneCopyNumber, findCopyNumberDrivers(drivers, geneCopyNumber.gene()))
            }
            .filter { (geneCopyNumber, drivers) -> MappingUtil.includedInGeneFilter(geneCopyNumber, geneFilter, { drivers.isNotEmpty() }) }
            .map { (geneCopyNumber, drivers) ->
                val canonicalDriver = drivers.firstOrNull { it.isCanonical }
                val otherGainDels =
                    drivers.filter { it != canonicalDriver }.map { driver -> findGainDel(gainDeletions, driver) }
                if (canonicalDriver != null) {
                    val canonicalGainDel = findGainDel(gainDeletions, canonicalDriver)
                    val event = canonicalGainDel.event()
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
                            canonicalGainDel.tumorMinCopies().roundToInt(),
                            canonicalGainDel.tumorMaxCopies().roundToInt()
                        ),
                        otherImpacts = otherGainDels.map { gainDel ->
                            TranscriptCopyNumberImpact(
                                gainDel.transcript(),
                                determineType(gainDel.interpretation()),
                                gainDel.tumorMinCopies().roundToInt(),
                                gainDel.tumorMaxCopies().roundToInt()
                            )
                        }.toSet(),
                    )
                } else {
                    val event =
                        if (otherGainDels.isEmpty()) geneCopyNumber.event() else otherGainDels.first()
                            .let { it.event() }
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
                            minCopies = geneCopyNumber.tumorMinCopies()
                                .roundToInt(), // TODO (CB): Should be changed once PurpleGeneCopyNumber has transcript / isCanonical fields
                            maxCopies = geneCopyNumber.tumorMaxCopies()
                                .roundToInt() // TODO (CB): Should be changed once PurpleGeneCopyNumber has transcript / isCanonical fields
                        ),
                        otherImpacts = otherGainDels.map { gainDel ->
                            TranscriptCopyNumberImpact(
                                gainDel.transcript(),
                                determineType(gainDel.interpretation()),
                                gainDel.tumorMaxCopies().roundToInt(),
                                gainDel.tumorMaxCopies().roundToInt()
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

            CopyNumberInterpretation.FULL_DEL -> {
                CopyNumberType.FULL_DEL
            }

            CopyNumberInterpretation.PARTIAL_DEL -> {
                CopyNumberType.PARTIAL_DEL
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

    private fun findGainDel(gainsDeles: List<GainDeletion>, driverToMatch: PurpleDriver): GainDeletion {
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