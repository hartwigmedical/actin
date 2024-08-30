package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibleExternalTrialGeneratorFunctionsTest {
    private val externalTrial1 = TestExternalTrialFactory.create(
        "title1",
        setOf(TestExternalTrialFactory.createCountry(CountryName.NETHERLANDS), TestExternalTrialFactory.createCountry(CountryName.GERMANY)),
        "url1",
        "nctId1"
    )
    private val externalTrial2 = TestExternalTrialFactory.create(
        "title2",
        setOf(TestExternalTrialFactory.createCountry(CountryName.BELGIUM, mapOf("Brussels" to emptySet()))),
        "url2",
        "nctId2"
    )
    private val externalTrial3 = TestExternalTrialFactory.create(
        "title3",
        setOf(
            TestExternalTrialFactory.createCountry(
                CountryName.NETHERLANDS,
                mapOf("Nijmegen" to setOf("Radboud UMC", "CWZ"), "Leiden" to setOf("LUMC"))
            )
        ),
        "url3",
        "nctId3"
    )
    private val externalTrial4 =
        TestExternalTrialFactory.create("title4", setOf(TestExternalTrialFactory.createCountry(CountryName.GERMANY)), "url4", "nctId4")

    private val externalTrialsByEvent = mapOf(
        "event1" to listOf(externalTrial1, externalTrial2),
        "event2" to listOf(externalTrial3),
        "event3" to listOf(externalTrial4)
    )

    @Test
    fun `Should return map of lists containing Dutch trials`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.localTrials(externalTrialsByEvent, CountryName.NETHERLANDS))
            .isEqualTo(mapOf("event1" to listOf(externalTrial1), "event2" to listOf(externalTrial3)))
    }

    @Test
    fun `Should return map of lists containing non-Dutch trials`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.nonLocalTrials(externalTrialsByEvent, CountryName.NETHERLANDS))
            .isEqualTo(mapOf("event1" to listOf(externalTrial2), "event3" to listOf(externalTrial4)))
    }

    @Test
    fun `Should return hospitals in home country`() {
        assertThat(
            EligibleExternalTrialGeneratorFunctions.hospitalsInHomeCountry(
                externalTrial3,
                CountryName.NETHERLANDS
            )
        ).isEqualTo(listOf("Radboud UMC", "CWZ", "LUMC"))
    }

    @Test
    fun `Should return country names and cities`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.countryNamesAndCities(externalTrial2)).isEqualTo("Belgium (Brussels)")
    }
}