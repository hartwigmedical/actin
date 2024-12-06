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
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.time.LocalDate
import com.hartwig.serve.datamodel.trial.Hospital as ServeHospital

object ClinicalEvidenceFactory {

    fun create(
        onLabelEvidences: List<EfficacyEvidence>,
        offLabelEvidences: List<EfficacyEvidence>,
        matchingCriteriaAndIndicationsPerEligibleTrial: Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>>
    ): ClinicalEvidence {
        val onLabelTreatmentEvidences = convertToTreatmentEvidences(true, onLabelEvidences)
        val offLabelTreatmentEvidences = convertToTreatmentEvidences(false, offLabelEvidences)

        return ClinicalEvidence(
            treatmentEvidence = onLabelTreatmentEvidences + offLabelTreatmentEvidences,
            eligibleTrials = convertToExternalTrials(matchingCriteriaAndIndicationsPerEligibleTrial)
        )
    }

    private fun convertToTreatmentEvidences(isOnLabel: Boolean, evidences: List<EfficacyEvidence>): Set<TreatmentEvidence> {
        return evidences.map { evidence ->
            val actionableEvent = ActionableEventsExtraction.extractEvent(evidence.molecularCriterium())
            createTreatmentEvidence(
                isOnLabel,
                evidence,
                actionableEvent.sourceDate(),
                actionableEvent.sourceEvent(),
                actionableEvent.isCategoryEvent()
            )
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
            molecularMatch = MolecularMatchDetails(sourceDate = sourceDate, sourceEvent = sourceEvent, isCategoryEvent = isCategoryEvent),
            applicableCancerType = CancerType(
                matchedCancerType = evidence.indication().applicableType().name(),
                excludedCancerSubTypes = evidence.indication().excludedSubTypes().map { ct -> ct.name() }.toSet()
            ),
            evidenceLevel = EvidenceLevel.valueOf(evidence.evidenceLevel().name),
            evidenceLevelDetails = EvidenceLevelDetails.valueOf(evidence.evidenceLevelDetails().name),
            evidenceDirection = EvidenceDirection(
                hasPositiveResponse = evidence.evidenceDirection().hasPositiveResponse(),
                hasBenefit = evidence.evidenceDirection().hasBenefit(),
                isResistant = evidence.evidenceDirection().isResistant,
                isCertain = evidence.evidenceDirection().isCertain
            ),
            evidenceYear = evidence.evidenceYear(),
            efficacyDescription = evidence.efficacyDescription()
        )
    }

    private fun convertToExternalTrials(
        matchingCriteriaAndIndicationsPerEligibleTrial: Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>>
    ): Set<ExternalTrial> {
        // TODO Implement properly for N criteria and M indications.
        return matchingCriteriaAndIndicationsPerEligibleTrial.keys.map { actionableTrial ->
            val matchingCriteriaAndIndications = matchingCriteriaAndIndicationsPerEligibleTrial[actionableTrial]
            val actionableEvent = ActionableEventsExtraction.extractEvent(matchingCriteriaAndIndications!!.first.first())

            createExternalTrial(
                actionableTrial,
                actionableEvent.sourceDate(),
                actionableEvent.sourceEvent(),
                actionableEvent.isCategoryEvent()
            )
        }.toSet()
    }

    private fun createExternalTrial(
        trial: ActionableTrial,
        sourceDate: LocalDate,
        sourceEvent: String,
        isCategoryEvent: Boolean
    ): ExternalTrial {
        return ExternalTrial(
            nctId = trial.nctId(),
            title = trial.acronym() ?: trial.title(),
            countries = trial.countries().map {
                CountryDetails(
                    country = determineCountry(it.name()),
                    hospitalsPerCity = it.hospitalsPerCity()
                        .mapValues { entry -> entry.value.map { hospital -> convertHospital(hospital) }.toSet() })
            }.toSet(),
            molecularMatches = setOf(
                MolecularMatchDetails(sourceDate = sourceDate, sourceEvent = sourceEvent, isCategoryEvent = isCategoryEvent)
            ),
            applicableCancerTypes = setOf(
                CancerType(
                    matchedCancerType = trial.indications().iterator().next().applicableType().name(),
                    excludedCancerSubTypes = trial.indications().iterator().next().excludedSubTypes().map { it.name() }.toSet()
                )
            ),
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
