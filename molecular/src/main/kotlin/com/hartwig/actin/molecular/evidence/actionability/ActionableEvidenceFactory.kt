package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ClinicalTrial
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.Treatment

object ActionableEvidenceFactory {

    fun createNoEvidence(): ActionableEvidence {
        return ActionableEvidence()
    }

    fun create(actionabilityMatch: ActionabilityMatch): ActionableEvidence {
        val onLabelEvidence = createOnLabelEvidence(actionabilityMatch.onLabelEvents)
        val offLabelEvidence = createOffLabelEvidence(actionabilityMatch.offLabelEvents)
        val externalTrialEvidence = createExternalTrialEvidence(actionabilityMatch.onLabelEvents)
        val merged = onLabelEvidence + offLabelEvidence + externalTrialEvidence
        return filterResistanceEvidence(filterRedundantLowerEvidence(merged))
    }

    private fun createOnLabelEvidence(onLabelEvents: List<ActionableEvent>): ActionableEvidence {
        return sourcedEvidence(onLabelEvents, ActionableEvidenceFactory::responsiveOnLabelEvidence)
    }

    private fun createOffLabelEvidence(offLabelEvents: List<ActionableEvent>): ActionableEvidence {
        return sourcedEvidence(offLabelEvents, ActionableEvidenceFactory::responsiveOffLabelEvidence)
    }

    private fun sourcedEvidence(
        events: List<ActionableEvent>, responsiveEvidenceGenerator: (ActionableEvent) -> ActionableEvidence
    ): ActionableEvidence {
        return events.filter { it.source() == ActionabilityConstants.EVIDENCE_SOURCE }
            .fold(ActionableEvidence()) { acc, event ->
                if (event.direction().isResponsive) {
                    acc + responsiveEvidenceGenerator.invoke(event)
                } else if (event.direction().isResistant) {
                    acc + resistantEvidence(event)
                } else {
                    acc
                }
            }
    }

    private fun createExternalTrialEvidence(onLabelEvents: List<ActionableEvent>): ActionableEvidence {
        return ActionableEvidence(
            externalEligibleTrials = onLabelEvents.filter { onLabelEvent ->
                onLabelEvent.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE && onLabelEvent.direction().isResponsive
            }
                .map { onLabelEvent ->
                    val trial = onLabelEvent.intervention() as ClinicalTrial
                    ExternalTrial(
                        title = trial.studyAcronym() ?: trial.studyTitle(),
                        countries = trial.countriesOfStudy().map(::determineCountry).toSet(),
                        url = extractNctUrl(onLabelEvent),
                        nctId = trial.studyNctId(),
                    )
                }
                .toSet()
        )
    }

    private fun determineCountry(country: String): Country {
        return when (country) {
            "Netherlands" -> Country.NETHERLANDS
            "Belgium" -> Country.BELGIUM
            "Germany" -> Country.GERMANY
            else -> Country.OTHER
        }
    }

    private fun extractNctUrl(event: ActionableEvent): String {
        return event.sourceUrls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + event.sourceUrls().joinToString { ", " })
    }

    private fun ActionableEvent.treatmentName(): String = (this.intervention() as Treatment).name()

    private fun responsiveOnLabelEvidence(onLabelResponsiveEvent: ActionableEvent): ActionableEvidence {
        val treatment = onLabelResponsiveEvent.treatmentName()
        return when (onLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    ActionableEvidence(approvedTreatments = setOf(treatment))
                } else {
                    ActionableEvidence(onLabelExperimentalTreatments = setOf(treatment))
                }
            }

            EvidenceLevel.B -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    ActionableEvidence(onLabelExperimentalTreatments = setOf(treatment))
                } else {
                    ActionableEvidence(preClinicalTreatments = setOf(treatment))
                }
            }

            else -> {
                ActionableEvidence(preClinicalTreatments = setOf(treatment))
            }
        }
    }

    private fun responsiveOffLabelEvidence(offLabelResponsiveEvent: ActionableEvent): ActionableEvidence {
        val treatment = offLabelResponsiveEvent.treatmentName()
        return when (offLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                ActionableEvidence(onLabelExperimentalTreatments = setOf(treatment))
            }

            EvidenceLevel.B -> {
                if (offLabelResponsiveEvent.direction().isCertain) {
                    ActionableEvidence(offLabelExperimentalTreatments = setOf(treatment))
                } else {
                    ActionableEvidence(preClinicalTreatments = setOf(treatment))
                }
            }

            else -> {
                ActionableEvidence(preClinicalTreatments = setOf(treatment))
            }
        }
    }

    private fun resistantEvidence(resistanceEvent: ActionableEvent): ActionableEvidence {
        val treatment = resistanceEvent.treatmentName()
        return when (resistanceEvent.level()) {
            EvidenceLevel.A, EvidenceLevel.B -> {
                if (resistanceEvent.direction().isCertain) {
                    ActionableEvidence(knownResistantTreatments = setOf(treatment))
                } else {
                    ActionableEvidence(suspectResistantTreatments = setOf(treatment))
                }
            }

            else -> {
                ActionableEvidence(suspectResistantTreatments = setOf(treatment))
            }
        }
    }

    fun filterRedundantLowerEvidence(evidence: ActionableEvidence): ActionableEvidence {
        val treatmentsToExcludeForOffLabel = evidence.approvedTreatments + evidence.onLabelExperimentalTreatments
        val treatmentsToExcludeForPreClinical =
            evidence.approvedTreatments + evidence.onLabelExperimentalTreatments + evidence.offLabelExperimentalTreatments

        return evidence.copy(
            onLabelExperimentalTreatments = cleanTreatments(evidence.onLabelExperimentalTreatments, evidence.approvedTreatments),
            offLabelExperimentalTreatments = cleanTreatments(evidence.offLabelExperimentalTreatments, treatmentsToExcludeForOffLabel),
            preClinicalTreatments = cleanTreatments(evidence.preClinicalTreatments, treatmentsToExcludeForPreClinical),
            suspectResistantTreatments = cleanTreatments(evidence.suspectResistantTreatments, evidence.knownResistantTreatments)
        )
    }

    private fun filterResistanceEvidence(evidence: ActionableEvidence): ActionableEvidence {
        val treatmentsToIncludeForResistance =
            evidence.approvedTreatments + evidence.onLabelExperimentalTreatments + evidence.offLabelExperimentalTreatments
        val applicableKnownResistantTreatments = filterTreatments(evidence.knownResistantTreatments, treatmentsToIncludeForResistance)
        val applicableSuspectResistantTreatments = filterTreatments(evidence.suspectResistantTreatments, treatmentsToIncludeForResistance)
        return evidence.copy(
            knownResistantTreatments = applicableKnownResistantTreatments,
            suspectResistantTreatments = applicableSuspectResistantTreatments
        )
    }

    private fun filterTreatments(treatments: Set<String>, treatmentsToInclude: Set<String>): Set<String> {
        return treatments.intersect(treatmentsToInclude)
    }

    private fun cleanTreatments(treatments: Set<String>, treatmentsToExclude: Set<String>): Set<String> {
        return treatments - treatmentsToExclude
    }
}
