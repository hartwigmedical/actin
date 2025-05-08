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
import com.hartwig.serve.datamodel.trial.Hospital as ServeHospital

object ClinicalEvidenceFactory {

    fun create(
        onLabelEvidences: List<EfficacyEvidence>,
        offLabelEvidences: List<EfficacyEvidence>,
        matchingCriteriaAndIndicationsPerEligibleTrial: Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>>
    ): ClinicalEvidence {
        val onLabelTreatmentEvidences = convertToTreatmentEvidences(isOnLabel = true, evidences = onLabelEvidences)
        val offLabelTreatmentEvidences = convertToTreatmentEvidences(isOnLabel = false, evidences = offLabelEvidences)

        return ClinicalEvidence(
            treatmentEvidence = onLabelTreatmentEvidences + offLabelTreatmentEvidences,
            eligibleTrials = convertToExternalTrials(matchingCriteriaAndIndicationsPerEligibleTrial)
        )
    }

    private fun convertToTreatmentEvidences(isOnLabel: Boolean, evidences: List<EfficacyEvidence>): Set<TreatmentEvidence> {
        return evidences.map { evidence ->
            createTreatmentEvidence(
                isOnLabel,
                evidence)
        }.toSet()
    }

    private fun createTreatmentEvidence(
        isOnLabel: Boolean,
        evidence: EfficacyEvidence,
    ): TreatmentEvidence {
        val treatment = evidence.treatment()
        return TreatmentEvidence(
            treatment = treatment.name(),
            isOnLabel = isOnLabel,
            molecularMatch = extractMolecularMatchDetails(evidence.molecularCriterium()),
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
            efficacyDescription = evidence.efficacyDescription(),
            treatmentApproachesDrugClass = treatment.treatmentApproachesDrugClass(),
            treatmentApproachesTherapy = treatment.treatmentApproachesTherapy()
        )
    }

    private fun convertToExternalTrials(
        matchingCriteriaAndIndicationsPerEligibleTrial: Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>>
    ): Set<ExternalTrial> {
        return matchingCriteriaAndIndicationsPerEligibleTrial.mapValues { (actionableTrial, matchingCriteriaAndIndications) ->
            val matchingCriteria = matchingCriteriaAndIndications.first
            val matchingIndications = matchingCriteriaAndIndications.second

            createExternalTrial(actionableTrial, matchingCriteria, matchingIndications)
        }.values.toSet()
    }

    private fun createExternalTrial(
        trial: ActionableTrial,
        matchingCriteria: Set<MolecularCriterium>,
        matchingIndications: Set<Indication>
    ): ExternalTrial {
        val countries = trial.countries().map {
            CountryDetails(
                country = determineCountry(it.name()),
                hospitalsPerCity = it.hospitalsPerCity()
                    .mapValues { entry -> entry.value.map { hospital -> convertHospital(hospital) }.toSet() })
        }.toSet()

        val molecularMatches = matchingCriteria.map {
            extractMolecularMatchDetails(it)
        }.toSet()

        val applicableCancerTypes = matchingIndications.map { indication ->
            CancerType(
                matchedCancerType = indication.applicableType().name(),
                excludedCancerSubTypes = indication.excludedSubTypes().map { it.name() }.toSet()
            )
        }.toSet()

        val url = trial.urls().find { it.length > 11 && it.takeLast(11).substring(0, 3) == "NCT" }
            ?: throw IllegalStateException("Found no URL ending with a NCT id: " + trial.urls().joinToString(", "))

        return ExternalTrial(
            nctId = trial.nctId(),
            title = trial.title(),
            acronym = trial.acronym(),
            treatments = trial.therapyNames(),
            countries = countries,
            molecularMatches = molecularMatches,
            applicableCancerTypes = applicableCancerTypes,
            url = url,
        )
    }

    private fun convertHospital(serveHospital: ServeHospital): Hospital {
        return Hospital(
            name = serveHospital.name(),
            isChildrensHospital = serveHospital.isChildrensHospital()
        )
    }

    private fun extractMolecularMatchDetails(molecularCriterium: MolecularCriterium): MolecularMatchDetails {
        val (evidenceType, event) = ActionableEventExtraction.extractEvent(molecularCriterium)
        return MolecularMatchDetails(
            sourceDate = event.sourceDate(),
            sourceEvent = event.sourceEvent(),
            evidenceType,
            sourceUrl = event.sourceUrls().first()
        )
    }

    private fun determineCountry(serveCountry: String): Country {
        return when (serveCountry) {
            "Netherlands" -> Country.NETHERLANDS
            "Belgium" -> Country.BELGIUM
            "Germany" -> Country.GERMANY
            "France" -> Country.FRANCE
            "United Kingdom" -> Country.UK
            "United States" -> Country.USA
            else -> Country.OTHER
        }
    }
}
