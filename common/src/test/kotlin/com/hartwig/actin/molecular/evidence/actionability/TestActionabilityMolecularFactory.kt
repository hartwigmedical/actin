package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect as DriverProteinEffect

object TestIndirectEvidenceFactory {

    val BRAF_V600E_VARIANT: VariantAnnotation = TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF",
        chromosome = "7",
        position = 140453136,
        ref = "A",
        alt = "T"
    )
    val BRAF_T599R_VARIANT: VariantAnnotation = TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF",
        chromosome = "7",
        position = 140453139,
        ref = "G",
        alt = "C"
    )
    val KRAS_G12V_VARIANT: VariantAnnotation = TestServeMolecularFactory.createVariantAnnotation(
        gene = "KRAS",
        chromosome = "12",
        position = 25245350,
        ref = "C",
        alt = "A"
    )
    val PIK3CA_E545K_VARIANT: VariantAnnotation = TestServeMolecularFactory.createVariantAnnotation(
        gene = "PIK3CA",
        chromosome = "3",
        position = 178936091,
        ref = "G",
        alt = "A"
    )

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
            .addSources(Knowledgebase.CKB)
            .build()
    }

    fun createVariant(annotation: VariantAnnotation, proteinEffect: DriverProteinEffect) =
        TestVariantFactory.createMinimal().copy(
            gene = annotation.gene(),
            chromosome = annotation.chromosome(),
            position = annotation.position(),
            ref = annotation.ref(),
            alt = annotation.alt(),
            driverLikelihood = DriverLikelihood.HIGH,
            isReportable = true,
            proteinEffect = proteinEffect,
        )
}

fun ActionableHotspot.firstVariant(): VariantAnnotation = variants().first()
