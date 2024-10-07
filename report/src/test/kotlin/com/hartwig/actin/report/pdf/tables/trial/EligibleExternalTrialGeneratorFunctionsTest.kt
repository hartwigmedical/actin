package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibleExternalTrialGeneratorFunctionsTest {
    private val externalTrialNetherlandsGermany = TestClinicalEvidenceFactory.createExternalTrial(
        "title1",
        setOf(
            TestClinicalEvidenceFactory.createCountry(
                CountryName.NETHERLANDS,
                mapOf("Amsterdam" to setOf("AMC"), "Leiden" to setOf("LUMC"))
            ),
            TestClinicalEvidenceFactory.createCountry(CountryName.GERMANY, mapOf("Berlin" to emptySet()))
        ),
        "url1",
        "nctId1"
    )
    private val externalTrialBelgium = TestClinicalEvidenceFactory.createExternalTrial(
        "title2",
        setOf(
            TestClinicalEvidenceFactory.createCountry(
                CountryName.BELGIUM,
                mapOf(
                    "Brussels" to setOf("Brussels hospital"),
                    "Liege" to emptySet(),
                    "Gent" to emptySet(),
                    "Antwerp" to emptySet(),
                    "Leuven" to emptySet(),
                    "Mechelen" to emptySet(),
                    "Namur" to emptySet(),
                    "Bruges" to emptySet(),
                    "Charleroi" to emptySet()
                )
            )
        ),
        "url2",
        "nctId2"
    )
    private val externalTrialNetherlands = TestClinicalEvidenceFactory.createExternalTrial(
        "title3",
        setOf(
            TestClinicalEvidenceFactory.createCountry(
                CountryName.NETHERLANDS,
                mapOf(
                    "Nijmegen" to setOf("Radboud UMC", "CWZ"),
                    "Leiden" to setOf("LUMC"),
                    "Amsterdam" to setOf("NKI-AVL", "AMC", "VUmc", "OLVG", "BovenIJ"),
                    "Groningen" to setOf("Martini", "UMCG", "Ommelander")
                )
            )
        ),
        "url3",
        "nctId3"
    )
    private val externalTrialGermany =
        TestClinicalEvidenceFactory.createExternalTrial(
            "title4",
            setOf(TestClinicalEvidenceFactory.createCountry(CountryName.GERMANY)),
            "url4",
            "nctId4"
        )

    private val externalTrialsByEvent = mapOf(
        "event1" to listOf(externalTrialNetherlandsGermany, externalTrialBelgium),
        "event2" to listOf(externalTrialNetherlands),
        "event3" to listOf(externalTrialGermany)
    )

    @Test
    fun `Should return map of lists containing Dutch trials`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.localTrials(externalTrialsByEvent, CountryName.NETHERLANDS))
            .isEqualTo(mapOf("event1" to listOf(externalTrialNetherlandsGermany), "event2" to listOf(externalTrialNetherlands)))
    }

    @Test
    fun `Should return map of lists containing non-Dutch trials`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.nonLocalTrials(externalTrialsByEvent, CountryName.NETHERLANDS))
            .isEqualTo(mapOf("event1" to listOf(externalTrialBelgium), "event3" to listOf(externalTrialGermany)))
        assertThat(EligibleExternalTrialGeneratorFunctions.nonLocalTrials(externalTrialsByEvent, CountryName.GERMANY))
            .isEqualTo(mapOf("event1" to listOf(externalTrialBelgium), "event2" to listOf(externalTrialNetherlands)))
    }

    @Test
    fun `Should return hospitals and cities in home country`() {
        val hospitalsAndCitiesExternalTrialNetherlands = EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(
            externalTrialNetherlands,
            CountryName.NETHERLANDS
        )
        val hospitalsAndCitiesExternalTrialNetherlandsGermany = EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(
            externalTrialNetherlandsGermany,
            CountryName.NETHERLANDS
        )
        val hospitalsAndCitiesExternalTrialBelgium = EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(
            externalTrialBelgium,
            CountryName.BELGIUM
        )
        assertThat(hospitalsAndCitiesExternalTrialNetherlands.first).isEqualTo("Many (please check link)")
        assertThat(hospitalsAndCitiesExternalTrialNetherlands.second).isEqualTo("Nijmegen, Leiden, Amsterdam, Groningen")
        assertThat(hospitalsAndCitiesExternalTrialNetherlandsGermany.first).isEqualTo("AMC, LUMC")
        assertThat(hospitalsAndCitiesExternalTrialNetherlandsGermany.second).isEqualTo("Amsterdam, Leiden")
        assertThat(hospitalsAndCitiesExternalTrialBelgium.first).isEqualTo("Brussels hospital")
        assertThat(hospitalsAndCitiesExternalTrialBelgium.second).isEqualTo("Many (please check link)")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw illegal state exception if home country not found in external trial`() {
        EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(externalTrialNetherlands, CountryName.BELGIUM)
    }

    @Test
    fun `Should return country names and cities`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(externalTrialNetherlandsGermany)).isEqualTo("Netherlands (Amsterdam, Leiden), Germany (Berlin)")
        assertThat(EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(externalTrialBelgium)).isEqualTo("Belgium (Many (please check link))")
        assertThat(EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(externalTrialNetherlands)).isEqualTo("Netherlands (Nijmegen, Leiden, Amsterdam, Groningen)")
    }
}