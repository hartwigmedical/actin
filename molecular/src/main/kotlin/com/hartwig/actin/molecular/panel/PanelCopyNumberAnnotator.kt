package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import org.apache.logging.log4j.LogManager

class PanelCopyNumberAnnotator(private val ensembleDataCache: EnsemblDataCache) {

    private val logger = LogManager.getLogger(PanelAnnotator::class.java)

    fun <T> annotate(copyNumber: Set<T>): List<CopyNumber> {
        return copyNumber.map { element ->
            when (element) {
                is SequencedAmplification -> convertSequencedAmplifiedGene(element)
                is SequencedDeletion -> convertSequencedDeletedGene(element)
                else -> throw IllegalArgumentException("Unsupported type: $element")
            }
        }
    }

    private fun convertSequencedAmplifiedGene(sequencedAmplification: SequencedAmplification): CopyNumber {
        val canonicalTranscript = canonicalTranscriptIdForGene(sequencedAmplification.gene)
        val isCanonicalTranscript = canonicalTranscript == sequencedAmplification.transcript || sequencedAmplification.transcript == null
        val transcriptId = sequencedAmplification.transcript ?: run {
            logger.warn("No transcript provided for panel amplification in gene ${sequencedAmplification.gene}, using canonical transcript")
            canonicalTranscript
        }
        val canonicalImpact = TranscriptCopyNumberImpact(
            transcriptId = canonicalTranscript,
            type = resolveCanonicalAmpType(isCanonicalTranscript, sequencedAmplification.isPartial),
            minCopies = if (isCanonicalTranscript) sequencedAmplification.copies else null,
            maxCopies = if (isCanonicalTranscript) sequencedAmplification.copies else null
        )
        val otherImpacts = if (isCanonicalTranscript) emptySet() else setOf(
            TranscriptCopyNumberImpact(
                transcriptId = transcriptId,
                type = if (sequencedAmplification.isPartial == true) CopyNumberType.PARTIAL_GAIN else CopyNumberType.FULL_GAIN,
                minCopies = sequencedAmplification.copies,
                maxCopies = sequencedAmplification.copies
            )
        )
        val gene = sequencedAmplification.gene

        return CopyNumber(
            gene = gene,
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
            isReportable = true,
            event = if (sequencedAmplification.isPartial == true) "$gene partial amp" else "$gene amp",
            driverLikelihood = DriverLikelihood.HIGH,
            evidence = ExtractionUtil.noEvidence(),
            canonicalImpact = canonicalImpact,
            otherImpacts = otherImpacts
        )
    }

    private fun convertSequencedDeletedGene(sequencedDeletion: SequencedDeletion): CopyNumber {
        val canonicalTranscript = canonicalTranscriptIdForGene(sequencedDeletion.gene)
        val isCanonicalTranscript = canonicalTranscript == sequencedDeletion.transcript || sequencedDeletion.transcript == null
        val transcriptId = sequencedDeletion.transcript ?: run {
            logger.warn("No transcript provided for panel deletion in gene ${sequencedDeletion.gene}, using canonical transcript")
            canonicalTranscript
        }
        val canonicalImpact = TranscriptCopyNumberImpact(
            transcriptId = canonicalTranscript,
            type = resolveCanonicalDelType(isCanonicalTranscript, sequencedDeletion.isPartial),
            minCopies = if (isCanonicalTranscript) 0 else null,
            maxCopies = if (isCanonicalTranscript) resolveMaxCopiesDel(sequencedDeletion.isPartial) else null
        )
        val otherImpacts = if (isCanonicalTranscript) emptySet() else setOf(
            TranscriptCopyNumberImpact(
                transcriptId = transcriptId,
                type = if (sequencedDeletion.isPartial == true) CopyNumberType.PARTIAL_DEL else CopyNumberType.FULL_DEL,
                minCopies = 0,
                maxCopies = resolveMaxCopiesDel(sequencedDeletion.isPartial)
            )
        )

        return CopyNumber(
            gene = sequencedDeletion.gene,
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
            isReportable = true,
            event = "${sequencedDeletion.gene} del",
            driverLikelihood = DriverLikelihood.HIGH,
            evidence = ExtractionUtil.noEvidence(),
            canonicalImpact = canonicalImpact,
            otherImpacts = otherImpacts
        )
    }

    private fun canonicalTranscriptIdForGene(gene: String): String {
        val geneData = ensembleDataCache.findGeneDataByName(gene)
            ?: throw IllegalArgumentException("No gene data found for gene $gene")
        return ensembleDataCache.findCanonicalTranscript(geneData.geneId())?.transcriptName()
            ?: throw IllegalStateException("No canonical transcript found for gene $gene")
    }

    private fun resolveCanonicalAmpType(isCanonicalTranscript: Boolean, isPartial: Boolean?): CopyNumberType {
        return when {
            isCanonicalTranscript && isPartial == true -> CopyNumberType.PARTIAL_GAIN
            isCanonicalTranscript -> CopyNumberType.FULL_GAIN
            else -> CopyNumberType.NONE
        }
    }

    private fun resolveCanonicalDelType(isCanonicalTranscript: Boolean, isPartial: Boolean?): CopyNumberType {
        return when {
            isCanonicalTranscript && isPartial == true -> CopyNumberType.PARTIAL_DEL
            isCanonicalTranscript -> CopyNumberType.FULL_DEL
            else -> CopyNumberType.NONE
        }
    }

    private fun resolveMaxCopiesDel(isPartial: Boolean?) = if (isPartial == true) 1 else 0
}