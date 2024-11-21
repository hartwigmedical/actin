package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.characteristicsFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.codonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.exonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.fusionFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.getCharacteristic
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.getFusion
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.getGene
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.getRange
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.hotspotFilter
import com.hartwig.actin.molecular.evidence.actionability.isCategoryEvent
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.time.LocalDate

object ClinicalEvidenceFactory {

    fun createNoEvidence(): ClinicalEvidence {
        return ClinicalEvidence()
    }

    fun create(actionabilityMatch: ActionabilityMatch): ClinicalEvidence {
        val onLabelEvidence = createAllTreatmentEvidences(true, actionabilityMatch.onLabelEvidence.evidences)
        val offLabelEvidence = createAllTreatmentEvidences(false, actionabilityMatch.offLabelEvidence.evidences)
        return ClinicalEvidence(
            externalEligibleTrials = createAllExternalTrials(actionabilityMatch.onLabelEvidence.trials),
            treatmentEvidence = onLabelEvidence + offLabelEvidence
        )
    }

    private fun createAllTreatmentEvidences(isOnLabel: Boolean, evidences: List<EfficacyEvidence>): Set<TreatmentEvidence> {
        val filters = listOf(
            geneFilter() to { evidence: EfficacyEvidence -> getGene(evidence) },
            codonFilter() to { evidence: EfficacyEvidence -> getRange(evidence) },
            hotspotFilter() to { evidence: EfficacyEvidence -> ActionableEventsFiltering.getHotspot(evidence) },
            exonFilter() to { evidence: EfficacyEvidence -> getRange(evidence) },
            fusionFilter() to { evidence: EfficacyEvidence -> getFusion(evidence) },
            characteristicsFilter() to { evidence: EfficacyEvidence -> getCharacteristic(evidence) },
        )

        return filters.flatMap { (filter, extractor) ->
            filterEfficacyEvidence(evidences, filter).map {
                createTreatmentEvidence(
                    isOnLabel,
                    it,
                    extractor(it).sourceDate(),
                    extractor(it).sourceEvent(),
                    extractor(it).isCategoryEvent()
                )
            }
        }.toSet()
    }

    private fun createTreatmentEvidence(
        isOnLabel: Boolean,
        evidence: EfficacyEvidence,
        sourceDate: LocalDate,
        sourceEvent: String,
        isCategoryEvent: Boolean
    ): TreatmentEvidence {
        return TreatmentEvidence(
            evidence.treatment().name(),
            EvidenceLevel.valueOf(evidence.evidenceLevel().name),
            isOnLabel,
            EvidenceDirection(
                hasPositiveResponse = evidence.evidenceDirection().hasPositiveResponse(),
                hasBenefit = evidence.evidenceDirection().hasBenefit(),
                isResistant = evidence.evidenceDirection().isResistant,
                isCertain = evidence.evidenceDirection().isCertain
            ),
            sourceDate,
            evidence.efficacyDescription(),
            evidence.evidenceYear(),
            isCategoryEvent,
            sourceEvent,
            evidence.evidenceLevelDetails(),
            ApplicableCancerType(
                evidence.indication().applicableType().name(),
                evidence.indication().excludedSubTypes().map { ct -> ct.name() }.toSet()
            ),
        )
    }

    private fun createAllExternalTrials(trials: List<ActionableTrial>): Set<ExternalTrial> {
        return listOf(
            geneFilter() to { trial: ActionableTrial -> getGene(trial) },
            codonFilter() to { trial: ActionableTrial -> getRange(trial) },
            hotspotFilter() to { trial: ActionableTrial -> ActionableEventsFiltering.getHotspot(trial) },
            exonFilter() to { trial: ActionableTrial -> getRange(trial) },
            fusionFilter() to { trial: ActionableTrial -> getFusion(trial) },
            characteristicsFilter() to { trial: ActionableTrial -> getCharacteristic(trial) },
        ).flatMap { (filter, extractor) ->
            filterAndExpandTrials(trials, filter).map {
                createExternalTrial(it, extractor(it).sourceEvent(), extractor(it).isCategoryEvent())
            }
        }.toSet()
    }

    private fun createExternalTrial(trial: ActionableTrial, sourceEvent: String, isCategoryEvent: Boolean): ExternalTrial {
        return ExternalTrial(
            title = trial.acronym() ?: trial.title(),
            countries = trial.countries()
                .map {
                    Country(
                        name = determineCountryName(it.name()),
                        hospitalsPerCity = it.hospitalsPerCity()
                            .mapValues { entry -> entry.value.map { hospital -> convertHospital(hospital) }.toSet() })
                }
                .toSet(),
            url = extractNctUrl(trial),
            nctId = trial.nctId(),
            applicableCancerType = ApplicableCancerType(
                trial.indications().iterator().next().applicableType().name(),
                trial.indications().iterator().next().excludedSubTypes().map { it.name() }.toSet()
            ),
            isCategoryEvent = isCategoryEvent,
            sourceEvent = sourceEvent,
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY
        )
    }

    private fun convertHospital(hospital: com.hartwig.serve.datamodel.trial.Hospital): com.hartwig.actin.datamodel.molecular.evidence.Hospital {
        return com.hartwig.actin.datamodel.molecular.evidence.Hospital(
            name = hospital.name(),
            isChildrensHospital = hospital.isChildrensHospital()
        )
    }

    private fun determineCountryName(countryName: String): CountryName {
        return when (countryName) {
            "Netherlands" -> CountryName.NETHERLANDS
            "Belgium" -> CountryName.BELGIUM
            "Germany" -> CountryName.GERMANY
            "United States" -> CountryName.US
            else -> CountryName.OTHER
        }
    }

    private fun extractNctUrl(trial: ActionableTrial): String {
        return trial.urls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + trial.urls().joinToString(", "))
    }
}
