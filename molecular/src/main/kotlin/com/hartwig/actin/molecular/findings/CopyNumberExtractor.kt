package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.DriverEventFactory
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.finding.datamodel.GainDeletion
import com.hartwig.hmftools.finding.datamodel.GainDeletion.GeneExtent
import kotlin.math.roundToInt

class CopyNumberExtractor(private val geneFilter: GeneFilter) {

    fun extract(gainDeletions: List<GainDeletion>): List<CopyNumber> {
        return gainDeletions.filter { MappingUtil.includedInGeneFilter(it, geneFilter) }
            .map { gainDeletion ->
                CopyNumber(
                    gene = gainDeletion.gene(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    isReportable = gainDeletion.isReported,
                    event = DriverEventFactory.event(gainDeletion),
                    driverLikelihood = MappingUtil.determineDriverLikelihood(gainDeletion),
                    evidence = ExtractionUtil.noEvidence(),
                    canonicalImpact = TranscriptCopyNumberImpact(
                        gainDeletion.transcript(),
                        determineType(gainDeletion),
                        gainDeletion.tumorMinCopies().roundToInt(),
                        gainDeletion.tumorMaxCopies().roundToInt()
                    ),
                    otherImpacts = setOf(),
                )
            }.sorted()
    }

    internal fun determineType(
        gainDeletion: GainDeletion
    ): CopyNumberType {
        return when (gainDeletion.geneExtent()) {
            GeneExtent.FULL_GENE -> when (gainDeletion.somaticType()) {
                GainDeletion.Type.HOM_DEL, GainDeletion.Type.HET_DEL -> CopyNumberType.FULL_DEL
                GainDeletion.Type.GAIN -> CopyNumberType.FULL_GAIN
                else -> throw IllegalArgumentException("Unsupported somatic type: " + gainDeletion.somaticType())
            }

            GeneExtent.PARTIAL_GENE -> when (gainDeletion.somaticType()) {
                GainDeletion.Type.HOM_DEL, GainDeletion.Type.HET_DEL -> CopyNumberType.PARTIAL_DEL
                GainDeletion.Type.GAIN -> CopyNumberType.PARTIAL_GAIN
                else -> throw IllegalArgumentException("Unsupported somatic type: " + gainDeletion.somaticType())
            }
        }
    }
}