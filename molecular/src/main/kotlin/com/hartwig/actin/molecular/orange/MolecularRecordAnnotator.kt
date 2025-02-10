package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions.createFusionCriteria
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions.createVariantCriteria
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

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
                microsatelliteEvidence = createEvidenceForNullableMatch(
                    isMicrosatelliteUnstable, evidenceDatabase::evidenceForMicrosatelliteStatus
                ),
                homologousRecombinationEvidence = createEvidenceForNullableMatch(
                    isHomologousRecombinationDeficient, evidenceDatabase::evidenceForHomologousRecombinationStatus
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
        nullableCharacteristic: Boolean?, lookUpEvidence: (Boolean) -> ClinicalEvidence
    ): ClinicalEvidence? {
        return nullableCharacteristic?.let { characteristic -> lookUpEvidence(characteristic) }
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
        val evidence = evidenceDatabase.evidenceForVariant(createVariantCriteria(variant))
        val alteration = GeneAlterationFactory.convertAlteration(
            variant.gene, evidenceDatabase.geneAlterationForVariant(createVariantCriteria(variant))
        )

        return variant.copy(
            evidence = evidence,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val evidence = evidenceDatabase.evidenceForCopyNumber(copyNumber)
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
        val evidence = evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruption)
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
        val alteration = GeneAlterationFactory.convertAlteration(disruption.gene, evidenceDatabase.geneAlterationForDisruption(disruption))
        val disruptionWithGeneAlterations = disruption.copy(
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
        )
        val evidence = evidenceDatabase.evidenceForDisruption(disruptionWithGeneAlterations)
        return disruptionWithGeneAlterations.copy(evidence = evidence)
    }

    private fun annotateFusion(fusion: Fusion): Fusion {
        val evidence = evidenceDatabase.evidenceForFusion(createFusionCriteria(fusion))
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
        val evidence = evidenceDatabase.evidenceForVirus(virus)
        return virus.copy(evidence = evidence)
    }
}