package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import java.util.function.Predicate

object EfficacyEvidenceExtractor {

    fun extractHotspotEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, ActionableEventExtraction.hotspotFilter())
    }

    fun extractCodonEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, ActionableEventExtraction.codonFilter())
    }

    fun extractExonEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, ActionableEventExtraction.exonFilter())
    }

    fun extractGeneEvidence(evidences: List<EfficacyEvidence>, validGeneEvents: Set<GeneEvent>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, ActionableEventExtraction.geneFilter(validGeneEvents))
    }

    fun extractFusionEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, ActionableEventExtraction.fusionFilter())
    }

    fun extractCharacteristicEvidence(evidences: List<EfficacyEvidence>, validTypes: Set<TumorCharacteristicType>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, ActionableEventExtraction.characteristicsFilter(validTypes))
    }

    private fun extractEfficacyEvidence(
        evidences: List<EfficacyEvidence>,
        predicate: Predicate<MolecularCriterium>
    ): List<EfficacyEvidence> {
        return evidences.filter { evidence -> predicate.test(evidence.molecularCriterium()) }
    }
}