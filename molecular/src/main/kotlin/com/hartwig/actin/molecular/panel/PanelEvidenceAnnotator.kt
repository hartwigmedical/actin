package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceMatcher
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

// Needs a better name, clashes with interface MolecularAnnotator. Or maybe this should all be in PanelAnnotator
class PanelEvidenceAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
) : MolecularAnnotator<PanelRecord, PanelRecord> {
    override fun annotate(input: PanelRecord): PanelRecord {
//        return annotateOld(input)
        return annotateNew(input)
    }

    fun annotateNew(input: PanelRecord): PanelRecord {
        val evidenceMatcher = evidenceDatabase.clinicalEvidenceMatcher(input)
        return input.copy(
            drivers =
                input.drivers.copy(
                    variants = input.drivers.variants.map { annotateWithEvidence(evidenceMatcher, it) },
                    copyNumbers = input.drivers.copyNumbers.map { annotatedInferredCopyNumber(evidenceMatcher, it) },
                    fusions = input.drivers.fusions.map { annotateFusion(evidenceMatcher, it) },
                )
        )
    }


//    fun annotateOld(input: PanelRecord): PanelRecord {
//        return input.copy(
//            drivers =
//                input.drivers.copy(
//                    variants = input.drivers.variants.map { annotateWithEvidence(it) },
//                    copyNumbers = input.drivers.copyNumbers.map { annotatedInferredCopyNumber(it) },
//                    fusions = input.drivers.fusions.map { annotateFusion(it) },
//                )
//        )
//    }

    // from PanelVariantAnnotator
    private fun annotateWithEvidence(evidenceMatcher: ClinicalEvidenceMatcher, variant: Variant): Variant {
//        val criteria = MatchingCriteriaFunctions.createVariantCriteria(variant)
        val evidence = evidenceMatcher.matchForVariant(variant)
        return variant.copy(evidence = evidence)
    }

    // from PanelFusionAnnotator
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

    // from PanelCopyNumberAnnotator
    private fun annotatedInferredCopyNumber(evidenceMatcher: ClinicalEvidenceMatcher, copyNumber: CopyNumber): CopyNumber {
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
}