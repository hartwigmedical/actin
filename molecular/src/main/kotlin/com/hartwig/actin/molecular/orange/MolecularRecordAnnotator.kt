package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihoodComparator
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.MolecularAnnotatorFunctions
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MolecularRecordAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<MolecularRecord, MolecularRecord> {

    private val logger: Logger = LogManager.getLogger(MolecularRecordAnnotator::class.java)

    override fun annotate(input: MolecularRecord): MolecularRecord {
        return input.copy(
            characteristics = annotateCharacteristics(input.characteristics),
            drivers = annotateDrivers(input.drivers)
        )
    }

    private fun annotateCharacteristics(characteristics: MolecularCharacteristics): MolecularCharacteristics {
        return with(characteristics) {
            copy(
                microsatelliteStability = annotateMicrosatelliteStability(microsatelliteStability),
                homologousRecombination = annotateHomologousRecombination(homologousRecombination),
                tumorMutationalBurden = annotateTumorMutationalBurden(tumorMutationalBurden),
                tumorMutationalLoad = annotateTumorMutationalLoad(tumorMutationalLoad)
            )
        }
    }

    private fun annotateMicrosatelliteStability(microsatelliteStability: MicrosatelliteStability?): MicrosatelliteStability? {
        return microsatelliteStability?.let {
            it.copy(evidence = evidenceDatabase.evidenceForMicrosatelliteStatus(it.isUnstable))
        }
    }

    private fun annotateHomologousRecombination(homologousRecombination: HomologousRecombination?): HomologousRecombination? {
        return homologousRecombination?.let {
            it.copy(evidence = evidenceDatabase.evidenceForHomologousRecombinationStatus(it.isDeficient))
        }
    }

    private fun annotateTumorMutationalBurden(tumorMutationalBurden: TumorMutationalBurden?): TumorMutationalBurden? {
        return tumorMutationalBurden?.let {
            it.copy(evidence = evidenceDatabase.evidenceForTumorMutationalBurdenStatus(it.isHigh))
        }
    }

    private fun annotateTumorMutationalLoad(tumorMutationalLoad: TumorMutationalLoad?): TumorMutationalLoad? {
        return tumorMutationalLoad?.let {
            it.copy(evidence = evidenceDatabase.evidenceForTumorMutationalLoadStatus(it.isHigh))
        }
    }

    private fun annotateDrivers(drivers: Drivers): Drivers {
        return drivers.copy(
            variants = reannotateDriverLikelihood(drivers.variants.map { annotateVariant(it) }),
            copyNumbers = drivers.copyNumbers.map { annotateCopyNumber(it) },
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruption(it) },
            disruptions = drivers.disruptions.map { annotateDisruption(it) },
            fusions = drivers.fusions.map { annotateFusion(it) },
            viruses = drivers.viruses.map { annotateVirus(it) }
        )
    }

    fun annotateVariant(variant: Variant): Variant {
        val alteration = evidenceDatabase.alterationForVariant(variant)

        if (!variant.isCancerAssociatedVariant && alteration.isCancerAssociatedVariant) {
            logger.info("Overwriting isCancerAssociatedVariant to true and setting driverLikelihood to HIGH for ${variant.event}")
        }

        val proteinEffect = MolecularAnnotatorFunctions.annotateProteinEffect(variant, alteration)

        val variantWithGeneAlteration = variant.copy(
            isCancerAssociatedVariant = alteration.isCancerAssociatedVariant,
            driverLikelihood = if (alteration.isCancerAssociatedVariant || proteinEffect == ProteinEffect.LOSS_OF_FUNCTION) DriverLikelihood.HIGH else variant.driverLikelihood,
            geneRole = alteration.geneRole,
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceDatabase.evidenceForVariant(variantWithGeneAlteration)
        return variantWithGeneAlteration.copy(evidence = evidence)
    }

    fun reannotateDriverLikelihood(variants: List<Variant>): List<Variant> {
        val variantsByGene = variants.groupBy { it.gene }
        return variantsByGene.flatMap { (_, geneVariants) ->
            val maxDriverLikelihood = geneVariants.map { it.driverLikelihood }.sortedWith(DriverLikelihoodComparator()).firstOrNull()
            geneVariants.map { variant -> variant.copy(driverLikelihood = if (variant.driverLikelihood != null) maxDriverLikelihood else null) }
        }
    }

    private fun annotateCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val alteration = evidenceDatabase.alterationForCopyNumber(copyNumber)
        val copyNumberWithGeneAlteration = copyNumber.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceDatabase.evidenceForCopyNumber(copyNumberWithGeneAlteration)
        return copyNumberWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateHomozygousDisruption(homozygousDisruption: HomozygousDisruption): HomozygousDisruption {
        val alteration = evidenceDatabase.alterationForHomozygousDisruption(homozygousDisruption)
        val homozygousDisruptionWithGeneAlteration = homozygousDisruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruptionWithGeneAlteration)
        return homozygousDisruptionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateDisruption(disruption: Disruption): Disruption {
        val alteration = evidenceDatabase.alterationForDisruption(disruption)
        val disruptionWithGeneAlteration = disruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
        )
        val evidence = evidenceDatabase.evidenceForDisruption(disruptionWithGeneAlteration)
        return disruptionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateFusion(fusion: Fusion): Fusion {
        val knownFusion = evidenceDatabase.lookupKnownFusion(fusion)
        val proteinEffect = GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        val isAssociatedWithDrugResistance = knownFusion.associatedWithDrugResistance()
        val fusionWithGeneAlteration =
            fusion.copy(proteinEffect = proteinEffect, isAssociatedWithDrugResistance = isAssociatedWithDrugResistance)
        val evidence = evidenceDatabase.evidenceForFusion(fusionWithGeneAlteration)
        return fusionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateVirus(virus: Virus): Virus {
        val evidence = evidenceDatabase.evidenceForVirus(virus)
        return virus.copy(evidence = evidence)
    }
}