package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihoodComparator
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MolecularRecordAnnotator(private val knownEventResolver: KnownEventResolver) : MolecularAnnotator<MolecularTest> {

    private val logger: Logger = LogManager.getLogger(MolecularRecordAnnotator::class.java)

    override fun annotate(input: MolecularRecord): MolecularRecord {
        return input.copy(
            drivers = annotateDrivers(input.drivers)
        )
    }

    private fun annotateDrivers(drivers: Drivers): Drivers {
        return drivers.copy(
            variants = reannotateDriverLikelihood(drivers.variants.map { annotateVariant(it) }),
            copyNumbers = drivers.copyNumbers.map { annotateCopyNumber(it) },
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruption(it) },
            disruptions = drivers.disruptions.map { annotateDisruption(it) },
            fusions = drivers.fusions.map { annotateFusion(it) },
        )
    }

    fun annotateVariant(variant: Variant): Variant {
        val alteration = knownEventResolver.resolveForVariant(variant)

        if (!variant.isCancerAssociatedVariant && alteration.isCancerAssociatedVariant) {
            logger.info("Overwriting isCancerAssociatedVariant to true and setting driverLikelihood to HIGH for ${variant.event}")
        }

        return variant.copy(
            isCancerAssociatedVariant = alteration.isCancerAssociatedVariant,
            driverLikelihood = if (alteration.isCancerAssociatedVariant) DriverLikelihood.HIGH else variant.driverLikelihood,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    fun reannotateDriverLikelihood(variants: List<Variant>): List<Variant> {
        val variantsByGene = variants.groupBy { it.gene }
        return variantsByGene.flatMap { (_, geneVariants) ->
            val maxDriverLikelihood = geneVariants.map { it.driverLikelihood }.sortedWith(DriverLikelihoodComparator()).firstOrNull()
            geneVariants.map { variant -> variant.copy(driverLikelihood = if (variant.driverLikelihood != null) maxDriverLikelihood else null) }
        }
    }

    private fun annotateCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val alteration = knownEventResolver.resolveForCopyNumber(copyNumber)
        return copyNumber.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateHomozygousDisruption(homozygousDisruption: HomozygousDisruption): HomozygousDisruption {
        val alteration = knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption)
        return homozygousDisruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateDisruption(disruption: Disruption): Disruption {
        val alteration = knownEventResolver.resolveForDisruption(disruption)
        return disruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
        )
    }

    private fun annotateFusion(fusion: Fusion): Fusion {
        val knownFusion = knownEventResolver.resolveForFusion(fusion)
        val proteinEffect = GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        val isAssociatedWithDrugResistance = knownFusion.associatedWithDrugResistance()
        return fusion.copy(proteinEffect = proteinEffect, isAssociatedWithDrugResistance = isAssociatedWithDrugResistance)
    }
}