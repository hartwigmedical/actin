package com.hartwig.actin.molecular.panel

import com.hartwig.actin.configuration.MolecularConfiguration
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.driverlikelihood.DndsModel
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

class PanelDriverAttributeAnnotator(
    private val knownEventResolver: KnownEventResolver,
    private val dndsDatabase: DndsDatabase,
    private val configuration: MolecularConfiguration
) : MolecularAnnotator<MolecularTest> {

    override fun annotate(input: MolecularTest): MolecularTest {
        val geneDriverLikelihoodModel =
            GeneDriverLikelihoodModel(DndsModel.create(dndsDatabase, input.characteristics.tumorMutationalBurden))
        return input.copy(
            drivers = input.drivers.copy(
                variants = annotateVariantsWithDriverAttributes(input.drivers.variants, geneDriverLikelihoodModel),
                copyNumbers = input.drivers.copyNumbers.map { annotatedCopyNumberWithDriverAttributes(it) },
                fusions = input.drivers.fusions.map { annotateFusionWithDriverAttributes(it) },
            )
        )
    }

    private fun annotateVariantsWithDriverAttributes(
        variants: List<Variant>,
        geneDriverLikelihoodModel: GeneDriverLikelihoodModel
    ): List<Variant> {
        val annotatedVariants = variants.map { annotateVariantWithGeneAlteration(it) }
        return annotateVariantsWithDriverLikelihood(annotatedVariants, geneDriverLikelihoodModel)
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

    private fun annotateVariantsWithDriverLikelihood(
        variants: List<Variant>,
        geneDriverLikelihoodModel: GeneDriverLikelihoodModel
    ): List<Variant> {
        val variantsByGene = variants.groupBy { it.gene }
        return variantsByGene.map {
            val geneRole = it.value.map { variant -> variant.geneRole }.first()
            val likelihood = geneDriverLikelihoodModel.evaluate(it.key, geneRole, it.value, configuration)
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