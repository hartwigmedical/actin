package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.MolecularMatchDetails
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory
import org.assertj.core.api.Assertions
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
    countries = sortedSetOf(),
    actinMolecularEvents = sortedSetOf(),
    sourceMolecularEvents = sortedSetOf(),
    applicableCancerTypes = sortedSetOf(),
    url = URL
)
private val NETHERLANDS = CountryDetails(country = Country.NETHERLANDS, hospitalsPerCity = emptyMap())
private val BELGIUM = CountryDetails(country = Country.BELGIUM, hospitalsPerCity = emptyMap())
private val TRIAL_1 = ExternalTrial(
    nctId = NCT_01,
    title = TITLE,
    countries = setOf(NETHERLANDS, BELGIUM),
    molecularMatches = setOf(
        MolecularMatchDetails(
            sourceDate = LocalDate.of(2023, 2, 3),
            sourceEvent = "source event 1",
            isCategoryEvent = false
        ),
        MolecularMatchDetails(
            sourceDate = LocalDate.of(2023, 2, 3),
            sourceEvent = "source event 2",
            isCategoryEvent = false
        )
    ),
    applicableCancerTypes = setOf(CancerType("cancer type 1", emptySet()), CancerType("cancer type 2", emptySet())),
    url = URL
)
private val TRIAL_2 = ExternalTrial(
    nctId = NCT_02,
    title = TITLE,
    countries = setOf(BELGIUM),
    molecularMatches = setOf(
        MolecularMatchDetails(
            sourceDate = LocalDate.of(2023, 2, 3),
            sourceEvent = "source event 3",
            isCategoryEvent = false
        )
    ),
    applicableCancerTypes = setOf(CancerType("cancer type 3", emptySet())),
    url = URL
)
private val TRIAL_MATCHES = setOf(
    TrialMatch(
        identification = TrialIdentification("TRIAL-1", open = true, "TR-1", "Different title of same trial 1", NCT_01),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList(),
        nonEvaluableCohorts = emptyList()
    ),
    TrialMatch(
        identification = TrialIdentification("TRIAL-3", open = true, "TR-3", "Different trial 3", "NCT00000003"),
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
                TMB_TARGET to setOf(TRIAL_1, TRIAL_2),
                EGFR_TARGET to setOf(TRIAL_2)
            )
        )

        assertThat(summarized).containsExactly(
            ExternalTrialSummary(
                nctId = TRIAL_2.nctId,
                title = TRIAL_2.title,
                countries = countrySet(BELGIUM),
                actinMolecularEvents = sortedSetOf(EGFR_TARGET, TMB_TARGET),
                sourceMolecularEvents = TRIAL_2.molecularMatches.map { it.sourceEvent }.toSortedSet(),
                applicableCancerTypes = TRIAL_2.applicableCancerTypes.toSortedSet(Comparator.comparing { it.matchedCancerType }),
                url = TRIAL_2.url
            ),
            ExternalTrialSummary(
                nctId = TRIAL_1.nctId,
                title = TRIAL_1.title,
                countries = countrySet(NETHERLANDS, BELGIUM),
                actinMolecularEvents = sortedSetOf(TMB_TARGET),
                sourceMolecularEvents = TRIAL_1.molecularMatches.map { it.sourceEvent }.toSortedSet(),
                applicableCancerTypes = TRIAL_1.applicableCancerTypes.toSortedSet(Comparator.comparing { it.matchedCancerType }),
                url = TRIAL_1.url
            )
        )
    }

    @Test
    fun `Should filter internal trials`() {
        val notFiltered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(nctId = "NCT00000002")
        assertThat(
            setOf(BASE_EXTERNAL_TRIAL_SUMMARY.copy(nctId = NCT_01), notFiltered).filterInternalTrials(TRIAL_MATCHES)
        ).containsExactly(notFiltered)
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
    fun `Should filter trials in or not in country`() {
        val country1Trial = BASE_EXTERNAL_TRIAL_SUMMARY.copy(countries = countrySet(NETHERLANDS))
        val country2Trial = BASE_EXTERNAL_TRIAL_SUMMARY.copy(countries = countrySet(BELGIUM))

        assertThat(
            setOf(country1Trial, country2Trial).filterInCountry(country1Trial.countries.first().country)
        ).containsExactly(country1Trial)

        assertThat(
            setOf(country1Trial, country2Trial).filterNotInCountry(country1Trial.countries.first().country)
        ).containsExactly(country2Trial)
    }

    @Test
    fun `Should filter molecular criteria already matched in interpreted cohorts`() {
        val interpretedCohorts = listOf(
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
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(interpretedCohorts)
        assertThat(result).containsExactly(notFiltered)
    }

    @Test
    fun `Should filter molecular criteria already matched in other trials`() {
        val otherTrial = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(EGFR_TARGET)
        )
        val filtered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(EGFR_TARGET)
        )
        val notFiltered = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
            actinMolecularEvents = sortedSetOf(TMB_TARGET)
        )
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInTrials(setOf(otherTrial))
        assertThat(result).containsExactly(notFiltered)
    }

    private fun countrySet(vararg countries: CountryDetails) = sortedSetOf(Comparator.comparing { it.country }, *countries)

    private fun createExternalTrialSummaryWithHospitals(vararg countryHospitals: Pair<CountryDetails, Map<String, Set<Hospital>>>):
            ExternalTrialSummary {
        val countries = countryHospitals.map { (country, hospitals) ->
            country.copy(hospitalsPerCity = hospitals)
        }.toSortedSet(Comparator.comparing { it.country })

        return BASE_EXTERNAL_TRIAL_SUMMARY.copy(countries = countries)
    }
}