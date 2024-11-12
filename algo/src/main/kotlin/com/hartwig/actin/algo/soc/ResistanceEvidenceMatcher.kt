package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.actionability.BreakendEvidence
import com.hartwig.actin.molecular.evidence.actionability.CopyNumberEvidence
import com.hartwig.actin.molecular.evidence.actionability.FusionEvidence
import com.hartwig.actin.molecular.evidence.actionability.HomozygousDisruptionEvidence
import com.hartwig.actin.molecular.evidence.actionability.VariantEvidence
import com.hartwig.actin.molecular.evidence.orange.MolecularRecordAnnotatorFunctions
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.Intervention
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.range.ActionableRange

class ResistanceEvidenceMatcher(
    private val candidateActionableEvents: List<ActionableEvent>,
    private val treatmentDatabase: TreatmentDatabase,
    private val molecularHistory: MolecularHistory
) {

    fun match(treatment: Treatment): List<ResistanceEvidence> {
        return candidateActionableEvents.filter { findTreatmentInDatabase(it.intervention(), treatment) != null }
            .map { actionableEvent ->
                ResistanceEvidence(
                    event = actionableEvent.sourceEvent(),
                    isTested = null,
                    isFound = isFound(actionableEvent, molecularHistory),
                    resistanceLevel = actionableEvent.evidenceLevel().toString(),
                    evidenceUrls = actionableEvent.evidenceUrls(),
                    treatmentName = findTreatmentInDatabase(actionableEvent.intervention(), treatment)!!
                )
            }.distinctBy { it.event }
    }

    fun isFound(event: ActionableEvent, molecularHistory: MolecularHistory): Boolean? {
        val molecularTests = molecularHistory.molecularTests

        return when (event) {
            is ActionableHotspot -> {
                val variantEvidence = VariantEvidence(listOf(event), emptyList(), emptyList())
                molecularTests.any { molecularTest ->
                    molecularTest.drivers.variants.any {
                        variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty()
                    }
                }
            }

            is ActionableGene -> {
                val actionableEvents = ImmutableActionableEvents.builder().genes(listOf(event)).build()

                val variantEvidence = VariantEvidence.create(actionableEvents)
                val fusionEvidence = FusionEvidence.create(actionableEvents)
                val copyNumberEvidence = CopyNumberEvidence.create(actionableEvents)
                val homDisEvidence = HomozygousDisruptionEvidence.create(actionableEvents)
                val disruptionEvidence = BreakendEvidence.create(actionableEvents)

                molecularTests.any { molecularTest ->
                    with(molecularTest.drivers) {
                        val variantMatch =
                            variants.any { variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty() }
                        val fusionMatch = fusions.any {
                            fusionEvidence.findMatches(MolecularRecordAnnotatorFunctions.createFusionCriteria(it)).isNotEmpty()
                        }
                        variantMatch || fusionMatch || copyNumbers.any { copyNumberEvidence.findMatches(it).isNotEmpty() } ||
                                homozygousDisruptions.any { homDisEvidence.findMatches(it).isNotEmpty() } ||
                                disruptions.any { disruptionEvidence.findMatches(it).isNotEmpty() }
                    }
                }
            }

            is ActionableFusion -> {
                val fusionEvidence = FusionEvidence(emptyList(), listOf(event))
                molecularTests.any { molecularTest ->
                    molecularTest.drivers.fusions.any {
                        fusionEvidence.findMatches(MolecularRecordAnnotatorFunctions.createFusionCriteria(it)).isNotEmpty()
                    }
                }
            }

            is ActionableRange -> {
                val variantEvidence = VariantEvidence(emptyList(), listOf(event), emptyList())
                molecularTests.any { molecularTest ->
                    molecularTest.drivers.variants.any {
                        variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty()
                    }
                }
            }

            else -> null
        }
    }

    private fun findTreatmentInDatabase(intervention: Intervention, treatmentToFind: Treatment): String? {
        val therapyName = (intervention as com.hartwig.serve.datamodel.Treatment).name()
        return EfficacyEntryFactory(treatmentDatabase).generateOptions(listOf(therapyName))
            .mapNotNull(treatmentDatabase::findTreatmentByName)
            .distinct()
            .singleOrNull()
            ?.takeIf { drugsInOtherTreatment(treatmentToFind, it) }
            ?.name
    }

    private fun drugsInOtherTreatment(treatment1: Treatment, treatment2: Treatment): Boolean {
        val drugs1 = (treatment1 as DrugTreatment).drugs
        val drugs2 = (treatment2 as DrugTreatment).drugs
        return (drugs2.all { it in drugs1 })
    }

    companion object {
        fun create(
            doidModel: DoidModel,
            tumorDoids: Set<String>,
            actionableEvents: ActionableEvents,
            treatmentDatabase: TreatmentDatabase,
            molecularHistory: MolecularHistory
        ): ResistanceEvidenceMatcher {
            val expandedTumorDoids = expandDoids(doidModel, tumorDoids)
            val filteredActionableEvents = with(actionableEvents) {
                hotspots() + codons() + exons() + genes() + fusions() + hla() + characteristics()
            }
                .filter { hasNoPositiveResponse(it) && isOnLabel(it, expandedTumorDoids) }

            return ResistanceEvidenceMatcher(filteredActionableEvents, treatmentDatabase, molecularHistory)
        }

        private fun isOnLabel(event: ActionableEvent, expandedTumorDoids: Set<String>): Boolean {
            if (!expandedTumorDoids.contains(event.applicableCancerType().doid())) {
                return false
            }
            return event.blacklistCancerTypes().none { expandedTumorDoids.contains(it.doid()) }
        }

        private fun expandDoids(doidModel: DoidModel, doids: Set<String>): Set<String> {
            return doids.flatMap { doidModel.doidWithParents(it) }.toSet()
        }

        private fun hasNoPositiveResponse(resistanceEvent: ActionableEvent): Boolean {
            return resistanceEvent.evidenceLevel() in setOf(
                EvidenceLevel.A,
                EvidenceLevel.B,
                EvidenceLevel.C
            ) && !resistanceEvent.direction().hasPositiveResponse()
        }
    }
}