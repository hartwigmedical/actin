package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.evidence.ServeVerifier.isCombinedProfile
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.actin.molecular.evidence.curation.GenericInhibitorFiltering
import com.hartwig.actin.molecular.evidence.matching.HotspotCoordinates
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

data class GeneEffectKey(
    val gene: String,
    val proteinEffect: ProteinEffect
)

private val SUPPORTED_PROTEIN_EFFECTS = setOf(
    ProteinEffect.GAIN_OF_FUNCTION,
    ProteinEffect.GAIN_OF_FUNCTION_PREDICTED,
    ProteinEffect.LOSS_OF_FUNCTION,
    ProteinEffect.LOSS_OF_FUNCTION_PREDICTED
)

class IndirectEvidenceMatcher(private val associatedEvidenceByGeneEffect: Map<GeneEffectKey, Set<EfficacyEvidence>>) {

    fun findIndirectEvidence(variant: Variant): Set<EfficacyEvidence> {
        val associatedEvidence = findAssociatedEvidence(variant.gene, variant.proteinEffect)
        val hotspotCoordinates = HotspotMatching.coordinates(variant)

        return associatedEvidence
            .asSequence()
            .filterNot { evidence -> evidence.isDirectHotspotMatch(hotspotCoordinates) }
            .toCollection(HashSet())
    }

    private fun findAssociatedEvidence(gene: String, proteinEffect: ProteinEffect): Set<EfficacyEvidence> {
        return associatedEvidenceByGeneEffect[GeneEffectKey(gene, proteinEffect)] ?: emptySet()
    }

    companion object {

        val logger: Logger = LogManager.getLogger(IndirectEvidenceMatcher::class.java)

        fun create(serveRecord: ServeRecord): IndirectEvidenceMatcher {
            val nonResistantHotspotsByCoordinates = collectNonResistantHotspotsByCoordinates(serveRecord)

            val associatedEvidenceByGeneEffect = serveRecord.evidences()
                .asSequence()
                .filter { it.source() == Knowledgebase.CKB }
                .mapNotNull { evidence -> associatedEvidenceCandidate(evidence, nonResistantHotspotsByCoordinates) }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, evidences) -> evidences.toCollection(HashSet()) }

            return IndirectEvidenceMatcher(associatedEvidenceByGeneEffect)
        }

        fun empty(): IndirectEvidenceMatcher = IndirectEvidenceMatcher(emptyMap())

        private fun collectNonResistantHotspotsByCoordinates(serveRecord: ServeRecord): Map<HotspotCoordinates, KnownHotspot> {
            val groupedByCoordinates = serveRecord.knownEvents().hotspots()
                .asSequence()
                .filter { it.sources().contains(Knowledgebase.CKB) }
                .filterNot { it.associatedWithDrugResistance() == true }
                .groupBy { HotspotMatching.coordinates(it) }

            groupedByCoordinates
                .filter { (_, hotspots) -> hotspots.size > 1 }
                .forEach { (key, hotspots) ->
                    hotspots.drop(1).forEach { duplicate ->
                        logger.warn("KnownHotspot with coordinates $key already exists in map; skipping duplicate for known hotspot $duplicate.")
                    }
                }

            return groupedByCoordinates.mapValues { (_, hotspots) -> hotspots.first() }
        }

        private fun associatedEvidenceCandidate(
            evidence: EfficacyEvidence,
            knownHotspotsByVariant: Map<HotspotCoordinates, KnownHotspot>
        ): Pair<GeneEffectKey, EfficacyEvidence>? {
            if (isCombinedProfile(evidence.molecularCriterium())) {
                return null
            }

            return evidence.molecularCriterium().hotspots().firstOrNull()
                ?.takeIf { ApplicabilityFiltering.isApplicable(it) }
                ?.variants()
                ?.let { variants -> resolveNonResistantKnownHotspot(variants, knownHotspotsByVariant) }
                ?.takeIf { GenericInhibitorFiltering.isGenericInhibitor(evidence.treatment()) }
                ?.let { knownHotspot -> toGeneEffectMatch(knownHotspot, evidence) }
        }
    }
}

private fun EfficacyEvidence.isDirectHotspotMatch(coordinates: HotspotCoordinates): Boolean {
    return molecularCriterium().hotspots()
        .asSequence()
        .flatMap { it.variants().asSequence() }
        .map { HotspotMatching.coordinates(it) }
        .any { it == coordinates }
}

private fun resolveNonResistantKnownHotspot(
    variants: Collection<VariantAnnotation>,
    knownHotspotsByVariant: Map<HotspotCoordinates, KnownHotspot>
): KnownHotspot? {
    return variants.asSequence()
        .mapNotNull { variant -> knownHotspotsByVariant[HotspotMatching.coordinates(variant)] }
        .toList()
        .takeUnless { hotspots -> hotspots.any { it.associatedWithDrugResistance() == true } }
        ?.firstOrNull()
}

private fun toGeneEffectMatch(
    knownHotspot: KnownHotspot,
    evidence: EfficacyEvidence
): Pair<GeneEffectKey, EfficacyEvidence>? {
    val proteinEffect = GeneAlterationFactory.convertProteinEffect(knownHotspot.proteinEffect())
    return proteinEffect
        .takeIf { it in SUPPORTED_PROTEIN_EFFECTS }
        ?.let { GeneEffectKey(knownHotspot.gene(), it) to evidence }
}
