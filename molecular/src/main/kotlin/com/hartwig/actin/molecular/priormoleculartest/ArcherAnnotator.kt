package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariantAnnotation
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.ActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory


class ArcherAnnotator(private val evidenceDatabase: EvidenceDatabase) : MolecularAnnotator<ArcherPanel> {
    override fun annotate(input: ArcherPanel): ArcherPanel {
        val annotatedVariants = input.variants.map {
            val criteria = VariantMatchCriteria(
                true,
                it.gene,
                it.codingEffect,
                it.type,
                it.chromosome,
                it.position,
                it.ref,
                it.alt
            )
            val evidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForVariant(
                    criteria
                )
            )
            val geneAlteration = GeneAlterationFactory.convertAlteration(
                it.gene, evidenceDatabase.geneAlterationForVariant(criteria)
            )
            val knownExon = evidenceDatabase.knownExonAlterationForVariant(criteria)
            val knownCodon = evidenceDatabase.knownCodonAlterationForVariant(criteria)
            it.copy(
                annotation = ArcherVariantAnnotation(
                    evidence = evidence,
                    geneRole = geneAlteration.geneRole,
                    proteinEffect = geneAlteration.proteinEffect,
                    isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
                    exonRank = knownExon?.inputExonRank(),
                    codonRank = knownCodon?.inputCodonRank()
                )
            )
        }

        val annotatedFusions = input.fusions.map {
            val criteria =
                FusionMatchCriteria(geneStart = it.gene, geneEnd = it.gene, driverType = FusionDriverType.NONE, isReportable = true)
            val evidence = ActionableEvidenceFactory.create(
                evidenceDatabase.evidenceForFusion(
                    criteria
                )
            )
            it.copy(evidence = evidence)
        }

        return input.copy(variants = annotatedVariants, fusions = annotatedFusions)
    }
}