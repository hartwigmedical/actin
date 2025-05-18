package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

class PanelEvidenceAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
) : MolecularAnnotator<PanelRecord, PanelRecord> {

    override fun annotate(input: PanelRecord): PanelRecord {
        return annotateWithClinicalEvidence(annotateWithDriverAttributes(input))
    }

    fun annotateWithDriverAttributes(input: PanelRecord): PanelRecord {
        return input.copy(
            drivers = input.drivers.copy(
                variants = annotateVariantsWithDriverAttributes(input.drivers.variants),
                copyNumbers = input.drivers.copyNumbers.map { annotatedCopyNumberWithDriverAttributes(it) },
                fusions = input.drivers.fusions.map { annotateFusionWithDriverAttributes(it) },
            )
        )
    }

    fun annotateWithClinicalEvidence(input: PanelRecord): PanelRecord {
        return input.copy(
            drivers = input.drivers.copy(
                variants = input.drivers.variants.map { annotateVariantWithClinicalEvidence(it) },
                copyNumbers = input.drivers.copyNumbers.map { annotatedCopyNumberWithClinicalEvidence(it) },
                fusions = input.drivers.fusions.map { annotateFusionWithClinicalEvidence(it) },
            )
        )
    }

    private fun annotateVariantsWithDriverAttributes(variants: List<Variant>): List<Variant> {
        val annotatedVariants = variants.map { annotateVariantWithGeneAlteration(it) }
        return annotateVariantsWithDriverLikelihood(annotatedVariants)
    }

    private fun annotateVariantWithGeneAlteration(variant: Variant): Variant {
        val alteration = evidenceDatabase.alterationForVariant(variant)

        return variant.copy(
            isHotspot = alteration.isHotspot,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateVariantsWithDriverLikelihood(variants: List<Variant>): List<Variant> {
        val variantsByGene = variants.groupBy { it.gene }
        return variantsByGene.map {
            val geneRole = it.value.map { variant -> variant.geneRole }.first()
            val likelihood = geneDriverLikelihoodModel.evaluate(it.key, geneRole, it.value)
            likelihood to it.value
        }.flatMap {
            it.second.map { variant ->
                variant.copy(
                    driverLikelihood = DriverLikelihood.from(it.first)
                )
            }
        }
    }

    private fun annotatedCopyNumberWithDriverAttributes(copyNumber: CopyNumber): CopyNumber {
        val alteration = evidenceDatabase.alterationForCopyNumber(copyNumber)
        return copyNumber.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateFusionWithDriverAttributes(fusion: Fusion): Fusion {
        val knownFusion = evidenceDatabase.lookupKnownFusion(fusion)
        val proteinEffect = GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        val isAssociatedWithDrugResistance = knownFusion.associatedWithDrugResistance()

        return fusion.copy(
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = isAssociatedWithDrugResistance
        )
    }

    private fun annotateVariantWithClinicalEvidence(variant: Variant): Variant {
        val evidence = evidenceDatabase.evidenceForVariant(variant)
        return variant.copy(evidence = evidence)
    }

    private fun annotatedCopyNumberWithClinicalEvidence(copyNumber: CopyNumber): CopyNumber {
        val evidence = evidenceDatabase.evidenceForCopyNumber(copyNumber)
        return copyNumber.copy(evidence = evidence)
    }

    private fun annotateFusionWithClinicalEvidence(fusion: Fusion): Fusion {
        val evidence = evidenceDatabase.evidenceForFusion(fusion)
        return fusion.copy(evidence = evidence)
    }
}