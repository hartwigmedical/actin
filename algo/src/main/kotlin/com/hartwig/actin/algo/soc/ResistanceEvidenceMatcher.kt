package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.datamodel.ResistanceEvidence
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.molecular.evidence.actionability.BreakendEvidence
import com.hartwig.actin.molecular.evidence.actionability.CopyNumberEvidence
import com.hartwig.actin.molecular.evidence.actionability.FusionEvidence
import com.hartwig.actin.molecular.evidence.actionability.HomozygousDisruptionEvidence
import com.hartwig.actin.molecular.evidence.actionability.VariantEvidence
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.evidence.orange.MolecularRecordAnnotatorFunctions
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.Intervention
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.immuno.ActionableHLA
import com.hartwig.serve.datamodel.range.ActionableRange

class ResistanceEvidenceMatcher(
    private val doidEntry: DoidEntry,
    private val applicableDoids: Set<String>,
    private val actionableEvents: ActionableEvents,
    private val treatmentDatabase: TreatmentDatabase,
    private val drivers: Drivers
) {

    fun match(treatment: Treatment): List<ResistanceEvidence> {
        return findMatches(actionableEvents.hotspots(), treatment) + findMatches(actionableEvents.codons(), treatment) + findMatches(
            actionableEvents.exons(),
            treatment
        ) + findMatches(actionableEvents.genes(), treatment) + findMatches(actionableEvents.fusions(), treatment) + findMatches(
            actionableEvents.hla(),
            treatment
        ) + findMatches(actionableEvents.characteristics(), treatment)
    }

    private fun findMatches(actionableEvents: List<ActionableEvent>, treatment: Treatment): List<ResistanceEvidence> {
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val expandedTumorDoids = expandDoids(doidModel, applicableDoids)
        return actionableEvents.filter {
            it.direction().isResistant &&
                    isOnLabel(it, expandedTumorDoids) &&
                    findTreatmentInDatabase(it.intervention(), treatment).isNotEmpty() &&
                    isResistant(it)
        }.map { actionableEvent ->
            ResistanceEvidence(
                event = actionableEvent.sourceEvent(),
                isTested = null,
                isFound = isFound(actionableEvent, drivers),
                resistanceLevel = actionableEvent.level().toString(),
                evidenceUrls = actionableEvent.evidenceUrls(),
                treatmentName = findTreatmentInDatabase(actionableEvent.intervention(), treatment)
            )
        }.distinctBy { it.event }
    }

    private fun findTreatmentInDatabase(intervention: Intervention, treatmentToFind: Treatment): String {
        val therapyName = (intervention as com.hartwig.serve.datamodel.Treatment).name()
        val treatment = EfficacyEntryFactory(treatmentDatabase).generateOptions(listOf(therapyName))
            .mapNotNull(treatmentDatabase::findTreatmentByName)
            .distinct().singleOrNull()
        return if (treatment?.let { drugsInOtherTreatment(treatmentToFind, treatment) } == true) treatment.name
        else ""
    }

    private fun isFound(event: ActionableEvent, drivers: Drivers): Boolean? {
        when (event) {
            is ActionableHotspot -> {
                val variantEvidence = VariantEvidence(listOf(event), emptyList(), emptyList())
                return drivers.variants.any {
                    variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty()
                }
            }

            is ActionableGene -> {
                val actionableEvents = ImmutableActionableEvents.builder().hotspots(emptyList()).codons(emptyList()).exons(emptyList())
                    .genes(listOf(event)).fusions(emptyList()).characteristics(emptyList()).hla(emptyList()).build()

                val variantEvidence = VariantEvidence.create(actionableEvents)
                val fusionEvidence = FusionEvidence.create(actionableEvents)
                val copyNumberEvidence = CopyNumberEvidence.create(actionableEvents)
                val homDisEvidence = HomozygousDisruptionEvidence.create(actionableEvents)
                val disruptionEvidence = BreakendEvidence.create(actionableEvents)

                val variantMatch =
                    drivers.variants.any { variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty() }
                val fusionMatch = drivers.fusions.any {
                    fusionEvidence.findMatches(MolecularRecordAnnotatorFunctions.createFusionCriteria(it)).isNotEmpty()
                }
                val copyNumberMatch = drivers.copyNumbers.any { copyNumberEvidence.findMatches(it).isNotEmpty() }
                val homDisMatch = drivers.homozygousDisruptions.any { homDisEvidence.findMatches(it).isNotEmpty() }
                val disruptionMatch = drivers.disruptions.any { disruptionEvidence.findMatches(it).isNotEmpty() }

                return variantMatch || fusionMatch || copyNumberMatch || homDisMatch || disruptionMatch

            }

            is ActionableFusion -> {
                val fusionEvidence = FusionEvidence(emptyList(), listOf(event))
                return drivers.fusions.any {
                    fusionEvidence.findMatches(MolecularRecordAnnotatorFunctions.createFusionCriteria(it)).isNotEmpty()
                }
            }

            is ActionableRange -> {
                val variantEvidence = VariantEvidence(emptyList(), listOf(event), emptyList())
                return drivers.variants.any {
                    variantEvidence.findMatches(MolecularRecordAnnotatorFunctions.createCriteria(it)).isNotEmpty()
                }
            }

            is ActionableCharacteristic -> {
                return null
            }

            is ActionableHLA -> {
                return null
            }

            else -> {
                return null
            }
        }
    }

    companion object {
        fun create(
            doidEntry: DoidEntry,
            tumorDoids: Set<String>,
            actionableEvents: ActionableEvents,
            treatmentDatabase: TreatmentDatabase,
            drivers: Drivers
        ): ResistanceEvidenceMatcher {
            return ResistanceEvidenceMatcher(doidEntry, tumorDoids, actionableEvents, treatmentDatabase, drivers)
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

        private fun isResistant(resistanceEvent: ActionableEvent): Boolean {
            return resistanceEvent.level() in setOf(
                EvidenceLevel.A,
                EvidenceLevel.B,
                EvidenceLevel.C
            ) && resistanceEvent.direction().isResistant
        }

        private fun drugsInOtherTreatment(treatment1: Treatment, treatment2: Treatment): Boolean {
            val drugs1 = (treatment1 as DrugTreatment).drugs
            val drugs2 = (treatment2 as DrugTreatment).drugs
            return (drugs2.all { it in drugs1 })
        }
    }
}