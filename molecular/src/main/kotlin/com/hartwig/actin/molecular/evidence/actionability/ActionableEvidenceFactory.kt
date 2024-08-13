package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.ActionableTreatment
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceTier
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
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(treatment, EvidenceLevel.A, EvidenceTier.I, ActinEvidenceCategory.APPROVED)
                        )
                    )
                } else {
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(treatment, EvidenceLevel.B, EvidenceTier.I, ActinEvidenceCategory.ON_LABEL)
                        )
                    )
                }
            }

            EvidenceLevel.B -> {
                if (onLabelResponsiveEvent.direction().isCertain) {
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(
                                treatment,
                                EvidenceLevel.B,
                                EvidenceTier.I,
                                ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL
                            )
                        )
                    )
                } else {
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(treatment, EvidenceLevel.B, EvidenceTier.I, ActinEvidenceCategory.PRE_CLINICAL)
                        )
                    )
                }
            }

            else -> {
                ActionableEvidence(
                    actionableTreatments = setOf(
                        ActionableTreatment(treatment, EvidenceLevel.B, EvidenceTier.I, ActinEvidenceCategory.PRE_CLINICAL)
                    )
                )
            }
        }
    }

    private fun responsiveOffLabelEvidence(offLabelResponsiveEvent: ActionableEvent): ActionableEvidence {
        val treatment = offLabelResponsiveEvent.treatmentName()
        return when (offLabelResponsiveEvent.level()) {
            EvidenceLevel.A -> {
                ActionableEvidence(
                    actionableTreatments = setOf(
                        ActionableTreatment(treatment, EvidenceLevel.B, EvidenceTier.I, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
                    )
                )
            }

            EvidenceLevel.B -> {
                if (offLabelResponsiveEvent.direction().isCertain) {
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(
                                treatment,
                                EvidenceLevel.B,
                                EvidenceTier.I,
                                ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL
                            )
                        )
                    )
                } else {
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(
                                treatment,
                                EvidenceLevel.B,
                                EvidenceTier.I,
                                ActinEvidenceCategory.PRE_CLINICAL
                            )
                        )
                    )
                }
            }

            else -> {
                ActionableEvidence(
                    actionableTreatments = setOf(
                        ActionableTreatment(
                            treatment,
                            offLabelResponsiveEvent.level(),
                            EvidenceTier.II,
                            ActinEvidenceCategory.PRE_CLINICAL
                        )
                    )
                )
            }
        }
    }

    private fun resistantEvidence(resistanceEvent: ActionableEvent): ActionableEvidence {
        val treatment = resistanceEvent.treatmentName()
        return when (resistanceEvent.level()) {
            EvidenceLevel.A, EvidenceLevel.B -> {
                if (resistanceEvent.direction().isCertain) {
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(
                                treatment,
                                resistanceEvent.level(),
                                EvidenceTier.I,
                                ActinEvidenceCategory.KNOWN_RESISTANT
                            )
                        )
                    )
                } else {
                    ActionableEvidence(
                        actionableTreatments = setOf(
                            ActionableTreatment(
                                treatment,
                                resistanceEvent.level(),
                                EvidenceTier.I,
                                ActinEvidenceCategory.SUSPECT_RESISTANT
                            )
                        )
                    )
                }
            }

            else -> {
                ActionableEvidence(
                    actionableTreatments = setOf(
                        ActionableTreatment(
                            treatment,
                            resistanceEvent.level(),
                            EvidenceTier.II,
                            ActinEvidenceCategory.SUSPECT_RESISTANT
                        )
                    )
                )
            }
        }
    }

    fun filterRedundantLowerEvidence(evidence: ActionableEvidence): ActionableEvidence {
        val treatmentsToExcludeForOffLabel =
            filter(evidence, ActinEvidenceCategory.APPROVED) + filter(evidence, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
        val treatmentsToExcludeForPreClinical =
            treatmentsToExcludeForOffLabel + filter(evidence, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)

        return evidence.copy(
            actionableTreatments = cleanTreatments(
                filter(evidence, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL),
                filter(evidence, ActinEvidenceCategory.APPROVED)
            ) + cleanTreatments(filter(evidence, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL), treatmentsToExcludeForOffLabel)
                    + cleanTreatments(filter(evidence, ActinEvidenceCategory.PRE_CLINICAL), treatmentsToExcludeForPreClinical)
                    + cleanTreatments(
                filter(evidence, ActinEvidenceCategory.SUSPECT_RESISTANT),
                filter(evidence, ActinEvidenceCategory.KNOWN_RESISTANT)
            )
        )
    }

    private fun filter(evidence: ActionableEvidence, category: ActinEvidenceCategory) =
        evidence.actionableTreatments.filter { it.category == category }.toSet()

    private fun filterResistanceEvidence(evidence: ActionableEvidence): ActionableEvidence {
        val treatmentsToIncludeForResistance =
            filter(evidence, ActinEvidenceCategory.APPROVED) + filter(evidence, ActinEvidenceCategory.ON_LABEL) + filter(
                evidence,
                ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL
            )
        val applicableKnownResistantTreatments =
            filterTreatments(filter(evidence, ActinEvidenceCategory.KNOWN_RESISTANT), treatmentsToIncludeForResistance)
        val applicableSuspectResistantTreatments =
            filterTreatments(filter(evidence, ActinEvidenceCategory.SUSPECT_RESISTANT), treatmentsToIncludeForResistance)
        return evidence.copy(
            actionableTreatments = applicableKnownResistantTreatments + applicableSuspectResistantTreatments
        )
    }

    private fun filterTreatments(
        treatments: Set<ActionableTreatment>,
        treatmentsToInclude: Set<ActionableTreatment>
    ): Set<ActionableTreatment> {
        return treatments.intersect(treatmentsToInclude)
    }

    private fun cleanTreatments(
        treatments: Set<ActionableTreatment>,
        treatmentsToExclude: Set<ActionableTreatment>
    ): Set<ActionableTreatment> {
        return treatments - treatmentsToExclude
    }
}
