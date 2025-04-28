package com.hartwig.actin.molecular.hotspotcomparison

import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.hotspot.HotspotFunctions
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.molecular.gene.KnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot

object HotspotComparator {

    fun annotateHotspots(orange: OrangeRecord, serveRecord: ServeRecord): List<AnnotatedHotspot> {
        val knownGenes = serveRecord.knownEvents().genes().map(KnownGene::gene).toSet()
        return orange.purple().allSomaticVariants().filter { it.gene() in knownGenes }
            .mapNotNull { variant ->
                val criteria = createVariantCriteria(variant)
                val knownEventResolver = KnownEventResolverFactory.create(serveRecord.knownEvents())
                val serveGeneAlteration =
                    knownEventResolver.resolveForVariant(criteria).find { (it as KnownHotspot).sources().contains(Knowledgebase.CKB) }
                val isHotspotServe = HotspotFunctions.isHotspot(serveGeneAlteration)
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

    private fun createVariantCriteria(variant: PurpleVariant) =
        VariantMatchCriteria(
            gene = variant.gene(),
            chromosome = variant.chromosome(),
            position = variant.position(),
            ref = variant.ref(),
            alt = variant.alt(),
            type = null,
            codingEffect = null,
            isReportable = variant.reported()
        )
}