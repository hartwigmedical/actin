package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceMatcher
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.util.ExtractionUtil

class MolecularRecordAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<MolecularRecord, MolecularRecord> {

    override fun annotate(input: MolecularRecord): MolecularRecord {

        // TODO maybe reorganize to evidence matcher this an instance var, but then the test needs to be as well, which seems odd
        //  can maybe delegate to a new internal object?
        val evidenceMatcher = evidenceDatabase.clinicalEvidenceMatcher(input)

        return input.copy(
            characteristics = annotateCharacteristics(evidenceMatcher, input.characteristics),
            drivers = annotateDrivers(evidenceMatcher, input.drivers)
        )
    }

    private fun annotateCharacteristics(
        evidenceMatcher: ClinicalEvidenceMatcher,
        characteristics: MolecularCharacteristics
    ): MolecularCharacteristics {
        return with(characteristics) {
            copy(
                microsatelliteStability = annotateMicrosatelliteStability(evidenceMatcher, microsatelliteStability),
                homologousRecombination = annotateHomologousRecombination(evidenceMatcher, homologousRecombination),
                tumorMutationalBurden = annotateTumorMutationalBurden(evidenceMatcher, tumorMutationalBurden),
                tumorMutationalLoad = annotateTumorMutationalLoad(evidenceMatcher, tumorMutationalLoad)
            )
        }
    }

    private fun annotateMicrosatelliteStability(
        evidenceMatcher: ClinicalEvidenceMatcher,
        microsatelliteStability: MicrosatelliteStability?
    ): MicrosatelliteStability? {
        return microsatelliteStability?.let {
            it.copy(evidence = evidenceMatcher.matchForMicrosatelliteStatus(it.isUnstable))
        }
    }

    private fun annotateHomologousRecombination(
        evidenceMatcher: ClinicalEvidenceMatcher,
        homologousRecombination: HomologousRecombination?
    ): HomologousRecombination? {
        return homologousRecombination?.let {
            val evidence =
                it.isDeficient?.let { isDeficient -> evidenceMatcher.matchForHomologousRecombinationStatus(isDeficient) }
                    ?: ExtractionUtil.noEvidence()
            it.copy(evidence = evidence)
        }
    }

    private fun annotateTumorMutationalBurden(
        evidenceMatcher: ClinicalEvidenceMatcher,
        tumorMutationalBurden: TumorMutationalBurden?
    ): TumorMutationalBurden? {
        return tumorMutationalBurden?.let {
            it.copy(evidence = evidenceMatcher.matchForHighTumorMutationalBurden(it.isHigh))
        }
    }

    private fun annotateTumorMutationalLoad(
        evidenceMatcher: ClinicalEvidenceMatcher,
        tumorMutationalLoad: TumorMutationalLoad?
    ): TumorMutationalLoad? {
        return tumorMutationalLoad?.let {
            it.copy(evidence = evidenceMatcher.matchForHighTumorMutationalLoad(it.isHigh))
        }
    }

    private fun annotateDrivers(evidenceMatcher: ClinicalEvidenceMatcher, drivers: Drivers): Drivers {
        return drivers.copy(
            variants = drivers.variants.map { annotateVariant(evidenceMatcher, it) },
            copyNumbers = drivers.copyNumbers.map { annotateCopyNumber(evidenceMatcher, it) },
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruption(evidenceMatcher, it) },
            disruptions = drivers.disruptions.map { annotateDisruption(evidenceMatcher, it) },
            fusions = drivers.fusions.map { annotateFusion(evidenceMatcher, it) },
            viruses = drivers.viruses.map { annotateVirus(evidenceMatcher, it) }
        )
    }

    private fun annotateVariant(evidenceMatcher: ClinicalEvidenceMatcher, variant: Variant): Variant {
        val alteration =
            GeneAlterationFactory.convertAlteration(
                variant.gene,
                evidenceDatabase.geneAlterationForVariant(MatchingCriteriaFunctions.createVariantCriteria(variant))
            )
        val variantWithGeneAlteration = variant.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceMatcher.matchForVariant(variantWithGeneAlteration)
        return variantWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateCopyNumber(evidenceMatcher: ClinicalEvidenceMatcher, copyNumber: CopyNumber): CopyNumber {
        val alteration =
            GeneAlterationFactory.convertAlteration(copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber))
        val copyNumberWithGeneAlteration = copyNumber.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceMatcher.matchForCopyNumber(copyNumberWithGeneAlteration)
        return copyNumberWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateHomozygousDisruption(
        evidenceMatcher: ClinicalEvidenceMatcher,
        homozygousDisruption: HomozygousDisruption
    ): HomozygousDisruption {
        val alteration = GeneAlterationFactory.convertAlteration(
            homozygousDisruption.gene,
            evidenceDatabase.geneAlterationForHomozygousDisruption(homozygousDisruption)
        )
        val homozygousDisruptionWithGeneAlteration = homozygousDisruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
        val evidence = evidenceMatcher.matchForHomozygousDisruption(homozygousDisruptionWithGeneAlteration)
        return homozygousDisruptionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateDisruption(evidenceMatcher: ClinicalEvidenceMatcher, disruption: Disruption): Disruption {
        val alteration = GeneAlterationFactory.convertAlteration(disruption.gene, evidenceDatabase.geneAlterationForDisruption(disruption))
        val disruptionWithGeneAlteration = disruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
        )
        val evidence = evidenceMatcher.matchForDisruption(disruptionWithGeneAlteration)
        return disruptionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateFusion(evidenceMatcher: ClinicalEvidenceMatcher, fusion: Fusion): Fusion {
        val knownFusion = evidenceDatabase.lookupKnownFusion(MatchingCriteriaFunctions.createFusionCriteria(fusion))
        val proteinEffect = if (knownFusion == null) ProteinEffect.UNKNOWN else {
            GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        }
        val isAssociatedWithDrugResistance = knownFusion?.associatedWithDrugResistance()
        val fusionWithGeneAlteration =
            fusion.copy(proteinEffect = proteinEffect, isAssociatedWithDrugResistance = isAssociatedWithDrugResistance)
        val evidence = evidenceMatcher.matchForFusion(fusionWithGeneAlteration)
        return fusionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotateVirus(evidenceMatcher: ClinicalEvidenceMatcher, virus: Virus): Virus {
        val evidence = evidenceMatcher.matchForVirus(virus)
        return virus.copy(evidence = evidence)
    }
}