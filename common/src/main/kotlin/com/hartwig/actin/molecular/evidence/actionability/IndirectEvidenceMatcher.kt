package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.evidence.ServeVerifier.isCombinedProfile
import com.hartwig.actin.molecular.evidence.curation.GenericInhibitorFiltering
import com.hartwig.actin.molecular.evidence.matching.HotspotCoordinates
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

data class GeneEffectKey(
    val gene: String,
    val groupedProteinEffect: GroupedProteinEffect
)

enum class GroupedProteinEffect {
    UNSUPPORTED,
    LOSS_OF_FUNCTION_OR_LOSS_OF_FUNCTION_PREDICTED,
    GAIN_OF_FUNCTION_OR_GAIN_OF_FUNCTION_PREDICTED
}

fun ProteinEffect.toGroupedProteinEffect(): GroupedProteinEffect {
    return when (this) {
        ProteinEffect.LOSS_OF_FUNCTION, ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> GroupedProteinEffect.LOSS_OF_FUNCTION_OR_LOSS_OF_FUNCTION_PREDICTED
        ProteinEffect.GAIN_OF_FUNCTION, ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> GroupedProteinEffect.GAIN_OF_FUNCTION_OR_GAIN_OF_FUNCTION_PREDICTED
        else -> GroupedProteinEffect.UNSUPPORTED
    }
}

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
        return associatedEvidenceByGeneEffect[GeneEffectKey(gene, proteinEffect.toGroupedProteinEffect())] ?: emptySet()
    }

    companion object {

        val logger: Logger = LogManager.getLogger(IndirectEvidenceMatcher::class.java)

        fun create(evidences: List<EfficacyEvidence>, knownHotspots: Set<KnownHotspot>): IndirectEvidenceMatcher {
            val nonResistantHotspotsByCoordinates = collectNonResistantHotspotsByCoordinates(knownHotspots)

            val associatedEvidenceByGeneEffect = evidences
                .asSequence()
                .mapNotNull { evidence -> associatedEvidenceCandidate(evidence, nonResistantHotspotsByCoordinates) }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, evidences) -> evidences.toCollection(HashSet()) }

            return IndirectEvidenceMatcher(associatedEvidenceByGeneEffect)
        }

        fun empty(): IndirectEvidenceMatcher = IndirectEvidenceMatcher(emptyMap())

        private fun collectNonResistantHotspotsByCoordinates(knownHotspots: Set<KnownHotspot>): Map<HotspotCoordinates, KnownHotspot> {
            val groupedByCoordinates = knownHotspots
                .asSequence()
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
    val groupedProteinEffect = GeneAlterationFactory.convertProteinEffect(knownHotspot.proteinEffect()).toGroupedProteinEffect()
    return if (groupedProteinEffect == GroupedProteinEffect.UNSUPPORTED) {
        null
    } else {
        GeneEffectKey(knownHotspot.gene(), groupedProteinEffect) to evidence
    }
}
