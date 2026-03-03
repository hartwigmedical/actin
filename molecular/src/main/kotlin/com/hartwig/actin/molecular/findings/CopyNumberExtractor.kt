package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.finding.datamodel.GainDeletion
import kotlin.math.roundToInt

class CopyNumberExtractor(private val geneFilter: GeneFilter) {

    fun extract(gainDeletions: List<GainDeletion>): List<CopyNumber> {
        return gainDeletions.filter { MappingUtil.includedInGeneFilter(it, geneFilter) }
            .map {gainDeletion ->
                    CopyNumber(
                        gene = gainDeletion.gene(),
                        geneRole = GeneRole.UNKNOWN,
                        proteinEffect = ProteinEffect.UNKNOWN,
                        isAssociatedWithDrugResistance = null,
                        isReportable = gainDeletion.isReported,
                        event = gainDeletion.event(),
                        driverLikelihood = MappingUtil.determineDriverLikelihood(gainDeletion),
                        evidence = ExtractionUtil.noEvidence(),
                        canonicalImpact = TranscriptCopyNumberImpact(
                            gainDeletion.transcript(),
                            determineType(gainDeletion.interpretation()),
                            gainDeletion.tumorMinCopies().roundToInt(),
                            gainDeletion.tumorMaxCopies().roundToInt()
                        ),
                        otherImpacts = setOf(),
                    )
            }.sorted()
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
        }
    }
}