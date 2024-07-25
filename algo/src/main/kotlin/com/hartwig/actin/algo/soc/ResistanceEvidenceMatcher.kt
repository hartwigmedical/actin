package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.datamodel.ResistanceEvidence
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.EvidenceLevel

class ResistanceEvidenceMatcher(
    private val doidEntry: DoidEntry,
    private val applicableDoids: Set<String>,
    private val actionableEvents: ActionableEvents
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

    fun findMatches(actionableEvents: List<ActionableEvent>, treatment: Treatment): List<ResistanceEvidence> {
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val expandedTumorDoids = expandDoids(doidModel, applicableDoids)
        return actionableEvents.filter {
            it.direction().isResistant && isOnLabel(
                it,
                expandedTumorDoids
            ) && (it.intervention() as Treatment) == treatment && knownResistance(it)
        }.map { actionableEvent ->
            ResistanceEvidence(
                event = actionableEvent.sourceEvent(),
                isTested = null,
                isFound = null,
                resistanceLevel = actionableEvent.level().toString(),
                evidenceUrls = actionableEvent.evidenceUrls()
            )
        }
    }

    companion object {
        fun create(doidEntry: DoidEntry, tumorDoids: Set<String>, actionableEvents: ActionableEvents): ResistanceEvidenceMatcher {
            return ResistanceEvidenceMatcher(doidEntry, tumorDoids, actionableEvents)
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

        private fun knownResistance(resistanceEvent: ActionableEvent): Boolean {
            return when (resistanceEvent.level()) {
                EvidenceLevel.A, EvidenceLevel.B -> {
                    resistanceEvent.direction().isCertain
                }

                else -> {
                    false
                }
            }
        }
    }
}