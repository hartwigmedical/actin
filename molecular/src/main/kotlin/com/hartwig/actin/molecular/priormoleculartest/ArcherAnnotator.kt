package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.ActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory


class ArcherAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<ArcherPanelExtraction, PanelRecord> {
    override fun annotate(input: ArcherPanelExtraction): PanelRecord {
        val annotatedVariants = input.variants.map {
            val criteria = VariantMatchCriteria(
                true,
                it.gene
            )
            val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(criteria))
            val geneAlteration = GeneAlterationFactory.convertAlteration(
                it.gene, evidenceDatabase.geneAlterationForVariant(criteria)
            )
            Variant(
                isReportable = true,
                event = "${it.gene} ${it.hgvsCodingImpact}",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = evidence ?: ActionableEvidence(),
                gene = it.gene,
                geneRole = geneAlteration.geneRole,
                proteinEffect = geneAlteration.proteinEffect,
                isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
                isHotspot = true,
                ref = "",
                alt = "",
                canonicalImpact = TranscriptImpact(
                    transcriptId = "",
                    hgvsCodingImpact = it.hgvsCodingImpact,
                    hgvsProteinImpact = "",
                    isSpliceRegion = false
                ),
                chromosome = "",
                position = 0,
                type = VariantType.SNV
            )
        }

        return PanelRecord(
            archerPanelExtraction = input,
            type = ExperimentType.ARCHER,
            date = input.date,
            drivers = Drivers(variants = annotatedVariants.toSet()),
            characteristics = MolecularCharacteristics(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }
}