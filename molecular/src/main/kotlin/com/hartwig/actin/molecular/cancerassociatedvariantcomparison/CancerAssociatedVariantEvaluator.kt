package com.hartwig.actin.molecular.cancerassociatedvariantcomparison

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.molecular.gene.KnownGene

object CancerAssociatedVariantEvaluator {

    fun annotateCavs(orange: OrangeRecord, serveRecord: ServeRecord): List<AnnotatedCancerAssociatedVariant> {
        val knownGenes = serveRecord.knownEvents().genes().map(KnownGene::gene).toSet()
        return orange.purple().allSomaticVariants().filter { it.gene() in knownGenes }
            .mapNotNull { variant ->
                val criteria = createMinimalVariantForMatching(variant)
                val knownEventResolver =
                    KnownEventResolverFactory.create(KnownEventResolverFactory.includeKnownEvents(serveRecord.knownEvents(), true))
                val serveVariantAlteration = knownEventResolver.resolveForVariant(criteria)
                val isCavServe = serveVariantAlteration.isCancerAssociatedVariant
                val isCavOrange = variant.hotspot() == HotspotType.HOTSPOT
                if (isCavServe || isCavOrange) {
                    AnnotatedCancerAssociatedVariant(
                        gene = variant.gene(),
                        chromosome = variant.chromosome(),
                        position = variant.position(),
                        ref = variant.ref(),
                        alt = variant.alt(),
                        codingImpact = variant.canonicalImpact().hgvsCodingImpact(),
                        proteinImpact = variant.canonicalImpact().hgvsProteinImpact(),
                        isCavOrange = isCavOrange,
                        isCavServe = isCavServe,
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
            isCancerAssociatedVariant = false,
            isReportable = false,
            event = "",
            driverLikelihood = null,
            evidence = ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet()),
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false
        )
}