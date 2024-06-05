package com.hartwig.actin.molecular.orange

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.hmf.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.hmf.driver.Disruption
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExtendedFusion
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExtendedVariant
import com.hartwig.actin.molecular.datamodel.hmf.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.hmf.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.hmf.driver.Virus
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.ActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory

class MolecularRecordAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<MolecularRecord, MolecularRecord> {

    override fun annotate(input: MolecularRecord): MolecularRecord {
        return input.copy(
            characteristics = annotateCharacteristics(input.characteristics),
            drivers = annotateDrivers(input.drivers),
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
            variants = drivers.variants.map { annotateVariant(it) }.toSet(),
            copyNumbers = drivers.copyNumbers.map { annotateCopyNumber(it) }.toSet(),
            homozygousDisruptions = drivers.homozygousDisruptions.map { annotateHomozygousDisruption(it) }.toSet(),
            disruptions = drivers.disruptions.map { annotateDisruption(it) }.toSet(),
            fusions = drivers.fusions.map { annotateFusion(it) }.toSet(),
            viruses = drivers.viruses.map { annotateViruse(it) }.toSet()
        )
    }


    private fun annotateVariant(variant: ExtendedVariant): ExtendedVariant {
        val evidence = if (variant.driverLikelihood == DriverLikelihood.HIGH) {
            ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForVariant(
                    createCriteria(variant)
                )
            )!!
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

    private fun createCriteria(variant: ExtendedVariant) = VariantMatchCriteria(
        gene = variant.gene,
        chromosome = variant.chromosome,
        position = variant.position,
        ref = variant.ref,
        alt = variant.alt,
        type = variant.type,
        codingEffect = variant.canonicalImpact.codingEffect,
        isReportable = variant.isReportable
    )

    private fun annotateCopyNumber(copyNumber: CopyNumber): CopyNumber {
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

    private fun annotateHomozygousDisruption(homozygousDisruption: HomozygousDisruption): HomozygousDisruption {
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

    private fun annotateDisruption(disruption: Disruption): Disruption {
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

    private fun annotateFusion(fusion: ExtendedFusion): ExtendedFusion {
        val evidence =
            ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForFusion(
                    createFusionCriteria(fusion)
                )
            )!!
        val knownFusion = evidenceDatabase.lookupKnownFusion(createFusionCriteria(fusion))

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

    private fun createFusionCriteria(fusion: ExtendedFusion) = FusionMatchCriteria(
        isReportable = fusion.isReportable,
        geneStart = fusion.geneStart,
        geneEnd = fusion.geneEnd,
        driverType = fusion.driverType
    )

    private fun annotateViruse(virus: Virus): Virus {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVirus(virus))!!
        return virus.copy(evidence = evidence)
    }
}