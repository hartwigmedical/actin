package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ApplicableCancerType
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceDirection
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceLevel
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.isCategoryEvent
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ClinicalTrial
import com.hartwig.serve.datamodel.Treatment
import com.hartwig.serve.datamodel.Country as ServeCountry

object ClinicalEvidenceFactory {

    fun createNoEvidence(): ClinicalEvidence {
        return ClinicalEvidence()
    }

    fun create(actionabilityMatch: ActionabilityMatch): ClinicalEvidence {
        val onLabelEvidence = createTreatmentEvidence(true, actionabilityMatch.onLabelEvents)
        val offLabelEvidence = createTreatmentEvidence(false, actionabilityMatch.offLabelEvents)
        return ClinicalEvidence(
            externalEligibleTrials = createExternalTrialEvidence(actionabilityMatch.onLabelEvents),
            treatmentEvidence = onLabelEvidence + offLabelEvidence
        )
    }

    private fun createTreatmentEvidence(onLabel: Boolean, events: List<ActionableEvent>) =
        events.filter { it.source() == ActionabilityConstants.EVIDENCE_SOURCE }.map {
            TreatmentEvidence(
                it.treatmentName(),
                EvidenceLevel.valueOf(it.level().name),
                onLabel,
                EvidenceDirection(
                    hasPositiveResponse = it.direction().hasPositiveResponse(),
                    hasBenefit = it.direction().hasBenefit(),
                    isResistant = it.direction().isResistant,
                    isCertain = it.direction().isCertain
                ),
                it.isCategoryEvent(),
                it.sourceEvent(),
                ApplicableCancerType(it.applicableCancerType().name(), it.blacklistCancerTypes().map { ct -> ct.name() }.toSet()),
            )
        }.toSet()

    private fun createExternalTrialEvidence(onLabelEvents: List<ActionableEvent>): Set<ExternalTrial> {
        return onLabelEvents.filter { onLabelEvent ->
            onLabelEvent.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE && onLabelEvent.direction().hasPositiveResponse()
        }
            .map { onLabelEvent ->
                val trial = onLabelEvent.intervention() as ClinicalTrial
                ExternalTrial(
                    title = trial.acronym() ?: trial.title(),
                    countries = trial.countries().map(ClinicalEvidenceFactory::determineCountry).toSet(),
                    url = extractNctUrl(onLabelEvent),
                    nctId = trial.nctId(),
                    applicableCancerType = ApplicableCancerType(
                        onLabelEvent.applicableCancerType().name(),
                        onLabelEvent.blacklistCancerTypes().map { it.name() }.toSet()
                    ),
                    isCategoryVariant = onLabelEvent.isCategoryEvent(),
                    sourceEvent = onLabelEvent.sourceEvent()
                )
            }
            .toSet()
    }

    private fun ActionableEvent.treatmentName(): String = (this.intervention() as Treatment).name()

    private fun determineCountry(country: ServeCountry): Country {
        return when (country.countryName()) {
            "Netherlands" -> Country.NETHERLANDS
            "Belgium" -> Country.BELGIUM
            "Germany" -> Country.GERMANY
            "United States" -> Country.US
            else -> Country.OTHER
        }
    }

    private fun extractNctUrl(event: ActionableEvent): String {
        return event.evidenceUrls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + event.sourceUrls().joinToString(", "))
    }
}
