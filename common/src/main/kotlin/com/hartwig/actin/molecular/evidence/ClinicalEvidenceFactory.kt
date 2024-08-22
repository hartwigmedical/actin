package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ApplicableCancerType
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ClinicalTrial
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.Treatment

object ClinicalEvidenceFactory {

    fun createNoEvidence(): ClinicalEvidence {
        return ClinicalEvidence()
    }

    fun create(actionabilityMatch: ActionabilityMatch): ClinicalEvidence {
        val onLabelEvidence = createOnLabelEvidence(actionabilityMatch.onLabelEvents)
        val offLabelEvidence = createOffLabelEvidence(actionabilityMatch.offLabelEvents)
        val externalTrialEvidence = createExternalTrialEvidence(actionabilityMatch.onLabelEvents)
        return onLabelEvidence + offLabelEvidence + externalTrialEvidence
    }

    private fun createOnLabelEvidence(onLabelEvents: List<ActionableEvent>): ClinicalEvidence {
        return sourcedEvidence(onLabelEvents, ClinicalEvidenceFactory::responsiveOnLabelEvidence)
    }

    private fun createOffLabelEvidence(offLabelEvents: List<ActionableEvent>): ClinicalEvidence {
        return sourcedEvidence(offLabelEvents, ClinicalEvidenceFactory::responsiveOffLabelEvidence)
    }

    private fun sourcedEvidence(
        events: List<ActionableEvent>, responsiveEvidenceGenerator: (ActionableEvent) -> ClinicalEvidence
    ): ClinicalEvidence {
        return events.filter { it.source() == ActionabilityConstants.EVIDENCE_SOURCE }
            .fold(ClinicalEvidence()) { acc, event ->
                if (event.direction().isResponsive) {
                    acc + responsiveEvidenceGenerator.invoke(event)
                } else if (event.direction().isResistant) {
                    acc + resistantEvidence(event)
                } else {
                    acc
                }
            }
    }

    private fun createExternalTrialEvidence(onLabelEvents: List<ActionableEvent>): ClinicalEvidence {
        return ClinicalEvidence(
            externalEligibleTrials = onLabelEvents.filter { onLabelEvent ->
                onLabelEvent.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE && onLabelEvent.direction().isResponsive
            }
                .map { onLabelEvent ->
                    val trial = onLabelEvent.intervention() as ClinicalTrial
                    ExternalTrial(
                        title = trial.studyAcronym() ?: trial.studyTitle(),
                        countries = trial.countriesOfStudy().map(ClinicalEvidenceFactory::determineCountry).toSet(),
                        url = extractNctUrl(onLabelEvent),
                        nctId = trial.studyNctId(),
                        applicableCancerType = ApplicableCancerType(
                            onLabelEvent.applicableCancerType().name(),
                            onLabelEvent.blacklistCancerTypes().map { it.name() }.toSet()
                        ),
                        sourceEvent = onLabelEvent.sourceEvent()
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
        return event.evidenceUrls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + event.sourceUrls().joinToString(", "))
    }

    private fun ActionableEvent.treatmentName(): String = (this.intervention() as Treatment).name()

    private fun responsiveOnLabelEvidence(onLabelResponsiveEvent: ActionableEvent): ClinicalEvidence {
        return when (onLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    actionableEvidence(onLabelResponsiveEvent, ActinEvidenceCategory.APPROVED)
                } else {
                    actionableEvidence(onLabelResponsiveEvent, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
                }
            }

            EvidenceLevel.B -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    actionableEvidence(onLabelResponsiveEvent, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
                } else {
                    actionableEvidence(onLabelResponsiveEvent, ActinEvidenceCategory.PRE_CLINICAL)
                }
            }

            else -> {
                actionableEvidence(onLabelResponsiveEvent, ActinEvidenceCategory.PRE_CLINICAL)
            }
        }
    }

    private fun responsiveOffLabelEvidence(offLabelResponsiveEvent: ActionableEvent): ClinicalEvidence {
        return when (offLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                actionableEvidence(offLabelResponsiveEvent, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
            }

            EvidenceLevel.B -> {
                if (offLabelResponsiveEvent.direction().isCertain) {
                    actionableEvidence(offLabelResponsiveEvent, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)
                } else {
                    actionableEvidence(offLabelResponsiveEvent, ActinEvidenceCategory.PRE_CLINICAL)
                }
            }

            else -> {
                actionableEvidence(offLabelResponsiveEvent, ActinEvidenceCategory.PRE_CLINICAL)
            }
        }
    }

    private fun resistantEvidence(resistanceEvent: ActionableEvent): ClinicalEvidence {
        return when (resistanceEvent.level()) {
            EvidenceLevel.A, EvidenceLevel.B -> {
                if (resistanceEvent.direction().isCertain) {
                    actionableEvidence(resistanceEvent, ActinEvidenceCategory.KNOWN_RESISTANT)
                } else {
                    actionableEvidence(resistanceEvent, ActinEvidenceCategory.SUSPECT_RESISTANT)
                }
            }

            else -> {
                actionableEvidence(resistanceEvent, ActinEvidenceCategory.SUSPECT_RESISTANT)
            }
        }
    }

    private fun actionableEvidence(
        event: ActionableEvent,
        actinEvidenceCategory: ActinEvidenceCategory
    ) = ClinicalEvidence(
        treatmentEvidence = setOf(
            TreatmentEvidence(
                treatment = event.treatmentName(),
                evidenceLevel = event.level(),
                sourceEvent = event.sourceEvent(),
                applicableCancerType = ApplicableCancerType(
                    cancerType = event.applicableCancerType().name(),
                    excludedCancerTypes = event.blacklistCancerTypes().map { it.name() }.toSet()
                ),
                category = actinEvidenceCategory
            )
        )
    )
}
