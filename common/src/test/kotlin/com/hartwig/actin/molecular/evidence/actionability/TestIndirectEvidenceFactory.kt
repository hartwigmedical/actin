package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation

object TestIndirectEvidenceFactory {

    data class HotspotEvidence(
        val molecularCriterium: MolecularCriterium,
        val evidence: EfficacyEvidence
    )

    fun createHotspotEvidence(
        hotspot: ActionableHotspot,
        treatmentName: String,
        drugClass: String
    ): HotspotEvidence {
        val molecularCriterium = ImmutableMolecularCriterium.builder()
            .addHotspots(hotspot)
            .build()

        val baseEvidence = TestServeEvidenceFactory.create(
            treatment = treatmentName,
            molecularCriterium = molecularCriterium
        )

        val treatment = ImmutableTreatment.builder()
            .from(baseEvidence.treatment())
            .treatmentApproachesDrugClass(listOf(drugClass))
            .build()

        val evidence = ImmutableEfficacyEvidence.builder()
            .from(baseEvidence)
            .treatment(treatment)
            .build()

        return HotspotEvidence(molecularCriterium, evidence)
    }

    fun createKnownHotspot(
        variant: VariantAnnotation,
        proteinEffect: ProteinEffect,
        associatedWithDrugResistance: Boolean = false
    ): KnownHotspot {
        return TestServeKnownFactory.hotspotBuilder()
            .gene(variant.gene())
            .chromosome(variant.chromosome())
            .position(variant.position())
            .ref(variant.ref())
            .alt(variant.alt())
            .proteinEffect(proteinEffect)
            .associatedWithDrugResistance(associatedWithDrugResistance)
            .build()
    }
}

fun ActionableHotspot.firstVariant(): VariantAnnotation = variants().first()
