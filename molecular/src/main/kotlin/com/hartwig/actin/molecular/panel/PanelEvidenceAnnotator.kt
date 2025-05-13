package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

class PanelEvidenceAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
) : MolecularAnnotator<PanelRecord, PanelRecord> {
    
    override fun annotate(input: PanelRecord): PanelRecord {
        return input.copy(
            drivers =
                input.drivers.copy(
                    variants = annotateVariants(input.drivers.variants),
                    copyNumbers = input.drivers.copyNumbers.map { annotatedCopyNumberWithEvidence(it) },
                    fusions = input.drivers.fusions.map { annotateFusionWithEvidence(it) },
                )
        )
    }

    private fun annotateVariants(variants: List<Variant>): List<Variant> {
        val annotatedVariants = variants.map { annotateWithGeneAlteration(it) }
        return annotateWithDriverLikelihood(annotatedVariants).map { annotateVariantWithEvidence(it) }
    }

    private fun annotateVariantWithEvidence(variant: Variant): Variant {
        val criteria = MatchingCriteriaFunctions.createVariantCriteria(variant)
        val evidence = evidenceDatabase.evidenceForVariant(criteria)
        return variant.copy(evidence = evidence)
    }

    private fun annotateWithGeneAlteration(variant: Variant): Variant {
        val criteria = MatchingCriteriaFunctions.createVariantCriteria(variant)
        val serveGeneAlteration = evidenceDatabase.geneAlterationForVariant(criteria)
        val geneAlteration = GeneAlterationFactory.convertAlteration(variant.gene, serveGeneAlteration)

        return variant.copy(
            isHotspot = isHotspot(serveGeneAlteration),
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateWithDriverLikelihood(variants: List<Variant>): List<Variant> {
        val variantsByGene = variants.groupBy { it.gene }
        return variantsByGene.map {
            val geneRole = it.value.map { variant -> variant.geneRole }.first()
            println("${it.value}")
            val likelihood = geneDriverLikelihoodModel.evaluate(it.key, geneRole, it.value)
            likelihood to it.value
        }.flatMap {
            it.second.map { variant ->
                variant.copy(
                    driverLikelihood = DriverLikelihood.from(it.first)
                )
            }
        }
    }

    private fun annotateFusionWithEvidence(fusion: Fusion): Fusion {
        val knownFusion = evidenceDatabase.lookupKnownFusion(MatchingCriteriaFunctions.createFusionCriteria(fusion))

        val proteinEffect = when (knownFusion) {
            null -> ProteinEffect.UNKNOWN
            else -> GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        }

        val isAssociatedWithDrugResistance = knownFusion?.associatedWithDrugResistance()

        val fusionWithGeneAlteration = fusion.copy(
            proteinEffect = proteinEffect,
            isAssociatedWithDrugResistance = isAssociatedWithDrugResistance
        )

        val matchingCriteria = MatchingCriteriaFunctions.createFusionCriteria(fusionWithGeneAlteration)
        val evidence = evidenceDatabase.evidenceForFusion(matchingCriteria)
        return fusionWithGeneAlteration.copy(evidence = evidence)
    }

    private fun annotatedCopyNumberWithEvidence(copyNumber: CopyNumber): CopyNumber {
        val geneAlteration = GeneAlterationFactory.convertAlteration(copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber))

        val copyNumberWithGeneAlteration = copyNumber.copy(
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )

        val evidence = evidenceDatabase.evidenceForCopyNumber(copyNumberWithGeneAlteration)
        return copyNumberWithGeneAlteration.copy(evidence = evidence)
    }
}