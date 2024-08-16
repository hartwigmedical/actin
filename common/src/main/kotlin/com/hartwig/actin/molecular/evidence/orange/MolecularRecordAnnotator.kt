package com.hartwig.actin.molecular.evidence.orange

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus
import com.hartwig.actin.molecular.evidence.ActionableEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.orange.MolecularRecordAnnotatorFunctions.createCriteria
import com.hartwig.actin.molecular.evidence.orange.MolecularRecordAnnotatorFunctions.createFusionCriteria
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

class MolecularRecordAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<MolecularRecord, MolecularRecord> {

    override fun annotate(input: MolecularRecord): MolecularRecord {
        return input.copy(
            characteristics = annotateCharacteristics(input.characteristics),
            drivers = annotateDrivers(input.drivers),
        )
    }

    private fun annotateCharacteristics(characteristics: MolecularCharacteristics): MolecularCharacteristics {
        return with(characteristics) {
            copy(
                microsatelliteEvidence = createEvidenceForNullableMatch(
                    isMicrosatelliteUnstable, evidenceDatabase::evidenceForMicrosatelliteStatus
                ),
                homologousRepairEvidence = createEvidenceForNullableMatch(
                    isHomologousRepairDeficient, evidenceDatabase::evidenceForHomologousRepairStatus
                ),
                tumorMutationalBurdenEvidence = createEvidenceForNullableMatch(
                    hasHighTumorMutationalBurden, evidenceDatabase::evidenceForTumorMutationalBurdenStatus
                ),
                tumorMutationalLoadEvidence = createEvidenceForNullableMatch(
                    hasHighTumorMutationalLoad, evidenceDatabase::evidenceForTumorMutationalLoadStatus
                )
            )
        }
    }

    private fun createEvidenceForNullableMatch(
        nullableCharacteristic: Boolean?, lookUpEvidence: (Boolean) -> ActionabilityMatch
    ): ActionableEvidence? {
        return nullableCharacteristic?.let { characteristic -> ActionableEvidenceFactory.create(lookUpEvidence(characteristic)) }
    }

    private fun annotateDrivers(drivers: Drivers): Drivers {
        return drivers.copy(
            variants = drivers.variants.map { annotateVariant(it) }.toSet(),
            copyNumbers = drivers.copyNumbers.map { annotateCopyNumber(it) }.toSet(),
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruption(it) }.toSet(),
            disruptions = drivers.disruptions.map { annotateDisruption(it) }.toSet(),
            fusions = drivers.fusions.map { annotateFusion(it) }.toSet(),
            viruses = drivers.viruses.map { annotateVirus(it) }.toSet()
        )
    }


    private fun annotateVariant(variant: Variant): Variant {
        val evidence = if (variant.driverLikelihood == DriverLikelihood.HIGH) {
            ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(createCriteria(variant)))
        } else {
            ActionableEvidenceFactory.createNoEvidence()
        }

        val alteration = GeneAlterationFactory.convertAlteration(
            variant.gene, evidenceDatabase.geneAlterationForVariant(createCriteria(variant))
        )

        return variant.copy(
            evidence = evidence,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber))
        val alteration = GeneAlterationFactory.convertAlteration(
            copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber)
        )

        return copyNumber.copy(
            evidence = evidence,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateHomozygousDisruption(homozygousDisruption: HomozygousDisruption): HomozygousDisruption {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruption))
        val alteration = GeneAlterationFactory.convertAlteration(
            homozygousDisruption.gene, evidenceDatabase.geneAlterationForHomozygousDisruption(homozygousDisruption)
        )

        return homozygousDisruption.copy(
            evidence = evidence,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
        )
    }

    private fun annotateDisruption(disruption: Disruption): Disruption {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForBreakend(disruption))
        val alteration = GeneAlterationFactory.convertAlteration(
            disruption.gene, evidenceDatabase.geneAlterationForBreakend(disruption)
        )

        return disruption.copy(
            evidence = evidence,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
        )
    }

    private fun annotateFusion(fusion: Fusion): Fusion {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForFusion(createFusionCriteria(fusion)))
        val knownFusion = evidenceDatabase.lookupKnownFusion(createFusionCriteria(fusion))

        val proteinEffect = if (knownFusion == null) ProteinEffect.UNKNOWN else {
            GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        }
        val isAssociatedWithDrugResistance = knownFusion?.associatedWithDrugResistance()

        return fusion.copy(
            evidence = evidence,
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = isAssociatedWithDrugResistance
        )
    }

    private fun annotateVirus(virus: Virus): Virus {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVirus(virus))
        return virus.copy(evidence = evidence)
    }
}