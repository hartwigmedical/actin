package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestMolecularMatchDetailsFactory
import com.hartwig.actin.report.trial.EventWithExternalTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.util.*

private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"
private const val NCT_01 = "NCT00000001"
private const val NCT_02 = "NCT00000002"
private const val TITLE = "title"
private const val ACRONYM = "acronym"
private const val URL = "url"

private val NETHERLANDS = CountryDetails(country = Country.NETHERLANDS, hospitalsPerCity = emptyMap())
private val BELGIUM = CountryDetails(country = Country.BELGIUM, hospitalsPerCity = emptyMap())
private val TRIAL_1 = TestExternalTrialFactory.create(
    nctId = NCT_01,
    title = TITLE,
    countries = setOf(NETHERLANDS, BELGIUM),
    molecularMatches = setOf(
        TestMolecularMatchDetailsFactory.create(
            sourceDate = LocalDate.of(2023, 2, 3),
            sourceEvent = "source event 1"
        ),
        TestMolecularMatchDetailsFactory.create(
            sourceDate = LocalDate.of(2023, 2, 3),
            sourceEvent = "source event 2"
        )
    ),
    applicableCancerTypes = setOf(CancerType("cancer type 1", emptySet()), CancerType("cancer type 2", emptySet())),
    url = URL,
)
private val TRIAL_2 = TestExternalTrialFactory.create(
    nctId = NCT_02,
    title = TITLE,
    acronym = ACRONYM,
    countries = setOf(BELGIUM),
    molecularMatches = setOf(
        TestMolecularMatchDetailsFactory.create(
            sourceDate = LocalDate.of(2023, 2, 3),
            sourceEvent = "source event 3"
        )
    ),
    applicableCancerTypes = setOf(CancerType("cancer type 3", emptySet())),
    url = URL,
)

class ExternalTrialSummarizerTest {

    @Test
    fun `Should summarize trials by aggregating events, source events and cancer types and sorting by event`() {
        val summarized = ExternalTrialSummarizer.summarize(
            listOf(
                EventWithExternalTrial(TMB_TARGET, TRIAL_1),
                EventWithExternalTrial(TMB_TARGET, TRIAL_2),
                EventWithExternalTrial(EGFR_TARGET, TRIAL_2)
            )
        )
        assertThat(summarized).containsExactly(
            fromExternalTrial(TRIAL_2, countrySet(BELGIUM), sortedSetOf(EGFR_TARGET, TMB_TARGET)),
            fromExternalTrial(TRIAL_1, countrySet(NETHERLANDS, BELGIUM), sortedSetOf(TMB_TARGET))
        )
    }

    private fun countrySet(vararg countries: CountryDetails) = sortedSetOf(Comparator.comparing { it.country }, *countries)

    private fun fromExternalTrial(
        externalTrial: ExternalTrial,
        countries: SortedSet<CountryDetails>,
        actinMolecularEvents: SortedSet<String>,
    ): ExternalTrialSummary {
        return ExternalTrialSummary(
            nctId = externalTrial.nctId,
            title = externalTrial.title(),
            countries = countries,
            actinMolecularEvents = actinMolecularEvents,
            sourceMolecularEvents = externalTrial.molecularMatches.map { it.sourceEvent }.toSortedSet(),
            applicableCancerTypes = externalTrial.applicableCancerTypes.toSortedSet(Comparator.comparing { it.matchedCancerType }),
            url = externalTrial.url
        )
    }
}