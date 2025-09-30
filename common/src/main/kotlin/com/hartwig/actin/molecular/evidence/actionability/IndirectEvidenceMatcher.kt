package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.evidence.ServeVerifier.isCombinedProfile
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.actin.molecular.evidence.curation.GenericInhibitorFiltering
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

private data class HotspotKey(
    val gene: String,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String
)

private val SUPPORTED_PROTEIN_EFFECTS = setOf(
    ProteinEffect.GAIN_OF_FUNCTION,
    ProteinEffect.GAIN_OF_FUNCTION_PREDICTED,
    ProteinEffect.LOSS_OF_FUNCTION,
    ProteinEffect.LOSS_OF_FUNCTION_PREDICTED
)

class IndirectEvidenceMatcher(private val treatmentByGeneEffectForHotspots: Map<GeneEffectKey, Set<EfficacyEvidence>>) {

    fun findIndirectEvidence(variant: Variant): Set<EfficacyEvidence> {
        val matches = findIndirectEvidence(variant.gene, variant.proteinEffect)

        val hotspotKey = variant.toHotspotKey()
        return matches
            .asSequence()
            .filterNot { evidence -> evidence.containsHotspot(hotspotKey) }
            .toCollection(HashSet())
    }

    private fun findIndirectEvidence(gene: String, proteinEffect: ProteinEffect): Set<EfficacyEvidence> {
        return treatmentByGeneEffectForHotspots[GeneEffectKey(gene, proteinEffect)] ?: emptySet()
    }

    companion object {

        val logger: Logger = LogManager.getLogger(IndirectEvidenceMatcher::class.java)

        fun create(serveRecord: ServeRecord): IndirectEvidenceMatcher {
            val knownResistantHotspotsByPosition = mapKnownResistantHotspotsByPosition(serveRecord)

            val treatmentByGeneEffect = serveRecord.evidences()
                .asSequence()
                .filter { it.source() == Knowledgebase.CKB }
                .filterNot { isCombinedProfile(it.molecularCriterium()) }
                .filter { it.molecularCriterium().hotspots().any() }
                .mapNotNull { evidence -> matchableEvidence(evidence, knownResistantHotspotsByPosition) }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, evidences) -> evidences.toCollection(HashSet()) }

            return IndirectEvidenceMatcher(treatmentByGeneEffect)
        }

        fun empty(): IndirectEvidenceMatcher = IndirectEvidenceMatcher(emptyMap())

        private fun mapKnownResistantHotspotsByPosition(serveRecord: ServeRecord): Map<HotspotKey, KnownHotspot> {
            val groupedByPosition = serveRecord.knownEvents().hotspots()
                .asSequence()
                .filter { it.sources().contains(Knowledgebase.CKB) }
                .filterNot { it.associatedWithDrugResistance() == true }
                .groupBy { it.toHotspotKey() }

            groupedByPosition
                .filter { (_, hotspots) -> hotspots.size > 1 }
                .forEach { (key, hotspots) ->
                    hotspots.drop(1).forEach { duplicate ->
                        logger.warn("KnownHotspot with key $key already exists in map; skipping duplicate for known hotspot $duplicate.")
                    }
                }

            return groupedByPosition.mapValues { (_, hotspots) -> hotspots.first() }
        }

        private fun matchableEvidence(
            evidence: EfficacyEvidence,
            knownHotspotsByVariant: Map<HotspotKey, KnownHotspot>
        ): Pair<GeneEffectKey, EfficacyEvidence>? {
            return evidence.molecularCriterium().hotspots().firstOrNull()
                ?.takeIf { ApplicabilityFiltering.isApplicable(it) }
                ?.variants()?.firstOrNull()
                ?.let { variant -> knownHotspotsByVariant[variant.toHotspotKey()] }
                ?.takeIf { hotspot -> hotspot.associatedWithDrugResistance() != true }
                ?.takeIf { GenericInhibitorFiltering.isGenericInhibitor(evidence.treatment()) }
                ?.let { knownHotspot ->
                    val proteinEffect = GeneAlterationFactory.convertProteinEffect(knownHotspot.proteinEffect())
                    proteinEffect.takeIf { it in SUPPORTED_PROTEIN_EFFECTS }
                        ?.let { GeneEffectKey(knownHotspot.gene(), it) to evidence }
                }
        }
    }
}

private fun EfficacyEvidence.containsHotspot(key: HotspotKey): Boolean {
    return molecularCriterium().hotspots()
        .asSequence()
        .flatMap { it.variants().asSequence() }
        .map { it.toHotspotKey() }
        .any { it == key }
}

private fun VariantAnnotation.toHotspotKey(): HotspotKey {
    return HotspotKey(gene(), chromosome(), position(), ref(), alt())
}

private fun KnownHotspot.toHotspotKey(): HotspotKey {
    return HotspotKey(gene(), chromosome(), position(), ref(), alt())
}

private fun Variant.toHotspotKey(): HotspotKey {
    return HotspotKey(gene, chromosome, position, ref, alt)
}
