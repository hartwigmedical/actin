package com.hartwig.actin.molecular.evidence.orange

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
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
    ): ClinicalEvidence? {
        return nullableCharacteristic?.let { characteristic -> ClinicalEvidenceFactory.create(lookUpEvidence(characteristic)) }
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
        val evidence = if (variant.driverLikelihood == DriverLikelihood.HIGH) {
            ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForVariant(createCriteria(variant)))
        } else {
            ClinicalEvidenceFactory.createNoEvidence()
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
        val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber))
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
        val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruption))
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
        val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForBreakend(disruption))
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
        val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForFusion(createFusionCriteria(fusion)))
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
        val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForVirus(virus))
        return virus.copy(evidence = evidence)
    }
}