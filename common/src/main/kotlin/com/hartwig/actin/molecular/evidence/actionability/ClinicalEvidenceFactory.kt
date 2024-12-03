package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.MolecularMatchDetails
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.characteristicsFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.codonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.exonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractCharacteristic
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractFusion
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractGene
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractRange
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.fusionFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.hotspotFilter
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.time.LocalDate
import com.hartwig.serve.datamodel.trial.Hospital as ServeHospital

object ClinicalEvidenceFactory {

    fun create(
        onLabelEvidences: List<EfficacyEvidence>,
        offLabelEvidences: List<EfficacyEvidence>,
        onLabelTrials: List<ActionableTrial>
    ): ClinicalEvidence {
        val onLabelTreatmentEvidences = convertToTreatmentEvidences(true, onLabelEvidences)
        val offLabelTreatmentEvidences = convertToTreatmentEvidences(false, offLabelEvidences)

        return ClinicalEvidence(
            treatmentEvidence = onLabelTreatmentEvidences + offLabelTreatmentEvidences,
            eligibleTrials = convertToExternalTrials(onLabelTrials)
        )
    }

    private fun convertToTreatmentEvidences(isOnLabel: Boolean, evidences: List<EfficacyEvidence>): Set<TreatmentEvidence> {
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
            treatment = evidence.treatment().name(),
            isOnLabel = isOnLabel,
            molecularMatch = MolecularMatchDetails(sourceEvent, isCategoryEvent),
            applicableCancerType = CancerType(
                evidence.indication().applicableType().name(),
                evidence.indication().excludedSubTypes().map { ct -> ct.name() }.toSet()
            ),
            evidenceLevel = EvidenceLevel.valueOf(evidence.evidenceLevel().name),
            evidenceLevelDetails = EvidenceLevelDetails.valueOf(evidence.evidenceLevelDetails().name),
            evidenceDirection = EvidenceDirection(
                hasPositiveResponse = evidence.evidenceDirection().hasPositiveResponse(),
                hasBenefit = evidence.evidenceDirection().hasBenefit(),
                isResistant = evidence.evidenceDirection().isResistant,
                isCertain = evidence.evidenceDirection().isCertain
            ),
            evidenceDate = sourceDate,
            evidenceYear = evidence.evidenceYear(),
            efficacyDescription = evidence.efficacyDescription()
        )
    }

    private fun convertToExternalTrials(trials: List<ActionableTrial>): Set<ExternalTrial> {
        return listOf(
            geneFilter() to { trial: ActionableTrial -> extractGene(trial) },
            codonFilter() to { trial: ActionableTrial -> extractRange(trial) },
            hotspotFilter() to { trial: ActionableTrial -> ActionableEventsExtraction.extractHotspot(trial) },
            exonFilter() to { trial: ActionableTrial -> extractRange(trial) },
            fusionFilter() to { trial: ActionableTrial -> extractFusion(trial) },
            characteristicsFilter() to { trial: ActionableTrial -> extractCharacteristic(trial) },
        ).flatMap { (filter, extractor) ->
            filterTrials(trials, filter).map {
                createExternalTrial(it, extractor(it).sourceEvent(), extractor(it).isCategoryEvent())
            }
        }.toSet()
    }

    private fun createExternalTrial(trial: ActionableTrial, sourceEvent: String, isCategoryEvent: Boolean): ExternalTrial {
        return ExternalTrial(
            nctId = trial.nctId(),
            title = trial.acronym() ?: trial.title(),
            molecularMatches = setOf(
                MolecularMatchDetails(
                    sourceEvent = sourceEvent,
                    isCategoryEvent = isCategoryEvent
                )
            ),
            applicableCancerTypes = setOf(
                CancerType(
                    trial.indications().iterator().next().applicableType().name(),
                    trial.indications().iterator().next().excludedSubTypes().map { it.name() }.toSet()
                )
            ),
            countries = trial.countries().map {
                CountryDetails(
                    country = determineCountry(it.name()),
                    hospitalsPerCity = it.hospitalsPerCity()
                        .mapValues { entry -> entry.value.map { hospital -> convertHospital(hospital) }.toSet() })
            }.toSet(),
            url = extractNctUrl(trial)
        )
    }

    private fun convertHospital(hospital: ServeHospital): Hospital {
        return Hospital(
            name = hospital.name(),
            isChildrensHospital = hospital.isChildrensHospital()
        )
    }

    private fun determineCountry(countryName: String): Country {
        return when (countryName) {
            "Netherlands" -> Country.NETHERLANDS
            "Belgium" -> Country.BELGIUM
            "Germany" -> Country.GERMANY
            "United States" -> Country.USA
            else -> Country.OTHER
        }
    }

    private fun extractNctUrl(trial: ActionableTrial): String {
        return trial.urls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + trial.urls().joinToString(", "))
    }
}
