package com.hartwig.actin.molecular.panel

import com.hartwig.actin.clinical.datamodel.SequencedFusion
import com.hartwig.actin.clinical.datamodel.SequencedSkippedExons
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvidenceFactory
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PanelFusionAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val knownFusionCache: KnownFusionCache,
    private val ensembleDataCache: EnsemblDataCache
) {
    fun annotate(fusions: Set<SequencedFusion>, skippedExons: Set<SequencedSkippedExons>): Set<Fusion> {
        return (fusions.map { createFusion(it) } + skippedExons.map { createFusionFromExonSkip(it) })
            .map { annotateFusion(it) }
            .toSet()
    }

    fun fusionDriverLikelihood(driverType: FusionDriverType): DriverLikelihood {
        return when (driverType) {
            FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_IG,
            FusionDriverType.KNOWN_PAIR_DEL_DUP -> DriverLikelihood.HIGH

            else -> DriverLikelihood.LOW
        }
    }

    private fun createFusion(sequencedFusion: SequencedFusion): Fusion {
        if (sequencedFusion.geneUp == null && sequencedFusion.geneDown == null) {
            throw IllegalArgumentException("Invalid fusion, no genes provided")
        }

        val isReportable = true
        val driverType = determineFusionDriverType(sequencedFusion.geneUp, sequencedFusion.geneDown)

        return Fusion(
            geneStart = sequencedFusion.geneUp ?: "",
            geneEnd = sequencedFusion.geneDown ?: "",
            driverType = driverType,
            proteinEffect = ProteinEffect.UNKNOWN,
            isReportable = isReportable,
            event = sequencedFusion.display(),
            driverLikelihood = if (isReportable) fusionDriverLikelihood(driverType) else null,
            evidence = ActionableEvidenceFactory.createNoEvidence(),
            isAssociatedWithDrugResistance = null,
            extendedFusionDetails = null
        )
    }

    fun determineFusionDriverType(geneUp: String?, geneDown: String?): FusionDriverType {
        if (geneUp != null && geneDown != null) {
            if (knownFusionCache.hasKnownFusion(geneUp, geneDown)) {
                return FusionDriverType.KNOWN_PAIR
            }

            if (geneUp == geneDown && knownFusionCache.hasExonDelDup(geneUp)) {
                return FusionDriverType.KNOWN_PAIR_DEL_DUP
            }
        }

        val isPromiscuous5 = geneUp?.let { knownFusionCache.hasPromiscuousFiveGene(it) } ?: false
        val isPromiscuous3 = geneDown?.let { knownFusionCache.hasPromiscuousThreeGene(it) } ?: false

        when {
            isPromiscuous5 && isPromiscuous3 -> return FusionDriverType.PROMISCUOUS_BOTH
            isPromiscuous5 -> return FusionDriverType.PROMISCUOUS_5
            isPromiscuous3 -> return FusionDriverType.PROMISCUOUS_3
        }

        return FusionDriverType.NONE
    }

    private fun createFusionFromExonSkip(sequencedSkippedExons: SequencedSkippedExons): Fusion {
        val isReportable = true
        val driverType = determineFusionDriverType(sequencedSkippedExons.gene, sequencedSkippedExons.gene)
        val transcript = sequencedSkippedExons.transcript ?: run {
            LOGGER.warn("No transcript provided for panel skipped exons in gene ${sequencedSkippedExons.gene}, using canonical transcript")
            canonicalTranscriptForGene(sequencedSkippedExons.gene)
        }

        return Fusion(
            geneStart = sequencedSkippedExons.gene,
            geneEnd = sequencedSkippedExons.gene,
            driverType = driverType,
            proteinEffect = ProteinEffect.UNKNOWN,
            isReportable = isReportable,
            event = sequencedSkippedExons.display(),
            driverLikelihood = if (isReportable) fusionDriverLikelihood(driverType) else null,
            evidence = ActionableEvidenceFactory.createNoEvidence(),
            isAssociatedWithDrugResistance = null,
            extendedFusionDetails = ExtendedFusionDetails(
                transcript,
                transcript,
                sequencedSkippedExons.exonStart,
                sequencedSkippedExons.exonEnd
            )
        )
    }

    private fun canonicalTranscriptForGene(gene: String): String {
        val geneData = ensembleDataCache.findGeneDataByName(gene)
            ?: throw IllegalArgumentException("No gene data found for gene $gene")
        val transcript = ensembleDataCache.findCanonicalTranscript(geneData.geneId())?.transcriptName()
            ?: throw IllegalStateException("No canonical transcript found for gene $gene")
        return transcript
    }

    private fun annotateFusion(fusion: Fusion): Fusion {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForFusion(createFusionMatchCriteria(fusion)))
        val knownFusion = evidenceDatabase.lookupKnownFusion(createFusionMatchCriteria(fusion))

        val proteinEffect = if (knownFusion == null) ProteinEffect.UNKNOWN else {
            GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        }
        val isAssociatedWithDrugResistance = knownFusion?.associatedWithDrugResistance()

        return fusion.copy(
            evidence = evidence,
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = isAssociatedWithDrugResistance,
        )
    }

    private fun createFusionMatchCriteria(fusion: Fusion) = FusionMatchCriteria(
        isReportable = fusion.isReportable,
        geneStart = fusion.geneStart,
        geneEnd = fusion.geneEnd,
        driverType = fusion.driverType
    )

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PanelAnnotator::class.java)
    }
}