package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.report.trial.ExternalTrialSummary
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val BASE_EXTERNAL_TRIAL_SUMMARY = ExternalTrialSummary(
    nctId = "nct",
    title = "title",
    url = "url",
    actinMolecularEvents = sortedSetOf(),
    sourceMolecularEvents = sortedSetOf(),
    applicableCancerTypes = sortedSetOf(),
    countries = sortedSetOf()
)

class ExternalTrialFunctionsTest {

    private val externalTrialNetherlandsGermany = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
        countries = countrySet(
            CountryDetails(
                Country.NETHERLANDS,
                mapOf("Amsterdam" to setOf(Hospital("AMC", false)), "Leiden" to setOf(Hospital("LUMC", false)))
            ),
            CountryDetails(Country.GERMANY, mapOf("Berlin" to emptySet()))
        )
    )
    private val externalTrialBelgium = BASE_EXTERNAL_TRIAL_SUMMARY.copy(
        countries = countrySet(
            CountryDetails(
                Country.BELGIUM,
                mapOf(
                    "Brussels" to setOf(Hospital("Brussels hospital", null)),
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
            CountryDetails(
                Country.NETHERLANDS,
                mapOf(
                    "Nijmegen" to setOf(Hospital("Radboud UMC", false), Hospital("CWZ", false)),
                    "Leiden" to setOf(Hospital("LUMC", false)),
                    "Amsterdam" to setOf(
                        Hospital("NKI-AVL", false),
                        Hospital("AMC", false),
                        Hospital("VUmc", false),
                        Hospital("OLVG", false),
                        Hospital("BovenIJ", false)
                    )
                )
            )
        )
    )

    @Test
    fun `Should return hospitals and cities in country`() {
        val hospitalsAndCitiesExternalTrialNetherlands = ExternalTrialFunctions.hospitalsAndCitiesInCountry(
            externalTrialNetherlands,
            Country.NETHERLANDS
        )
        val hospitalsAndCitiesExternalTrialNetherlandsGermany = ExternalTrialFunctions.hospitalsAndCitiesInCountry(
            externalTrialNetherlandsGermany,
            Country.NETHERLANDS
        )
        val hospitalsAndCitiesExternalTrialBelgium = ExternalTrialFunctions.hospitalsAndCitiesInCountry(
            externalTrialBelgium,
            Country.BELGIUM
        )
        assertThat(hospitalsAndCitiesExternalTrialNetherlands.first).isEqualTo("3+ locations - see link")
        assertThat(hospitalsAndCitiesExternalTrialNetherlands.second).isEqualTo("Nijmegen, Leiden, Amsterdam")
        assertThat(hospitalsAndCitiesExternalTrialNetherlandsGermany.first).isEqualTo("AMC, LUMC")
        assertThat(hospitalsAndCitiesExternalTrialNetherlandsGermany.second).isEqualTo("Amsterdam, Leiden")
        assertThat(hospitalsAndCitiesExternalTrialBelgium.first).isEqualTo("Brussels hospital")
        assertThat(hospitalsAndCitiesExternalTrialBelgium.second).isEqualTo("3+ locations - see link")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw illegal state exception if country not found in external trial`() {
        ExternalTrialFunctions.hospitalsAndCitiesInCountry(externalTrialNetherlands, Country.BELGIUM)
    }

    @Test
    fun `Should return country names and cities`() {
        assertThat(ExternalTrialFunctions.countryNamesWithCities(externalTrialNetherlandsGermany))
            .isEqualTo("NL (Amsterdam, Leiden), Germany (Berlin)")
        assertThat(ExternalTrialFunctions.countryNamesWithCities(externalTrialBelgium))
            .isEqualTo("Belgium (3+ locations - see link)")
        assertThat(ExternalTrialFunctions.countryNamesWithCities(externalTrialNetherlands))
            .isEqualTo("NL (Nijmegen, Leiden, Amsterdam)")
    }

    private fun countrySet(vararg countries: CountryDetails) = sortedSetOf(Comparator.comparing { it.country }, *countries)
}