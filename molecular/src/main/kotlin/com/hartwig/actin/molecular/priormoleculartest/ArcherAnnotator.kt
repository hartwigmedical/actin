package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariantExtraction
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.ActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.pave.VariantTranscriptImpact
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.serve.datamodel.hotspot.KnownHotspot


class ArcherAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    private val transcriptAnnotator: VariantAnnotator,
    private val paveLite: PaveLite
) :
    MolecularAnnotator<ArcherPanelExtraction, PanelRecord> {

    override fun annotate(input: ArcherPanelExtraction): PanelRecord {
        val annotatedVariants = input.variants.map {

            val transcriptAnnotation = transcriptAnnotator.resolve(it.gene, null, it.hgvsCodingImpact)
                ?: throw RuntimeException("Unable to resolve variant in variant annotator. See prior warnings.")
            val paveAnnotation = paveLite.run(it.gene, transcriptAnnotation.transcript(), transcriptAnnotation.position())

            val criteria = VariantMatchCriteria(
                isReportable = true,
                gene = it.gene,
                chromosome = transcriptAnnotation.chromosome(),
                ref = transcriptAnnotation.ref(),
                alt = transcriptAnnotation.alt(),
                position = transcriptAnnotation.position()
            )
            val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(criteria))
            val geneAlteration = GeneAlterationFactory.convertAlteration(
                it.gene, evidenceDatabase.geneAlterationForVariant(criteria)
            )

            createVariantWithEvidence(it, evidence, geneAlteration, transcriptAnnotation, paveAnnotation)
        }

        val variantsByGene = annotatedVariants.groupBy { it.gene }
        val variantsWithDriverLikelihoodModel = variantsByGene.map {
            val geneRole = it.value.map { variant -> variant.geneRole }.first()
            val likelihood = geneDriverLikelihoodModel.evaluate(it.key, geneRole, it.value)
            likelihood to it.value
        }.flatMap {
            it.second.map { variant ->
                variant.copy(
                    driverLikelihood = DriverLikelihood.from(it.first)
                )
            }
        }

        return PanelRecord(
            archerPanelExtraction = input,
            type = ExperimentType.ARCHER,
            date = input.date,
            drivers = Drivers(variants = variantsWithDriverLikelihoodModel.toSet()),
            characteristics = MolecularCharacteristics(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }

    private fun createVariantWithEvidence(
        it: ArcherVariantExtraction,
        evidence: ActionableEvidence?,
        geneAlteration: GeneAlteration,
        transcriptAnnotation: com.hartwig.actin.tools.variant.Variant,
        paveAnnotation: VariantTranscriptImpact?
    ) = Variant(
        isReportable = true,
        event = "${it.gene} ${it.hgvsCodingImpact}",
        driverLikelihood = DriverLikelihood.LOW,
        evidence = evidence ?: ActionableEvidence(),
        gene = it.gene,
        geneRole = geneAlteration.geneRole,
        proteinEffect = geneAlteration.proteinEffect,
        isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
        isHotspot = geneAlteration is KnownHotspot,
        ref = transcriptAnnotation.ref(),
        alt = transcriptAnnotation.alt(),
        canonicalImpact = TranscriptImpact(
            transcriptId = transcriptAnnotation.transcript(),
            hgvsCodingImpact = it.hgvsCodingImpact,
            hgvsProteinImpact = "",
            isSpliceRegion = false,
            affectedExon = paveAnnotation?.affectedExon(),
            affectedCodon = paveAnnotation?.affectedCodon()
        ),
        chromosome = transcriptAnnotation.chromosome(),
        position = transcriptAnnotation.position(),
        type = VariantType.SNV
    )
}