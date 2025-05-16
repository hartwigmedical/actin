package com.hartwig.actin.molecular.hotspotcomparison

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.molecular.gene.KnownGene

object HotspotEvaluator {

    fun annotateHotspots(orange: OrangeRecord, serveRecord: ServeRecord): List<AnnotatedHotspot> {
        val knownGenes = serveRecord.knownEvents().genes().map(KnownGene::gene).toSet()
        return orange.purple().allSomaticVariants().filter { it.gene() in knownGenes }
            .mapNotNull { variant ->
                val criteria = createMinimalVariantForMatching(variant)
                val knownEventResolver =
                    KnownEventResolverFactory.create(KnownEventResolverFactory.includeKnownEvents(serveRecord.knownEvents(), true))
                val serveVariantAlteration = knownEventResolver.resolveForVariant(criteria)
                val isHotspotServe = serveVariantAlteration.isHotspot
                val isHotspotOrange = variant.hotspot() == HotspotType.HOTSPOT
                if (isHotspotServe || isHotspotOrange) {
                    AnnotatedHotspot(
                        gene = variant.gene(),
                        chromosome = variant.chromosome(),
                        position = variant.position(),
                        ref = variant.ref(),
                        alt = variant.alt(),
                        codingImpact = variant.canonicalImpact().hgvsCodingImpact(),
                        proteinImpact = variant.canonicalImpact().hgvsProteinImpact(),
                        isHotspotOrange = isHotspotOrange,
                        isHotspotServe = isHotspotServe,
                    )
                } else {
                    null
                }
            }
    }

    private fun createMinimalVariantForMatching(variant: PurpleVariant) =
        Variant(
            gene = variant.gene(),
            chromosome = variant.chromosome(),
            position = variant.position(),
            ref = variant.ref(),
            alt = variant.alt(),
            type = VariantType.UNDEFINED,
            variantAlleleFrequency = null,
            canonicalImpact = TranscriptVariantImpact(
                transcriptId = "",
                hgvsCodingImpact = "",
                hgvsProteinImpact = "",
                affectedCodon = 0,
                isSpliceRegion = false,
                effects = emptySet(),
                codingEffect = CodingEffect.NONE,
                affectedExon = null
            ),
            otherImpacts = emptySet(),
            extendedVariantDetails = null,
            isHotspot = false,
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet()),
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false
        )
}