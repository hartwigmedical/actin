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
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.characteristicsFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.codonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.exonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.fusionFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractCharacteristic
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractFusion
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractGene
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractRange
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.hotspotFilter
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
            geneFilter() to { evidence: EfficacyEvidence -> extractGene(evidence) },
            codonFilter() to { evidence: EfficacyEvidence -> extractRange(evidence) },
            hotspotFilter() to { evidence: EfficacyEvidence -> ActionableEventsExtraction.extractHotspot(evidence) },
            exonFilter() to { evidence: EfficacyEvidence -> extractRange(evidence) },
            fusionFilter() to { evidence: EfficacyEvidence -> extractFusion(evidence) },
            characteristicsFilter() to { evidence: EfficacyEvidence -> extractCharacteristic(evidence) },
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
            geneFilter() to { trial: ActionableTrial -> extractGene(trial) },
            codonFilter() to { trial: ActionableTrial -> extractRange(trial) },
            hotspotFilter() to { trial: ActionableTrial -> ActionableEventsExtraction.extractHotspot(trial) },
            exonFilter() to { trial: ActionableTrial -> extractRange(trial) },
            fusionFilter() to { trial: ActionableTrial -> extractFusion(trial) },
            characteristicsFilter() to { trial: ActionableTrial -> extractCharacteristic(trial) },
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
