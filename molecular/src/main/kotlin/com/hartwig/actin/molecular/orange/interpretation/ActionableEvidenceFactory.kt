package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableExternalTrial
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.EvidenceLevel

object ActionableEvidenceFactory {

    fun createNoEvidence(): ActionableEvidence {
        return ImmutableActionableEvidence.builder().build()
    }

    fun create(actionabilityMatch: ActionabilityMatch?): ActionableEvidence? {
        if (actionabilityMatch == null) {
            return null
        }

        val onLabelEvidence = createOnLabelEvidence(actionabilityMatch.onLabelEvents)
        val offLabelEvidence = createOffLabelEvidence(actionabilityMatch.offLabelEvents)
        val externalTrialEvidence = createExternalTrialEvidence(actionabilityMatch.onLabelEvents)
        val merged: ActionableEvidence =
            ImmutableActionableEvidence.builder().from(onLabelEvidence).from(offLabelEvidence).from(externalTrialEvidence).build()
        val simplified = filterRedundantLowerEvidence(merged)
        return filterResistanceEvidence(simplified)
    }

    private fun createOnLabelEvidence(onLabelEvents: List<ActionableEvent>): ActionableEvidence {
        val builder = ImmutableActionableEvidence.builder()
        for (onLabelEvent in onLabelEvents) {
            if (onLabelEvent.source() == ActionabilityConstants.EVIDENCE_SOURCE) {
                if (onLabelEvent.direction().isResponsive) {
                    populateResponsiveOnLabelEvidence(builder, onLabelEvent)
                } else if (onLabelEvent.direction().isResistant) {
                    populateResistantEvidence(builder, onLabelEvent)
                }
            }
        }
        return builder.build()
    }

    private fun createOffLabelEvidence(offLabelEvents: List<ActionableEvent>): ActionableEvidence {
        val builder = ImmutableActionableEvidence.builder()
        for (offLabelEvent in offLabelEvents) {
            if (offLabelEvent.source() == ActionabilityConstants.EVIDENCE_SOURCE) {
                if (offLabelEvent.direction().isResponsive) {
                    populateResponsiveOffLabelEvidence(builder, offLabelEvent)
                } else if (offLabelEvent.direction().isResistant) {
                    populateResistantEvidence(builder, offLabelEvent)
                }
            }
        }
        return builder.build()
    }

    private fun createExternalTrialEvidence(onLabelEvents: List<ActionableEvent>): ActionableEvidence {
        val builder = ImmutableActionableEvidence.builder()
        for (onLabelEvent in onLabelEvents) {
            if (onLabelEvent.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE && onLabelEvent.direction().isResponsive) {
                val nctUrl = extractNctUrl(onLabelEvent)

                builder.addExternalEligibleTrials(
                    ImmutableExternalTrial.builder()
                        .title(onLabelEvent.treatment().name())
                        // evidenceUrls() contains a set of countries
                        .countries(determineCountries(onLabelEvent.evidenceUrls()))
                        .url(nctUrl)
                        .nctId(nctUrl.takeLast(11))
                        .build()
                )
            }
        }
        return builder.build()
    }

    private fun determineCountries(countries: Set<String>): Set<Country> {
        val values = mutableSetOf<Country>()
        for (country in countries) {
            values.add(determineCountry(country))
        }

        return values
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
        for (url in event.sourceUrls()) {
            if (isNctUrl(url)) {
                return url
            }
        }

        throw IllegalStateException("Found no URL ending with a NCT id: " + event.sourceUrls().joinToString { ", " })
    }

    private fun isNctUrl(url: String): Boolean {
        if ((url.length > 11)) {
            val nctId = url.takeLast(11)
            if (nctId.substring(0, 3) == "NCT") {
                return true
            }
        }
        return false
    }

    private fun populateResponsiveOnLabelEvidence(
        builder: ImmutableActionableEvidence.Builder,
        onLabelResponsiveEvent: ActionableEvent
    ) {
        val treatment = onLabelResponsiveEvent.treatment().name()
        when (onLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    builder.addApprovedTreatments(treatment)
                } else {
                    builder.addOnLabelExperimentalTreatments(treatment)
                }
            }

            EvidenceLevel.B -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    builder.addOnLabelExperimentalTreatments(treatment)
                } else {
                    builder.addPreClinicalTreatments(treatment)
                }
            }

            else -> {
                builder.addPreClinicalTreatments(treatment)
            }
        }
    }

    private fun populateResponsiveOffLabelEvidence(
        builder: ImmutableActionableEvidence.Builder,
        offLabelResponsiveEvent: ActionableEvent
    ) {
        val treatment = offLabelResponsiveEvent.treatment().name()
        when (offLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                builder.addOnLabelExperimentalTreatments(treatment)
            }

            EvidenceLevel.B -> {
                if (offLabelResponsiveEvent.direction().isCertain) {
                    builder.addOffLabelExperimentalTreatments(treatment)
                } else {
                    builder.addPreClinicalTreatments(treatment)
                }
            }

            else -> {
                builder.addPreClinicalTreatments(treatment)
            }
        }
    }

    private fun populateResistantEvidence(
        builder: ImmutableActionableEvidence.Builder,
        resistanceEvent: ActionableEvent
    ) {
        val treatment = resistanceEvent.treatment().name()
        when (resistanceEvent.level()) {
            EvidenceLevel.A, EvidenceLevel.B -> {
                if (resistanceEvent.direction().isCertain) {
                    builder.addKnownResistantTreatments(treatment)
                } else {
                    builder.addSuspectResistantTreatments(treatment)
                }
            }

            else -> {
                builder.addSuspectResistantTreatments(treatment)
            }
        }
    }

    internal fun filterRedundantLowerEvidence(evidence: ActionableEvidence): ActionableEvidence {
        val treatmentsToExcludeForOnLabel = evidence.approvedTreatments()
        val cleanedOnLabelTreatments = cleanTreatments(evidence.onLabelExperimentalTreatments(), treatmentsToExcludeForOnLabel)
        val treatmentsToExcludeForOffLabel = evidence.approvedTreatments() + evidence.onLabelExperimentalTreatments()
        val cleanedOffLabelTreatments = cleanTreatments(evidence.offLabelExperimentalTreatments(), treatmentsToExcludeForOffLabel)
        val treatmentsToExcludeForPreClinical =
            evidence.approvedTreatments() + evidence.onLabelExperimentalTreatments() + evidence.offLabelExperimentalTreatments()
        val cleanedPreClinicalTreatments = cleanTreatments(evidence.preClinicalTreatments(), treatmentsToExcludeForPreClinical)
        val treatmentsToExcludeForSuspectResistant = evidence.knownResistantTreatments()
        val cleanedSuspectResistantTreatments =
            cleanTreatments(evidence.suspectResistantTreatments(), treatmentsToExcludeForSuspectResistant)
        return ImmutableActionableEvidence.builder()
            .from(evidence)
            .onLabelExperimentalTreatments(cleanedOnLabelTreatments)
            .offLabelExperimentalTreatments(cleanedOffLabelTreatments)
            .preClinicalTreatments(cleanedPreClinicalTreatments)
            .suspectResistantTreatments(cleanedSuspectResistantTreatments)
            .build()
    }

    private fun filterResistanceEvidence(evidence: ActionableEvidence): ActionableEvidence {
        val treatmentsToIncludeForResistance =
            evidence.approvedTreatments() + evidence.onLabelExperimentalTreatments() + evidence.offLabelExperimentalTreatments()
        val applicableKnownResistantTreatments = filterTreatments(evidence.knownResistantTreatments(), treatmentsToIncludeForResistance)
        val applicableSuspectResistantTreatments = filterTreatments(evidence.suspectResistantTreatments(), treatmentsToIncludeForResistance)
        return ImmutableActionableEvidence.builder()
            .from(evidence)
            .knownResistantTreatments(applicableKnownResistantTreatments)
            .suspectResistantTreatments(applicableSuspectResistantTreatments)
            .build()
    }

    private fun filterTreatments(treatments: Set<String>, treatmentsToInclude: Set<String>): Set<String> {
        return treatments.filter { it in treatmentsToInclude }.toSet()
    }

    private fun cleanTreatments(treatments: Set<String>, treatmentsToExclude: Set<String>): Set<String> {
        return treatments.filterNot { it in treatmentsToExclude }.toSet()
    }
}
