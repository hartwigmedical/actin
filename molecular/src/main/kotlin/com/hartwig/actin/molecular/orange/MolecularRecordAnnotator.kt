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
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.actin.molecular.hotspot.HotspotFunctions
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.util.ExtractionUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val LOGGER: Logger = LogManager.getLogger(MolecularRecordAnnotator::class.java)

class MolecularRecordAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<MolecularRecord, MolecularRecord> {

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
            val evidence =
                it.isDeficient?.let { isDeficient -> evidenceDatabase.evidenceForHomologousRecombinationStatus(isDeficient) }
                    ?: ExtractionUtil.noEvidence()
            it.copy(evidence = evidence)
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
            variants = drivers.variants.map { annotateVariant(it) },
            copyNumbers = drivers.copyNumbers.map { annotateCopyNumber(it) },
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruption(it) },
            disruptions = drivers.disruptions.map { annotateDisruption(it) },
            fusions = drivers.fusions.map { annotateFusion(it) },
            viruses = drivers.viruses.map { annotateVirus(it) }
        )
    }

    private fun annotateVariant(variant: Variant): Variant {
        val geneAlteration =
            evidenceDatabase.geneAlterationsForVariant(MatchingCriteriaFunctions.createVariantCriteria(variant)).firstOrNull()
        val isServeHotspot = HotspotFunctions.isHotspot(geneAlteration)
        val alteration = GeneAlterationFactory.convertAlteration(variant.gene, geneAlteration)

        if (!variant.isHotspot && isServeHotspot) {
            LOGGER.info("Overwriting isHotspot to true and setting driverLikelihood to HIGH for ${variant.event}")
        }

        val variantWithGeneAlteration = variant.copy(
            isHotspot = variant.isHotspot || isServeHotspot,
            driverLikelihood = if (isServeHotspot) DriverLikelihood.HIGH else variant.driverLikelihood,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceDatabase.evidenceForVariant(MatchingCriteriaFunctions.createVariantCriteria(variantWithGeneAlteration))
        return variantWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val alteration =
            GeneAlterationFactory.convertAlteration(copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber))
        val copyNumberWithGeneAlteration = copyNumber.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceDatabase.evidenceForCopyNumber(copyNumberWithGeneAlteration)
        return copyNumberWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateHomozygousDisruption(homozygousDisruption: HomozygousDisruption): HomozygousDisruption {
        val alteration = GeneAlterationFactory.convertAlteration(
            homozygousDisruption.gene,
            evidenceDatabase.geneAlterationForHomozygousDisruption(homozygousDisruption)
        )
        val homozygousDisruptionWithGeneAlteration = homozygousDisruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruptionWithGeneAlteration)
        return homozygousDisruptionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateDisruption(disruption: Disruption): Disruption {
        val alteration = GeneAlterationFactory.convertAlteration(disruption.gene, evidenceDatabase.geneAlterationForDisruption(disruption))
        val disruptionWithGeneAlteration = disruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
        )
        val evidence = evidenceDatabase.evidenceForDisruption(disruptionWithGeneAlteration)
        return disruptionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateFusion(fusion: Fusion): Fusion {
        val knownFusion = evidenceDatabase.lookupKnownFusion(MatchingCriteriaFunctions.createFusionCriteria(fusion))
        val proteinEffect = if (knownFusion == null) ProteinEffect.UNKNOWN else {
            GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        }
        val isAssociatedWithDrugResistance = knownFusion?.associatedWithDrugResistance()
        val fusionWithGeneAlteration =
            fusion.copy(proteinEffect = proteinEffect, isAssociatedWithDrugResistance = isAssociatedWithDrugResistance)
        val evidence = evidenceDatabase.evidenceForFusion(MatchingCriteriaFunctions.createFusionCriteria(fusionWithGeneAlteration))
        return fusionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateVirus(virus: Virus): Virus {
        val evidence = evidenceDatabase.evidenceForVirus(virus)
        return virus.copy(evidence = evidence)
    }
}