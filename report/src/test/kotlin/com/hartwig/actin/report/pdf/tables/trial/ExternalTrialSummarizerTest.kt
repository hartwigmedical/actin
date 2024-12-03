package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"
private const val NCT_01 = "NCT00000001"
private const val NCT_02 = "NCT00000002"
private const val TITLE = "title"
private const val URL = "url"

private val BASE_EXTERNAL_TRIAL_SUMMARY = ExternalTrialSummary(
    nctId = NCT_01,
    title = "title",
    url = URL,
    actinMolecularEvents = sortedSetOf(),
    sourceMolecularEvents = sortedSetOf(),
    cancerTypes = sortedSetOf(),
    countries = sortedSetOf()
)
private val NETHERLANDS = TestClinicalEvidenceFactory.createCountry(CountryName.NETHERLANDS)
private val BELGIUM = TestClinicalEvidenceFactory.createCountry(CountryName.BELGIUM)

private val TRIAL_1_INSTANCE_1 = TestClinicalEvidenceFactory.createExternalTrial(
    TITLE, setOf(NETHERLANDS), URL, NCT_01
).copy(sourceEvent = "sourceEvent1", applicableCancerType = ApplicableCancerType("cancerType1", emptySet()))
private val TRIAL_1_INSTANCE_2 = TestClinicalEvidenceFactory.createExternalTrial(
    TITLE, setOf(BELGIUM), URL, NCT_01
).copy(sourceEvent = "sourceEvent2", applicableCancerType = ApplicableCancerType("cancerType2", emptySet()))
private val TRIAL_2_INSTANCE_1 = TestClinicalEvidenceFactory.createExternalTrial(
    TITLE, setOf(BELGIUM), URL, NCT_02
).copy(sourceEvent = "sourceEvent3", applicableCancerType = ApplicableCancerType("cancerType3", emptySet()))

private val TRIAL_MATCHES = setOf(
    TrialMatch(
        identification = TrialIdentification("TRIAL-1", true, "TR-1", "Different title of same trial 1", NCT_01),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList(),
        nonEvaluableCohorts = emptyList()
    ),
    TrialMatch(
        identification = TrialIdentification("TRIAL-2", true, "TR-2", "Different trial 2", "NCT00000003"),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList(),
        nonEvaluableCohorts = emptyList()
    )
)

class ExternalTrialSummarizerTest {

    @Test
    fun `Should summarize trials by aggregating events, source events and cancer types and sorting by event`() {
        val summarized = ExternalTrialSummarizer.summarize(
            mapOf(
                TMB_TARGET to setOf(TRIAL_1_INSTANCE_1), EGFR_TARGET to setOf(
                    TRIAL_1_INSTANCE_2, TRIAL_2_INSTANCE_1
                )
            )
        )
        assertThat(summarized).containsExactly(
            ExternalTrialSummary(
                nctId = TRIAL_2_INSTANCE_1.nctId,
                title = TRIAL_2_INSTANCE_1.title,
                url = TRIAL_2_INSTANCE_1.url,
                actinMolecularEvents = sortedSetOf(EGFR_TARGET),
                sourceMolecularEvents = sortedSetOf(TRIAL_2_INSTANCE_1.sourceEvent),
                cancerTypes = sortedSetOf(
                    Comparator.comparing { it.cancerType }, TRIAL_2_INSTANCE_1.applicableCancerType
                ),
                countries = countrySet(BELGIUM),
            ),
            ExternalTrialSummary(
                nctId = TRIAL_1_INSTANCE_1.nctId,
                title = TRIAL_1_INSTANCE_1.title,
                url = TRIAL_1_INSTANCE_1.url,
                actinMolecularEvents = sortedSetOf(TMB_TARGET, EGFR_TARGET),
                sourceMolecularEvents = sortedSetOf(TRIAL_1_INSTANCE_1.sourceEvent, TRIAL_1_INSTANCE_2.sourceEvent),
                cancerTypes = sortedSetOf(
                    Comparator.comparing { it.cancerType },
                    TRIAL_1_INSTANCE_1.applicableCancerType,
                    TRIAL_1_INSTANCE_2.applicableCancerType
                ),
                countries = countrySet(NETHERLANDS, BELGIUM),
            )
        )
    }

    @Test
    fun `Should filter internal trials`() {
        val notFiltered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(nctId = "NCT00000002")
        assertThat(
            setOf(
                BASE_EXTERNAL_TRIAL_SUMMARY.copy(nctId = NCT_01),
                notFiltered
            ).filterInternalTrials(TRIAL_MATCHES)
        ).containsExactly(notFiltered)
    }

    @Test
    fun `Should filter trials exclusively in childrens hospitals in reference country`() {
        val notFilteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                "Utrecht" to setOf(Hospital("PMC", true)),
                "Amsterdam" to setOf(Hospital("NKI", false))
            )
        )
        val filteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                "Utrecht" to setOf(Hospital("Sophia KinderZiekenhuis", true))
            )
        )
        val notFilteredHospitalInOtherCountry = createExternalTrialSummaryWithHospitals(
            BELGIUM to mapOf(
                "Leuven" to setOf(Hospital("Leuven hospital", null))
            )
        )
        assertThat(
            setOf(notFilteredHospital, filteredHospital, notFilteredHospitalInOtherCountry)
                .filterExclusivelyInChildrensHospitalsInReferenceCountry(1960, LocalDate.of(2021, 1, 1), CountryName.NETHERLANDS)
        ).containsExactlyInAnyOrder(notFilteredHospital, notFilteredHospitalInOtherCountry)
    }

    @Test
    fun `Should not filter trials exclusively in childrens hospitals in reference country when patient is younger than 25 years old`() {
        val notFilteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                "Utrecht" to setOf(Hospital("PMC", true))
            )
        )
        val result = setOf(notFilteredHospital).filterExclusivelyInChildrensHospitalsInReferenceCountry(
            birthYear = 2000,
            referenceDate = LocalDate.of(2021, 1, 1),
            countryOfReference = CountryName.NETHERLANDS
        )
        assertThat(result).containsExactlyInAnyOrder(notFilteredHospital)
    }

    @Test
    fun `Should filter trials in home country and not in home country`() {
        val inHomeCountry = BASE_EXTERNAL_TRIAL_SUMMARY.copy(countries = countrySet(NETHERLANDS))
        val notInHomeCountry = BASE_EXTERNAL_TRIAL_SUMMARY.copy(countries = countrySet(BELGIUM))
        assertThat(
            setOf(
                inHomeCountry,
                notInHomeCountry
            ).filterInCountryOfReference(CountryName.NETHERLANDS)
        ).containsExactly(inHomeCountry)
        assertThat(setOf(inHomeCountry, notInHomeCountry).filterNotInCountryOfReference(CountryName.NETHERLANDS)).containsExactly(
            notInHomeCountry
        )
    }

    @Test
    fun `Should filter molecular criteria already matched in hospital trials`() {
        val hospitalLocalEvaluatedCohorts = listOf(
            InterpretedCohortTestFactory.interpretedCohort(
                molecularEvents = setOf(EGFR_TARGET)
            )
        )
        val filtered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(EGFR_TARGET)
        )
        val notFiltered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(TMB_TARGET)
        )
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(hospitalLocalEvaluatedCohorts)
        assertThat(result).containsExactly(notFiltered)
    }

    @Test
    fun `Should filter molecular criteria already matched in national trials`() {
        val nationalTrial = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(EGFR_TARGET)
        )
        val filtered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(EGFR_TARGET)
        )
        val notFiltered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(TMB_TARGET)
        )
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInTrials(setOf(nationalTrial))
        assertThat(result).containsExactly(notFiltered)
    }

    private fun countrySet(vararg countries: Country) = sortedSetOf(Comparator.comparing { it.name }, *countries)

    private fun createExternalTrialSummaryWithHospitals(vararg countryHospitals: Pair<Country, Map<String, Set<Hospital>>>): ExternalTrialSummary {
        val countries = countryHospitals.map { (country, hospitals) ->
            country.copy(hospitalsPerCity = hospitals)
        }.toSortedSet(Comparator.comparing { it.name })
        return BASE_EXTERNAL_TRIAL_SUMMARY.copy(countries = countries)
    }

}