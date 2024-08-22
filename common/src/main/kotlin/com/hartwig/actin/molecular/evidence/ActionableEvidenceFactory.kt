package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.ActionableTreatment
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ClinicalTrial
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.Treatment
import com.hartwig.serve.datamodel.Country as ServeCountry

object ActionableEvidenceFactory {

    fun createNoEvidence(): ActionableEvidence {
        return ActionableEvidence()
    }

    fun create(actionabilityMatch: ActionabilityMatch): ActionableEvidence {
        val onLabelEvidence = createOnLabelEvidence(actionabilityMatch.onLabelEvents)
        val offLabelEvidence = createOffLabelEvidence(actionabilityMatch.offLabelEvents)
        val externalTrialEvidence = createExternalTrialEvidence(actionabilityMatch.onLabelEvents)
        return onLabelEvidence + offLabelEvidence + externalTrialEvidence
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
                        title = trial.acronym() ?: trial.title(),
                        countries = trial.countries().map(ActionableEvidenceFactory::determineCountry).toSet(),
                        url = extractNctUrl(onLabelEvent),
                        nctId = trial.nctId(),
                    )
                }
                .toSet()
        )
    }

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

    private fun ActionableEvent.treatmentName(): String = (this.intervention() as Treatment).name()

    private fun responsiveOnLabelEvidence(onLabelResponsiveEvent: ActionableEvent): ActionableEvidence {
        val treatment = onLabelResponsiveEvent.treatmentName()
        return when (onLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    actionableEvidence(treatment, onLabelResponsiveEvent.level(), ActinEvidenceCategory.APPROVED)
                } else {
                    actionableEvidence(treatment, onLabelResponsiveEvent.level(), ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
                }
            }

            EvidenceLevel.B -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    actionableEvidence(treatment, onLabelResponsiveEvent.level(), ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
                } else {
                    actionableEvidence(treatment, onLabelResponsiveEvent.level(), ActinEvidenceCategory.PRE_CLINICAL)
                }
            }

            else -> {
                actionableEvidence(treatment, onLabelResponsiveEvent.level(), ActinEvidenceCategory.PRE_CLINICAL)
            }
        }
    }

    private fun responsiveOffLabelEvidence(offLabelResponsiveEvent: ActionableEvent): ActionableEvidence {
        val treatment = offLabelResponsiveEvent.treatmentName()
        return when (offLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                actionableEvidence(treatment, offLabelResponsiveEvent.level(), ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
            }

            EvidenceLevel.B -> {
                if (offLabelResponsiveEvent.direction().isCertain) {
                    actionableEvidence(treatment, offLabelResponsiveEvent.level(), ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)
                } else {
                    actionableEvidence(treatment, offLabelResponsiveEvent.level(), ActinEvidenceCategory.PRE_CLINICAL)
                }
            }

            else -> {
                actionableEvidence(treatment, offLabelResponsiveEvent.level(), ActinEvidenceCategory.PRE_CLINICAL)
            }
        }
    }

    private fun resistantEvidence(resistanceEvent: ActionableEvent): ActionableEvidence {
        val treatment = resistanceEvent.treatmentName()
        return when (resistanceEvent.level()) {
            EvidenceLevel.A, EvidenceLevel.B -> {
                if (resistanceEvent.direction().isCertain) {
                    actionableEvidence(treatment, resistanceEvent.level(), ActinEvidenceCategory.KNOWN_RESISTANT)
                } else {
                    actionableEvidence(treatment, resistanceEvent.level(), ActinEvidenceCategory.SUSPECT_RESISTANT)
                }
            }

            else -> {
                actionableEvidence(treatment, resistanceEvent.level(), ActinEvidenceCategory.SUSPECT_RESISTANT)
            }
        }
    }

    private fun actionableEvidence(
        treatment: String,
        evidenceLevel: EvidenceLevel,
        actinEvidenceCategory: ActinEvidenceCategory
    ) = ActionableEvidence(
        actionableTreatments = setOf(
            ActionableTreatment(
                treatment,
                evidenceLevel,
                actinEvidenceCategory
            )
        )
    )
}
