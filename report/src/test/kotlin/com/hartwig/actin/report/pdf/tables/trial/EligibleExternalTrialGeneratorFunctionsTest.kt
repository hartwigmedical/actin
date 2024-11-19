package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val BASE_EXTERNAL_TRIAL_SUMMARY = ExternalTrialSummary(
    nctId = "nct",
    title = "title",
    url = "url",
    actinMolecularEvents = sortedSetOf(),
    sourceMolecularEvents = sortedSetOf(),
    cancerTypes = sortedSetOf(),
    countries = sortedSetOf(),
    cities = sortedSetOf(),
    hospitals = sortedSetOf()
)

class EligibleExternalTrialGeneratorFunctionsTest {

    private val externalTrialNetherlandsGermany = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
        countries = countrySet(
            TestClinicalEvidenceFactory.createCountry(
                CountryName.NETHERLANDS,
                mapOf("Amsterdam" to setOf("AMC"), "Leiden" to setOf("LUMC"))
            ),
            TestClinicalEvidenceFactory.createCountry(CountryName.GERMANY, mapOf("Berlin" to emptySet()))
        )
    )
    private val externalTrialBelgium = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
        countries = countrySet(
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
        )
    )
    private val externalTrialNetherlands = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
        countries = countrySet(
            TestClinicalEvidenceFactory.createCountry(
                CountryName.NETHERLANDS,
                mapOf(
                    "Nijmegen" to setOf("Radboud UMC", "CWZ"),
                    "Leiden" to setOf("LUMC"),
                    "Amsterdam" to setOf("NKI-AVL", "AMC", "VUmc", "OLVG", "BovenIJ"),
                    "Groningen" to setOf("Martini", "UMCG", "Ommelander")
                )
            )
        )
    )

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
        assertThat(hospitalsAndCitiesExternalTrialNetherlands.first).isEqualTo("Many, please check link")
        assertThat(hospitalsAndCitiesExternalTrialNetherlands.second).isEqualTo("Nijmegen, Leiden, Amsterdam, Groningen")
        assertThat(hospitalsAndCitiesExternalTrialNetherlandsGermany.first).isEqualTo("AMC, LUMC")
        assertThat(hospitalsAndCitiesExternalTrialNetherlandsGermany.second).isEqualTo("Amsterdam, Leiden")
        assertThat(hospitalsAndCitiesExternalTrialBelgium.first).isEqualTo("Brussels hospital")
        assertThat(hospitalsAndCitiesExternalTrialBelgium.second).isEqualTo("Many, please check link")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw illegal state exception if home country not found in external trial`() {
        EligibleExternalTrialGeneratorFunctions.hospitalsAndCitiesInCountry(externalTrialNetherlands, CountryName.BELGIUM)
    }

    @Test
    fun `Should return country names and cities`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(externalTrialNetherlandsGermany)).isEqualTo("Netherlands (Amsterdam, Leiden), Germany (Berlin)")
        assertThat(EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(externalTrialBelgium)).isEqualTo("Belgium (Many, please check link)")
        assertThat(EligibleExternalTrialGeneratorFunctions.countryNamesWithCities(externalTrialNetherlands)).isEqualTo("Netherlands (Nijmegen, Leiden, Amsterdam, Groningen)")
    }

    private fun countrySet(vararg countries: Country) = sortedSetOf(Comparator.comparing { it.name }, *countries)
}