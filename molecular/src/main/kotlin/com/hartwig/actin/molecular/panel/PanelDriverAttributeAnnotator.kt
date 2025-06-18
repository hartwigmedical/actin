package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

typealias PanelRecordWithDriverAttributes = PanelRecord

class PanelDriverAttributeAnnotator(
    private val knownEventResolver: KnownEventResolver,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
) : MolecularAnnotator<PanelRecord, PanelRecordWithDriverAttributes> {

    override fun annotate(input: PanelRecord): PanelRecordWithDriverAttributes {
        return input.copy(
            drivers = input.drivers.copy(
                variants = annotateVariantsWithDriverAttributes(input.drivers.variants),
                copyNumbers = input.drivers.copyNumbers.map { annotatedCopyNumberWithDriverAttributes(it) },
                fusions = input.drivers.fusions.map { annotateFusionWithDriverAttributes(it) },
            )
        )
    }

    private fun annotateVariantsWithDriverAttributes(variants: List<Variant>): List<Variant> {
        val annotatedVariants = variants.map { annotateVariantWithGeneAlteration(it) }
        return annotateVariantsWithDriverLikelihood(annotatedVariants)
    }

    private fun annotateVariantWithGeneAlteration(variant: Variant): Variant {
        val alteration = knownEventResolver.resolveForVariant(variant)

        return variant.copy(
            isCancerAssociatedVariant = alteration.isCancerAssociatedVariant,
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
        val alteration = knownEventResolver.resolveForCopyNumber(copyNumber)
        return copyNumber.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateFusionWithDriverAttributes(fusion: Fusion): Fusion {
        val knownFusion = knownEventResolver.resolveForFusion(fusion)
        val proteinEffect = GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        val isAssociatedWithDrugResistance = knownFusion.associatedWithDrugResistance()

        return fusion.copy(
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = isAssociatedWithDrugResistance
        )
    }
}