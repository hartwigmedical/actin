package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchDetails
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
import com.hartwig.serve.datamodel.efficacy.Treatment
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.GenderCriterium
import com.hartwig.serve.datamodel.trial.Hospital as ServeHospital

class ClinicalEvidenceFactory(
    private val cancerTypeResolver: CancerTypeApplicabilityResolver,
    private val patientGender: Gender?
) {

    fun create(actionabilityMatch: ActionabilityMatch): ClinicalEvidence {
        return ClinicalEvidence(
            treatmentEvidence = convertToTreatmentEvidences(actionabilityMatch.evidenceMatches, false) +
                    convertToTreatmentEvidences(actionabilityMatch.indirectEvidenceMatches, true),
            eligibleTrials = convertToExternalTrials(determineOnLabelTrials(actionabilityMatch.matchingCriteriaPerTrialMatch))
        )
    }

    private fun convertToTreatmentEvidences(
        evidences: List<EfficacyEvidence>,
        isIndirect: Boolean
    ): Set<TreatmentEvidence> {
        return evidences.map { evidence ->
            createTreatmentEvidence(cancerTypeResolver.resolve(evidence.indication()), evidence, isIndirect)
        }.toSet()
    }

    private fun createTreatmentEvidence(
        cancerTypeApplicability: CancerTypeMatchApplicability,
        evidence: EfficacyEvidence,
        isIndirect: Boolean
    ): TreatmentEvidence {
        val treatment = evidence.treatment()
        return TreatmentEvidence(
            treatment = treatment.name(),
            treatmentTypes = determineTreatmentTypes(treatment),
            molecularMatch = createMolecularMatchDetails(evidence.molecularCriterium(), isIndirect),
            cancerTypeMatch = CancerTypeMatchDetails(
                cancerType = CancerType(
                    matchedCancerType = evidence.indication().applicableType().name(),
                    excludedCancerSubTypes = evidence.indication().excludedSubTypes().map { ct -> ct.name() }.toSet()
                ), applicability = cancerTypeApplicability
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

    private fun determineTreatmentTypes(treatment: Treatment): Set<String> {
        return treatment.treatmentApproachesDrugClass().ifEmpty { treatment.treatmentApproachesTherapy() }
    }

    private fun determineOnLabelTrials(matchingCriteriaPerTrialMatch: Map<ActionableTrial, Set<MolecularCriterium>>):
            Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>> {
        return matchingCriteriaPerTrialMatch.mapValues { (trial, criteria) ->
            criteria to trial.indications()
                .filter { cancerTypeResolver.resolve(it) == CancerTypeMatchApplicability.SPECIFIC_TYPE }.toSet()
        }
            .filter { (_, criteriaAndIndications) -> criteriaAndIndications.second.isNotEmpty() }
    }

    private fun convertToExternalTrials(
        matchingCriteriaAndIndicationsPerEligibleTrial: Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>>
    ): Set<ExternalTrial> {
        return matchingCriteriaAndIndicationsPerEligibleTrial.mapValues { (actionableTrial, matchingCriteriaAndIndications) ->
            val matchingCriteria = matchingCriteriaAndIndications.first
            val matchingIndications = matchingCriteriaAndIndications.second
            val genderCriterium = actionableTrial.genderCriterium()

            createExternalTrial(actionableTrial, matchingCriteria, matchingIndications, genderCriterium)
        }.values.toSet()
    }

    private fun createExternalTrial(
        trial: ActionableTrial,
        matchingCriteria: Set<MolecularCriterium>,
        matchingIndications: Set<Indication>,
        genderCriterium: GenderCriterium?
    ): ExternalTrial {
        val countries = trial.countries().map {
            CountryDetails(
                country = determineCountry(it.name()),
                hospitalsPerCity = it.hospitalsPerCity()
                    .mapValues { entry -> entry.value.map { hospital -> convertHospital(hospital) }.toSet() })
        }.toSet()

        val molecularMatches = matchingCriteria.map {
            createMolecularMatchDetails(it, false)
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
            genderMatch = matchGender(genderCriterium, patientGender),
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

    private fun createMolecularMatchDetails(molecularCriterium: MolecularCriterium, isIndirect: Boolean): MolecularMatchDetails {
        val (evidenceType, event) = ActionableEventExtraction.extractEvent(molecularCriterium)
        return MolecularMatchDetails(
            sourceDate = event.sourceDate(),
            sourceEvent = event.sourceEvent(),
            evidenceType,
            sourceUrl = event.sourceUrls().first(),
            isIndirect = isIndirect
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

    companion object {
        fun matchGender(genderCriterium: GenderCriterium?, patientGender: Gender?): Boolean? {
            return when {
                genderCriterium == GenderCriterium.BOTH && patientGender in setOf(Gender.FEMALE, Gender.MALE) -> true
                genderCriterium == null || patientGender == null -> null
                else -> patientGender.name == genderCriterium.name
            }
        }
    }
}
