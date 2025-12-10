package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
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
private const val NCT_04 = "NCT00000004"
private const val URL = "url"
private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"
private const val ROS1_TARGET = "ROS1"
private const val AMSTERDAM = "Amsterdam"
private const val UTRECHT = "Utrecht"

private val NKI = Hospital("NKI", isChildrensHospital = false)
private val PMC = Hospital("PMC", isChildrensHospital = true)
private val NETHERLANDS = CountryDetails(country = Country.NETHERLANDS, hospitalsPerCity = mapOf(AMSTERDAM to setOf(NKI)))
private val BELGIUM = CountryDetails(country = Country.BELGIUM, hospitalsPerCity = emptyMap())
private val EGFR_ACTIONABLE = TestVariantFactory.createMinimal().copy(event = EGFR_TARGET)
private val TMB_ACTIONABLE = TestVariantFactory.createMinimal().copy(event = TMB_TARGET)
private val ROS1_ACTIONABLE = TestVariantFactory.createMinimal().copy(event = ROS1_TARGET)
private val BASE_EXTERNAL_TRIAL = TestExternalTrialFactory.create(nctId = NCT_01, title = "title", url = URL)
private val EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL = ActionableWithExternalTrial(EGFR_ACTIONABLE, BASE_EXTERNAL_TRIAL)
private val TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL = ActionableWithExternalTrial(TMB_ACTIONABLE, BASE_EXTERNAL_TRIAL)
private val ROS1_ACTIONABLE_WITH_EXTERNAL_TRIAL = ActionableWithExternalTrial(ROS1_ACTIONABLE, BASE_EXTERNAL_TRIAL)
private val NETHERLANDS_ROS1 =
    ROS1_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = setOf(NETHERLANDS), nctId = NCT_04))
private val BELGIUM_TMB =
    TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = setOf(BELGIUM), nctId = NCT_02))

private val INTERNAL_TRIAL_IDS = setOf(NCT_01, NCT_03)
private val EVALUABLE_COHORTS = listOf(
    InterpretedCohortTestFactory.interpretedCohort(
        isPotentiallyEligible = true,
        isOpen = true,
        isIgnore = false,
        hasSlotsAvailable = true,
        molecularInclusionEvents = setOf(EGFR_TARGET)
    )
)

class TrialsProviderTest {

    @Test
    fun `Should filter open and eligible cohorts correctly (excluding missing molecular result for evaluation cohorts)`() {
        val cohortToRemain = InterpretedCohortTestFactory.interpretedCohort(
            isPotentiallyEligible = true,
            isOpen = true,
            isMissingMolecularResultForEvaluation = false
        )
        val cohortToFilter1 = cohortToRemain.copy(isPotentiallyEligible = false)
        val cohortToFilter2 = cohortToRemain.copy(isOpen = false)
        val cohortToFilter3 = cohortToRemain.copy(isMissingMolecularResultForEvaluation = true)

        val filtered =
            TrialsProvider.filterCohortsOpenAndEligible(listOf(cohortToRemain, cohortToFilter1, cohortToFilter2, cohortToFilter3))
        assertThat(filtered).containsExactly(cohortToRemain)
    }

    @Test
    fun `Should filter internal trials`() {
        val notFiltered = EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(nctId = NCT_02))
        assertThat(
            setOf(
                EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(nctId = NCT_01)),
                notFiltered
            ).filterInternalTrials(setOf(NCT_01, NCT_03))
        ).containsExactly(notFiltered)
    }

    @Test
    fun `Should filter all trials running in the Netherlands if tumor type is lung cancer`() {
        listOf(Country.NETHERLANDS, Country.BELGIUM).forEach { country ->
            val trialsProvider = trialsProvider(
                setOf(NETHERLANDS_ROS1, BELGIUM_TMB),
                retainOriginalExternalTrials = false,
                isLungCancer = true,
                country = country
            )
            val externalTrials = trialsProvider.externalTrials()
            assertThat(externalTrials.nationalTrials.filtered).isEmpty()
        }
    }

    @Test
    fun `Should not filter international trials if tumor type is lung cancer`() {
        val trialsProvider = trialsProvider(setOf(BELGIUM_TMB), retainOriginalExternalTrials = false, isLungCancer = true)
        val externalTrials = trialsProvider.externalTrials()
        assertThat(externalTrials.internationalTrials.filtered).containsExactly(BELGIUM_TMB)
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
    fun `Should not filter trials exclusively in childrens hospitals in reference country when patient is young adult`() {
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
                molecularInclusionEvents = setOf(EGFR_TARGET)
            )
        )
        val filtered = EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL
        val notFiltered = TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(interpretedCohorts)
        assertThat(result).containsExactly(notFiltered)
    }

    @Test
    fun `Should filter molecular criteria already matched in other trials`() {
        val otherTrial = EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL
        val filtered = EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL
        val notFiltered = TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL
        val result = setOf(filtered, notFiltered).filterMolecularCriteriaAlreadyPresentInTrials(setOf(otherTrial))
        assertThat(result).containsExactly(notFiltered)
    }

    @Test
    fun `Should not filter external trials when retaining original external trials`() {
        val externalTrialsSet: Set<ActionableWithExternalTrial> = setOf(NETHERLANDS_ROS1, BELGIUM_TMB)
        val trialsProvider = trialsProvider(externalTrialsSet, retainOriginalExternalTrials = true)
        val externalTrials = trialsProvider.externalTrialsUnfiltered()

        assertThat(externalTrials.nationalTrials.filtered).isEqualTo(externalTrials.nationalTrials.original)
        assertThat(externalTrials.internationalTrials.filtered).isEqualTo(externalTrials.internationalTrials.original)
        assertThat(externalTrials.nationalTrials.filtered).containsExactly(NETHERLANDS_ROS1)
        assertThat(externalTrials.internationalTrials.filtered).containsExactly(BELGIUM_TMB)
    }

    @Test
    fun `Should filter internal trials from external trials and return filtered and original equal when retaining all external trials`() {
        val country1Trial1 = EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS)))
        val country1Trial2 =
            EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS), nctId = NCT_02))
        val country2Trial1 =
            TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM)))

        val externalTrialsSet: Set<ActionableWithExternalTrial> = setOf(country1Trial1, country1Trial2, country2Trial1)
        val trialsProvider = trialsProvider(externalTrialsSet, retainOriginalExternalTrials = true)
        val externalTrials = trialsProvider.externalTrials()

        assertThat(externalTrials.nationalTrials.filtered).isEqualTo(externalTrials.nationalTrials.original)
        assertThat(externalTrials.internationalTrials.filtered).isEqualTo(externalTrials.internationalTrials.original)
        assertThat(externalTrials.nationalTrials.filtered).containsExactly(country1Trial2)
        assertThat(externalTrials.internationalTrials.filtered).isEmpty()
    }

    @Test
    fun `Should filter internal trials from external and clean in filtered external trials when not retaining all external trials`() {
        // Should be filtered based on INTERNAL_TRIAL_IDS
        val country1Trial1 = EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS)))
        val country1Trial2 = TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(
            trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(NETHERLANDS), nctId = NCT_02)
        )

        // Should be filtered based on INTERNAL_TRIAL_IDS
        val country2Trial1 =
            TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM)))
        val country2Trial2 =
            EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM), nctId = NCT_02))

        // Should be filtered based on national trials with same molecular criteria
        val country2Trial3 = TMB_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(
            trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM), nctId = NCT_02)
        )
        val country2Trial4 = ROS1_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(
            trial = BASE_EXTERNAL_TRIAL.copy(countries = countrySet(BELGIUM), nctId = NCT_02)
        )

        val externalTrialsSet: Set<ActionableWithExternalTrial> =
            setOf(country1Trial1, country1Trial2, country2Trial1, country2Trial2, country2Trial3, country2Trial4)
        val trialsProvider = trialsProvider(externalTrialsSet, retainOriginalExternalTrials = false)
        val externalTrials = trialsProvider.externalTrials()

        assertThat(externalTrials.nationalTrials.original).containsExactly(country1Trial2)
        assertThat(externalTrials.internationalTrials.original).containsExactly(country2Trial2, country2Trial3, country2Trial4)
        assertThat(externalTrials.nationalTrials.filtered).containsExactly(country1Trial2)
        assertThat(externalTrials.internationalTrials.filtered).containsExactly(country2Trial4)
    }

    private fun countrySet(vararg countries: CountryDetails) = sortedSetOf(Comparator.comparing { it.country }, *countries)

    private fun createExternalTrialSummaryWithHospitals(vararg countryHospitals: Pair<CountryDetails, Map<String, Set<Hospital>>>):
            ActionableWithExternalTrial {
        val countries = countryHospitals.map { (country, hospitals) ->
            country.copy(hospitalsPerCity = hospitals)
        }.toSortedSet(Comparator.comparing { it.country })

        return EGFR_ACTIONABLE_WITH_EXTERNAL_TRIAL.copy(trial = BASE_EXTERNAL_TRIAL.copy(countries = countries))
    }

    private fun trialsProvider(
        externalTrialsSet: Set<ActionableWithExternalTrial>,
        retainOriginalExternalTrials: Boolean,
        isLungCancer: Boolean = false,
        country: Country = Country.NETHERLANDS
    ): TrialsProvider {
        return TrialsProvider(
            externalTrialsSet,
            EVALUABLE_COHORTS,
            emptyList(),
            INTERNAL_TRIAL_IDS,
            false,
            isLungCancer,
            country,
            retainOriginalExternalTrials
        )
    }
}