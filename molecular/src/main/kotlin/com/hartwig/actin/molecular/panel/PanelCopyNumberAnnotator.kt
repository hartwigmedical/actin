package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import org.apache.logging.log4j.LogManager

private const val MIN_COPY_NUMBER = 6
private const val MAX_COPY_NUMBER = 6
private const val PLOIDY = 2

class PanelCopyNumberAnnotator(private val evidenceDatabase: EvidenceDatabase, private val ensembleDataCache: EnsemblDataCache) {

    private val logger = LogManager.getLogger(PanelAnnotator::class.java)

    fun <T> annotate(copyNumber: Set<T>): List<CopyNumber> {
        return copyNumber.map { element ->
            when (element) {
                is SequencedAmplification -> inferredCopyNumber(element)
                is SequencedDeletedGene -> inferredCopyNumber(element)
                else -> throw IllegalArgumentException("Unsupported type: $element")
            }
        }.map(::annotatedInferredCopyNumber)
    }

    private fun annotatedInferredCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber))
        val geneAlteration =
            GeneAlterationFactory.convertAlteration(copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber))
        return copyNumber.copy(
            evidence = evidence,
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )
    }

    private fun inferredCopyNumber(sequencedAmplifiedGene: SequencedAmplification): CopyNumber {
        val canonicalTranscript = canonicalTranscriptForGene(sequencedAmplifiedGene.gene)
        val isCanonicalTranscript = canonicalTranscript == sequencedAmplifiedGene.transcript || sequencedAmplifiedGene.transcript == null
        val transcriptId = sequencedAmplifiedGene.transcript ?: run {
            logger.warn("No transcript provided for panel skipped exons in gene ${sequencedAmplifiedGene.gene}, using canonical transcript")
            canonicalTranscript
        }
        val canonicalImpact = TranscriptCopyNumberImpact(
            transcriptId = canonicalTranscript,
            type = if (isCanonicalTranscript) CopyNumberType.FULL_GAIN else CopyNumberType.NONE,
            minCopies = if (isCanonicalTranscript) MIN_COPY_NUMBER else PLOIDY,
            maxCopies = if (isCanonicalTranscript) MAX_COPY_NUMBER else PLOIDY
        )
        val otherImpacts = if (isCanonicalTranscript) emptySet() else setOf(
            TranscriptCopyNumberImpact(
                transcriptId = transcriptId,
                type = CopyNumberType.FULL_GAIN,
                minCopies = MIN_COPY_NUMBER,
                maxCopies = MAX_COPY_NUMBER
            )
        )

        return CopyNumber(
            gene = sequencedAmplifiedGene.gene,
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
            isReportable = true,
            event = sequencedAmplifiedGene.gene,
            driverLikelihood = DriverLikelihood.HIGH,
            evidence = ClinicalEvidenceFactory.createNoEvidence(),
            canonicalImpact = canonicalImpact,
            otherImpacts = otherImpacts
        )
    }

    private fun inferredCopyNumber(sequencedDeletedGene: SequencedDeletedGene): CopyNumber {
        val canonicalTranscript = canonicalTranscriptForGene(sequencedDeletedGene.gene)
        val isCanonicalTranscript = canonicalTranscript == sequencedDeletedGene.transcript || sequencedDeletedGene.transcript == null
        val transcriptId = sequencedDeletedGene.transcript ?: run {
            logger.warn("No transcript provided for panel skipped exons in gene ${sequencedDeletedGene.gene}, using canonical transcript")
            canonicalTranscript
        }
        val canonicalImpact = TranscriptCopyNumberImpact(
            transcriptId = canonicalTranscript,
            type = if (isCanonicalTranscript) CopyNumberType.LOSS else CopyNumberType.NONE,
            minCopies = if (isCanonicalTranscript) 0 else PLOIDY,
            maxCopies = if (isCanonicalTranscript) 0 else PLOIDY
        )
        val otherImpacts = if (isCanonicalTranscript) emptySet() else setOf(
            TranscriptCopyNumberImpact(
                transcriptId = transcriptId,
                type = CopyNumberType.LOSS,
                minCopies = 0,
                maxCopies = 0
            )
        )

        return CopyNumber(
            gene = sequencedDeletedGene.gene,
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
            isReportable = true,
            event = sequencedDeletedGene.gene,
            driverLikelihood = DriverLikelihood.HIGH,
            evidence = ClinicalEvidenceFactory.createNoEvidence(),
            canonicalImpact = canonicalImpact,
            otherImpacts = otherImpacts
        )
    }

    private fun canonicalTranscriptForGene(gene: String): String {
        val geneData = ensembleDataCache.findGeneDataByName(gene)
            ?: throw IllegalArgumentException("No gene data found for gene $gene")
        val transcript = ensembleDataCache.findCanonicalTranscript(geneData.geneId())?.transcriptName()
            ?: throw IllegalStateException("No canonical transcript found for gene $gene")
        return transcript
    }
}