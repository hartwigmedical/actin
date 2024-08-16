package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.panel.PanelFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelSkippedExonsExtraction
import com.hartwig.actin.molecular.evidence.ActionableEvidenceFactory
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PanelFusionAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val knownFusionCache: KnownFusionCache,
    private val ensembleDataCache: EnsemblDataCache
) {
    fun annotate(fusions: List<PanelFusionExtraction>, skippedExons: List<PanelSkippedExonsExtraction>): Set<Fusion> {
        return (fusions.map { createFusion(it) } + skippedExons.map { createFusionFromExonSkip(it) })
            .map { annotateFusion(it) }
            .toSet()
    }

    fun fusionDriverLikelihood(driverType: FusionDriverType): DriverLikelihood? {
        return when (driverType) {
            FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_IG,
            FusionDriverType.KNOWN_PAIR_DEL_DUP -> DriverLikelihood.HIGH

            else -> DriverLikelihood.LOW
        }
    }

    private fun createFusion(panelFusionExtraction: PanelFusionExtraction): Fusion {
        if (panelFusionExtraction.geneUp == null && panelFusionExtraction.geneDown == null) {
            throw IllegalArgumentException("Invalid fusion, no genes provided")
        }

        val isReportable = true
        val driverType = determineFusionDriverType(panelFusionExtraction.geneUp, panelFusionExtraction.geneDown)

        return Fusion(
            geneStart = panelFusionExtraction.geneUp ?: "",
            geneEnd = panelFusionExtraction.geneDown ?: "",
            driverType = driverType,
            proteinEffect = ProteinEffect.UNKNOWN,
            isReportable = isReportable,
            event = panelFusionExtraction.display(),
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

    private fun createFusionFromExonSkip(panelSkippedExonsExtraction: PanelSkippedExonsExtraction): Fusion {
        val isReportable = true
        val driverType = determineFusionDriverType(panelSkippedExonsExtraction.gene, panelSkippedExonsExtraction.gene)
        val transcript = panelSkippedExonsExtraction.transcript ?: run {
            LOGGER.warn("No transcript provided for panel skipped exons in gene ${panelSkippedExonsExtraction.gene}, using canonical transcript")
            canonicalTranscriptForGene(panelSkippedExonsExtraction.gene)
        }

        return Fusion(
            geneStart = panelSkippedExonsExtraction.gene,
            geneEnd = panelSkippedExonsExtraction.gene,
            driverType = driverType,
            proteinEffect = ProteinEffect.UNKNOWN,
            isReportable = isReportable,
            event = panelSkippedExonsExtraction.display(),
            driverLikelihood = if (isReportable) fusionDriverLikelihood(driverType) else null,
            evidence = ActionableEvidenceFactory.createNoEvidence(),
            isAssociatedWithDrugResistance = null,
            extendedFusionDetails = ExtendedFusionDetails(
                transcript,
                transcript,
                panelSkippedExonsExtraction.start,
                panelSkippedExonsExtraction.end
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