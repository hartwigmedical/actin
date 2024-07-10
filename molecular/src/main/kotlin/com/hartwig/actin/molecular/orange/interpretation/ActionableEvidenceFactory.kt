package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.EvidenceLevel

object ActionableEvidenceFactory {

    fun createNoEvidence(): ActionableEvidence {
        return ActionableEvidence()
    }

    fun create(actionabilityMatch: ActionabilityMatch?): ActionableEvidence? {
        // TODO try removing the optionals in the param/return and fixup breakage
        if (actionabilityMatch == null) {
            return null
        }

        val onLabelEvidence = createOnLabelEvidence(actionabilityMatch.onLabelEvents)
        val offLabelEvidence = createOffLabelEvidence(actionabilityMatch.offLabelEvents)
        val externalTrialEvidence = createExternalTrialEvidence(actionabilityMatch.onLabelEvents)
        val merged = onLabelEvidence + offLabelEvidence + externalTrialEvidence
        return filterResistanceEvidence(filterRedundantLowerEvidence(merged))
    }

    private fun createOnLabelEvidence(onLabelEvents: List<ActionableEvent>): ActionableEvidence {
        return sourcedEvidence(onLabelEvents, ::responsiveOnLabelEvidence)
    }

    private fun createOffLabelEvidence(offLabelEvents: List<ActionableEvent>): ActionableEvidence {
        return sourcedEvidence(offLabelEvents, ::responsiveOffLabelEvidence)
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
                    val nctUrl = extractNctUrl(onLabelEvent)
                    ExternalTrial(
                        title = onLabelEvent.treatment().name(),
                        // evidenceUrls() contains a set of countries
                        countries = onLabelEvent.evidenceUrls().map(::determineCountry).toSet(),
                        url = nctUrl,
                        nctId = nctUrl.takeLast(11),
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
            "United States" -> Country.US
            else -> Country.OTHER
        }
    }

    private fun extractNctUrl(event: ActionableEvent): String {
        return event.sourceUrls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + event.sourceUrls().joinToString { ", " })
    }

    private fun responsiveOnLabelEvidence(onLabelResponsiveEvent: ActionableEvent): ActionableEvidence {
        val treatment = onLabelResponsiveEvent.treatment().name()
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
        val treatment = offLabelResponsiveEvent.treatment().name()
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
        val treatment = resistanceEvent.treatment().name()
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
