package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory
import com.hartwig.actin.report.trial.TrialsProvider.Companion.partitionByCountry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val NCT_01 = "NCT00000001"
private const val URL = "url"

private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"

private val NETHERLANDS = CountryDetails(country = Country.NETHERLANDS, hospitalsPerCity = emptyMap())
private val BELGIUM = CountryDetails(country = Country.BELGIUM, hospitalsPerCity = emptyMap())

private val BASE_EXTERNAL_TRIAL = TestExternalTrialFactory.create(
    nctId = NCT_01,
    title = "title",
    countries = sortedSetOf(),
    molecularMatches = setOf(),
    applicableCancerTypes = sortedSetOf(),
    url = URL
)

val TRIAL_MATCHES = setOf(
    TrialMatch(
        identification = TrialIdentification("TRIAL-1", open = true, "TR-1", "Different title of same trial 1", NCT_01, null, null, null, emptySet(), null),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList(),
        nonEvaluableCohorts = emptyList()
    ),
    TrialMatch(
        identification = TrialIdentification("TRIAL-3", open = true, "TR-3", "Different trial 3", "NCT00000003", null, null, null, emptySet(), null),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList(),
        nonEvaluableCohorts = emptyList()
    )
)

class TrialsProviderTest {

    @Test
    fun `Should filter internal trials`() {
        val notFiltered = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(nctId = "NCT00000002"))
        assertThat(
            setOf(
                EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(nctId = NCT_01)),
                notFiltered
            ).filterInternalTrials(TRIAL_MATCHES.toList())
        ).containsExactly(notFiltered)
    }

    @Test
    fun `Should partition by country`() {
        val country1Trial = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS)))
        val country2Trial = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM)))

        val (inCountry, otherCountries) = partitionByCountry(setOf(country1Trial, country2Trial), Country.NETHERLANDS)

        assertThat(inCountry).containsExactly(country1Trial)
        assertThat(otherCountries).containsExactly(country2Trial)
    }

    @Test
    fun `Should filter trials exclusively in childrens hospitals in reference country`() {
        val notFilteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                "Utrecht" to setOf(Hospital("PMC", isChildrensHospital = true)),
                "Amsterdam" to setOf(Hospital("NKI", isChildrensHospital = false))
            )
        )
        val filteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                "Utrecht" to setOf(Hospital("Sophia KinderZiekenhuis", isChildrensHospital = true))
            )
        )
        assertThat(
            setOf(notFilteredHospital, filteredHospital)
                .filterExclusivelyInChildrensHospitalsInReferenceCountry(
                    birthYear = 1960,
                    referenceDate = LocalDate.of(2021, 1, 1),
                    countryOfReference = Country.NETHERLANDS
                )
        ).containsExactly(notFilteredHospital)
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
            countryOfReference = Country.NETHERLANDS
        )
        assertThat(result).containsExactly(notFilteredHospital)
    }

    @Test
    fun `Should filter molecular criteria already matched in interpreted cohorts`() {
        val interpretedCohorts = listOf(
            InterpretedCohortTestFactory.interpretedCohort(
                molecularEvents = setOf(EGFR_TARGET)
            )
        )
        val filtered = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL)
        val notFiltered = EventWithExternalTrial(TMB_TARGET, BASE_EXTERNAL_TRIAL)
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(interpretedCohorts)
        assertThat(result).containsExactly(notFiltered)
    }

    @Test
    fun `Should filter molecular criteria already matched in other trials`() {
        val otherTrial = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL)
        val filtered = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL)
        val notFiltered = EventWithExternalTrial(TMB_TARGET, BASE_EXTERNAL_TRIAL)
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInTrials(setOf(otherTrial))
        assertThat(result).containsExactly(notFiltered)
    }

    private fun countrySet(vararg countries: CountryDetails) = sortedSetOf(Comparator.comparing { it.country }, *countries)

    private fun createExternalTrialSummaryWithHospitals(vararg countryHospitals: Pair<CountryDetails, Map<String, Set<Hospital>>>):
            EventWithExternalTrial {
        val countries = countryHospitals.map { (country, hospitals) ->
            country.copy(hospitalsPerCity = hospitals)
        }.toSortedSet(Comparator.comparing { it.country })

        return EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countries))
    }
}