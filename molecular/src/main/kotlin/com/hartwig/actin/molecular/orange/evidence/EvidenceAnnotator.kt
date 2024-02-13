package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.orange.interpretation.ActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory

class EvidenceAnnotator(private val evidenceDatabase: EvidenceDatabase) {
    fun annotate(record: MolecularRecord): MolecularRecord {
        return record.copy(
            characteristics = annotateCharacteristics(record.characteristics),
            drivers = annotateDrivers(record.drivers),
        )
    }

    private fun annotateCharacteristics(characteristics: MolecularCharacteristics): MolecularCharacteristics {
        return characteristics.copy(
            microsatelliteEvidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForMicrosatelliteStatus(characteristics.isMicrosatelliteUnstable)
            ),
            homologousRepairEvidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForHomologousRepairStatus(characteristics.isHomologousRepairDeficient)
            ),
            tumorMutationalBurdenEvidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForTumorMutationalBurdenStatus(characteristics.hasHighTumorMutationalBurden)
            ),
            tumorMutationalLoadEvidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForTumorMutationalLoadStatus(characteristics.hasHighTumorMutationalLoad)
            ),
        )
    }

    private fun annotateDrivers(drivers: MolecularDrivers): MolecularDrivers {
        return drivers.copy(
            variants = drivers.variants.map { annotateVariants(it) }.toSet(),
            copyNumbers = drivers.copyNumbers.map { annotateCopyNumbers(it) }.toSet(),
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruptions(it) }.toSet(),
            disruptions = drivers.disruptions.map { annotateDisruptions(it) }.toSet(),
            fusions = drivers.fusions.map { annotateFusions(it) }.toSet(),
            viruses = drivers.viruses.map { annotateViruses(it) }.toSet()
        )
    }


    private fun annotateVariants(variant: Variant): Variant {
        val evidence = if (variant.driverLikelihood == DriverLikelihood.HIGH) {
            ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(variant))!!
        } else {
            ActionableEvidenceFactory.createNoEvidence()
        }

        val alteration = GeneAlterationFactory.convertAlteration(
            variant.gene, evidenceDatabase.geneAlterationForVariant(variant)
        )

        return variant.copy(
            evidence = evidence,
            geneRole = alteration.geneRole,
            proteinEffect = alteration.proteinEffect,
            isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateCopyNumbers(copyNumber: CopyNumber): CopyNumber {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber))!!
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

    private fun annotateHomozygousDisruptions(homozygousDisruption: HomozygousDisruption): HomozygousDisruption {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruption))!!
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

    private fun annotateDisruptions(disruption: Disruption): Disruption {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForBreakend(disruption))!!
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

    private fun annotateFusions(fusion: Fusion): Fusion {
        val knownFusion = evidenceDatabase.lookupKnownFusion(fusion)

        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForFusion(fusion))!!

        val proteinEffect = if (knownFusion == null) ProteinEffect.UNKNOWN else {
            GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        }
        val isAssociatedWithDrugResistance = knownFusion?.associatedWithDrugResistance()

        return fusion.copy(
            evidence = evidence,
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = isAssociatedWithDrugResistance,
        )
    }

    private fun annotateViruses(virus: Virus): Virus {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVirus(virus))!!
        return virus.copy(evidence = evidence)
    }
}