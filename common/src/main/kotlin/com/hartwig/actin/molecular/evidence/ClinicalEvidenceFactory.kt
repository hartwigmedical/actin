package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.isCategoryVariant
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ClinicalTrial
import com.hartwig.serve.datamodel.EvidenceLevelDetails
import com.hartwig.serve.datamodel.Treatment

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
                it.date(),
                it.description(),
                it.isCategoryVariant(),
                it.sourceEvent(),
                it.evidenceLevelDetails(),
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
                    countries = trial.countries()
                        .map { Country(name = determineCountryName(it.countryName()), hospitalsPerCity = it.hospitalsPerCity()) }
                        .toSet(),
                    url = extractNctUrl(onLabelEvent),
                    nctId = trial.nctId(),
                    applicableCancerType = ApplicableCancerType(
                        onLabelEvent.applicableCancerType().name(),
                        onLabelEvent.blacklistCancerTypes().map { it.name() }.toSet()
                    ),
                    isCategoryVariant = onLabelEvent.isCategoryVariant(),
                    sourceEvent = onLabelEvent.sourceEvent(),
                    evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY
                )
            }
            .toSet()
    }

    private fun ActionableEvent.treatmentName(): String = (this.intervention() as Treatment).name()

    private fun determineCountryName(countryName: String): CountryName {
        return when (countryName) {
            "Netherlands" -> CountryName.NETHERLANDS
            "Belgium" -> CountryName.BELGIUM
            "Germany" -> CountryName.GERMANY
            "United States" -> CountryName.US
            else -> CountryName.OTHER
        }
    }

    private fun extractNctUrl(event: ActionableEvent): String {
        return event.evidenceUrls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + event.sourceUrls().joinToString(", "))
    }
}
