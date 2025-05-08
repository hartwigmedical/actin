package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import org.apache.logging.log4j.LogManager

class PanelFusionAnnotator(
    private val knownFusionCache: KnownFusionCache,
    private val ensembleDataCache: EnsemblDataCache
) {

    private val logger = LogManager.getLogger(PanelAnnotator::class.java)

    fun annotate(fusions: Set<SequencedFusion>, skippedExons: Set<SequencedSkippedExons>): List<Fusion> {
        return (fusions.map { createFusion(it) } + skippedExons.map { createFusionFromExonSkip(it) })
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
            evidence = ExtractionUtil.noEvidence(),
            isAssociatedWithDrugResistance = null,
            geneTranscriptStart = sequencedFusion.transcriptUp,
            geneTranscriptEnd = sequencedFusion.transcriptDown,
            fusedExonUp = sequencedFusion.exonUp,
            fusedExonDown = sequencedFusion.exonDown
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
            logger.warn("No transcript provided for panel skipped exons in gene ${sequencedSkippedExons.gene}, using canonical transcript")
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
            evidence = ExtractionUtil.noEvidence(),
            isAssociatedWithDrugResistance = null,
            geneTranscriptStart = transcript,
            geneTranscriptEnd = transcript,
            fusedExonUp = sequencedSkippedExons.exonStart,
            fusedExonDown = sequencedSkippedExons.exonEnd
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