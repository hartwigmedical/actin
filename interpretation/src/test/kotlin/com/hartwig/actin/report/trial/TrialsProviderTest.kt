package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val NCT_01 = "NCT00000001"
private const val NCT_02 = "NCT00000002"
private const val NCT_03 = "NCT00000003"
private const val URL = "url"

private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"
private const val ROS1_TARGET = "ROS1"

private val AMSTERDAM = "Amsterdam"
private val UTRECHT = "Utrecht"
private val NKI = Hospital("NKI", isChildrensHospital = false)
private val PMC = Hospital("PMC", isChildrensHospital = true)
private val NETHERLANDS = CountryDetails(country = Country.NETHERLANDS, hospitalsPerCity = mapOf(AMSTERDAM to setOf(NKI)))
private val BELGIUM = CountryDetails(country = Country.BELGIUM, hospitalsPerCity = emptyMap())

private val BASE_EXTERNAL_TRIAL = TestExternalTrialFactory.create(
    nctId = NCT_01,
    title = "title",
    countries = sortedSetOf(),
    molecularMatches = setOf(),
    applicableCancerTypes = sortedSetOf(),
    url = URL
)

private val INTERNAL_TRIAL_IDS = setOf(NCT_01, NCT_03)
private val EVALUABLE_COHORTS = listOf(
    InterpretedCohortTestFactory.interpretedCohort(
        isPotentiallyEligible = true,
        isOpen = true,
        isIgnore = false,
        hasSlotsAvailable = true,
        molecularEvents = setOf(EGFR_TARGET)
    )
)

class TrialsProviderTest {

    @Test
    fun `Should filter internal trials`() {
        val notFiltered = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(NCT_02))
        assertThat(
            setOf(
                EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(nctId = NCT_01)),
                notFiltered
            ).filterInternalTrials(setOf(NCT_01, NCT_03))
        ).containsExactly(notFiltered)
    }

    @Test
    fun `Should filter trials exclusively in childrens hospitals in reference country`() {
        val notFilteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                UTRECHT to setOf(PMC),
                AMSTERDAM to setOf(NKI)
            )
        )
        val filteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                UTRECHT to setOf(Hospital("Sophia KinderZiekenhuis", isChildrensHospital = true))
            )
        )
        assertThat(
            setOf(notFilteredHospital, filteredHospital)
                .filterExclusivelyInChildrensHospitalsInReferenceCountry(
                    false,
                    countryOfReference = Country.NETHERLANDS
                )
        ).containsExactly(notFilteredHospital)
    }

    @Test
    fun `Should not filter trials exclusively in childrens hospitals in reference country when patient is younger than 25 years old`() {
        val notFilteredHospital = createExternalTrialSummaryWithHospitals(
            NETHERLANDS to mapOf(
                UTRECHT to setOf(PMC)
            )
        )
        val result = setOf(notFilteredHospital).filterExclusivelyInChildrensHospitalsInReferenceCountry(
            true,
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

    @Test
    fun `externalTrialsUnfiltered should not filter external trials`() {
        val country1Trial1 =
            EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS), nctId = NCT_01))
        val country2Trial1 = EventWithExternalTrial(TMB_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM), nctId = NCT_02))

        val externalTrialsSet: Set<EventWithExternalTrial> = setOf(country1Trial1, country2Trial1)
        val trialsProvider =
            TrialsProvider(externalTrialsSet, EVALUABLE_COHORTS, listOf(), INTERNAL_TRIAL_IDS, false, Country.NETHERLANDS, true)
        val externalTrials = trialsProvider.externalTrialsUnfiltered()

        assertThat(externalTrials.nationalTrials.filtered).isEqualTo(externalTrials.nationalTrials.original)
        assertThat(externalTrials.internationalTrials.filtered).isEqualTo(externalTrials.internationalTrials.original)
        assertThat(externalTrials.nationalTrials.filtered).containsExactly(country1Trial1)
        assertThat(externalTrials.internationalTrials.filtered).containsExactly(country2Trial1)
    }

    @Test
    fun `externalTrials should filter internal trials and return filtered and original equal with extended mode`() {
        val country1Trial1 = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS)))
        val country1Trial2 =
            EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS), nctId = NCT_02))
        val country2Trial1 = EventWithExternalTrial(TMB_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM)))

        val externalTrialsSet: Set<EventWithExternalTrial> = setOf(country1Trial1, country1Trial2, country2Trial1)
        val trialsProvider =
            TrialsProvider(externalTrialsSet, EVALUABLE_COHORTS, listOf(), INTERNAL_TRIAL_IDS, false, Country.NETHERLANDS, true)
        val externalTrials = trialsProvider.externalTrials()

        assertThat(externalTrials.nationalTrials.filtered).isEqualTo(externalTrials.nationalTrials.original)
        assertThat(externalTrials.internationalTrials.filtered).isEqualTo(externalTrials.internationalTrials.original)
        assertThat(externalTrials.nationalTrials.filtered).containsExactly(country1Trial2)
        assertThat(externalTrials.internationalTrials.filtered).isEmpty()
    }

    @Test
    fun `externalTrials should filter internal trials and filtered and original should be different without extended mode`() {
        // Should be filtered based on INTERNAL_TRIAL_IDS
        val country1Trial1 = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS)))
        // Should be filtered based on matching event in evaluable cohorts
        val country1Trial2 =
            EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS), nctId = NCT_02))
        val country1Trial3 =
            EventWithExternalTrial(TMB_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS), nctId = NCT_02))
        // Should be filtered based on INTERNAL_TRIAL_IDS
        val country2Trial1 = EventWithExternalTrial(TMB_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM)))
        val country2Trial2 = EventWithExternalTrial(EGFR_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM), nctId = NCT_02))
        // Should be filtered based on national trials with same molecular criteria
        val country2Trial3 = EventWithExternalTrial(TMB_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM), nctId = NCT_02))
        val country2Trial4 = EventWithExternalTrial(ROS1_TARGET, BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM), nctId = NCT_02))

        val externalTrialsSet: Set<EventWithExternalTrial> =
            setOf(country1Trial1, country1Trial2, country1Trial3, country2Trial1, country2Trial2, country2Trial3, country2Trial4)
        val trialsProvider =
            TrialsProvider(externalTrialsSet, EVALUABLE_COHORTS, listOf(), INTERNAL_TRIAL_IDS, false, Country.NETHERLANDS, false)
        val externalTrials = trialsProvider.externalTrials()

        assertThat(externalTrials.nationalTrials.original).containsExactly(country1Trial2, country1Trial3)
        assertThat(externalTrials.internationalTrials.original).containsExactly(country2Trial2, country2Trial3, country2Trial4)
        assertThat(externalTrials.nationalTrials.filtered).containsExactly(country1Trial3)
        assertThat(externalTrials.internationalTrials.filtered).containsExactly(country2Trial4)
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