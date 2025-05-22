package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import org.apache.logging.log4j.LogManager

class PanelCopyNumberAnnotator(private val evidenceDatabase: EvidenceDatabase, private val ensembleDataCache: EnsemblDataCache) {

    private val logger = LogManager.getLogger(PanelAnnotator::class.java)

    fun <T> annotate(copyNumber: Set<T>): List<CopyNumber> {
        return copyNumber.map { element ->
            when (element) {
                is SequencedAmplification -> convertSequencedAmplifiedGene(element)
                is SequencedDeletion -> convertSequencedDeletedGene(element)
                else -> throw IllegalArgumentException("Unsupported type: $element")
            }
        }.map(::annotatedInferredCopyNumber)
    }

    private fun annotatedInferredCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val alteration = evidenceDatabase.alterationForCopyNumber(copyNumber)
        val copyNumberWithGeneAlteration = copyNumber.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceDatabase.evidenceForCopyNumber(copyNumberWithGeneAlteration)
        return copyNumberWithGeneAlteration.copy(evidence = evidence)
    }

    private fun convertSequencedAmplifiedGene(sequencedAmplifiedGene: SequencedAmplification): CopyNumber {
        val canonicalTranscript = canonicalTranscriptIdForGene(sequencedAmplifiedGene.gene)
        val isCanonicalTranscript = canonicalTranscript == sequencedAmplifiedGene.transcript || sequencedAmplifiedGene.transcript == null
        val transcriptId = sequencedAmplifiedGene.transcript ?: run {
            logger.warn("No transcript provided for panel amplification in gene ${sequencedAmplifiedGene.gene}, using canonical transcript")
            canonicalTranscript
        }
        val canonicalImpact = TranscriptCopyNumberImpact(
            transcriptId = canonicalTranscript,
            type = if (isCanonicalTranscript) CopyNumberType.FULL_GAIN else CopyNumberType.NONE,
            minCopies = if (isCanonicalTranscript) sequencedAmplifiedGene.copies else null,
            maxCopies = if (isCanonicalTranscript) sequencedAmplifiedGene.copies else null
        )
        val otherImpacts = if (isCanonicalTranscript) emptySet() else setOf(
            TranscriptCopyNumberImpact(
                transcriptId = transcriptId,
                type = CopyNumberType.FULL_GAIN,
                minCopies = sequencedAmplifiedGene.copies,
                maxCopies = sequencedAmplifiedGene.copies
            )
        )

        return CopyNumber(
            gene = sequencedAmplifiedGene.gene,
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
            isReportable = true,
            event = "${sequencedAmplifiedGene.gene} amp",
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
            type = if (isCanonicalTranscript) CopyNumberType.DEL else CopyNumberType.NONE,
            minCopies = if (isCanonicalTranscript) 0 else null,
            maxCopies = if (isCanonicalTranscript) 0 else null
        )
        val otherImpacts = if (isCanonicalTranscript) emptySet() else setOf(
            TranscriptCopyNumberImpact(
                transcriptId = transcriptId,
                type = CopyNumberType.DEL,
                minCopies = 0,
                maxCopies = 0
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
}