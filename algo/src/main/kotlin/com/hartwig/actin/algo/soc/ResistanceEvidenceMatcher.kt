package com.hartwig.actin.algo.soc

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.datamodel.ResistanceEvidence
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.Intervention

class ResistanceEvidenceMatcher(
    private val doidEntry: DoidEntry,
    private val applicableDoids: Set<String>,
    private val actionableEvents: ActionableEvents,
    private val treatmentDatabase: TreatmentDatabase
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
                    findTreatmentInDatabase(it.intervention(), treatment.name) &&
                    resistant(it)
        }.map { actionableEvent ->
            ResistanceEvidence(
                event = actionableEvent.sourceEvent(),
                isTested = null,
                isFound = null,
                resistanceLevel = actionableEvent.level().toString(),
                evidenceUrls = actionableEvent.evidenceUrls()
            )
        }.distinctBy { it.event }
    }

    private fun findTreatmentInDatabase(intervention: Intervention, treatmentToFind: String): Boolean {
        val therapyName = (intervention as com.hartwig.serve.datamodel.Treatment).name()
        val treatment = EfficacyEntryFactory(treatmentDatabase).generateOptions(listOf(therapyName))
            .mapNotNull(treatmentDatabase::findTreatmentByName)
            .distinct().singleOrNull()
        return treatment?.let { treatmentToFind.contains(treatment.name) } ?: false
    }

    companion object {
        fun create(
            doidEntry: DoidEntry,
            tumorDoids: Set<String>,
            actionableEvents: ActionableEvents,
            treatmentDatabase: TreatmentDatabase
        ): ResistanceEvidenceMatcher {
            return ResistanceEvidenceMatcher(doidEntry, tumorDoids, actionableEvents, treatmentDatabase)
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

        private fun resistant(resistanceEvent: ActionableEvent): Boolean {
            return resistanceEvent.level() in setOf(EvidenceLevel.A, EvidenceLevel.B, EvidenceLevel.C)
        }
    }
}