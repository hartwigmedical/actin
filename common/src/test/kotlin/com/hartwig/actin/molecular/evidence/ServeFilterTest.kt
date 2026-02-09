package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.TestServeFactory.createCountry
import com.hartwig.actin.molecular.evidence.TestServeFactory.createServeDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val COUNTRY_1 = "country1"
private const val COUNTRY_2 = "country2"
private const val COUNTRY_3 = "country3"

class ServeFilterTest {

    private val country1 = createCountry(COUNTRY_1)
    private val country2 = createCountry(COUNTRY_2)
    private val country3 = createCountry(COUNTRY_3)

    @Test
    fun `Should keep trials that are available in a certain country`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)

        val trial1 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country1))
        val trial2 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country1, country2))

        val database = createServeDatabase(evidence, listOf(trial1, trial2))

        val filteredDatabase = ServeFilter.filterCountriesInServeDatabase(database, setOf(COUNTRY_1))

        filteredDatabase.records().values.forEach { record ->
            assertThat(record.trials().size).isEqualTo(2)
            assertThat(record.trials()[0].countries()).contains(country1)
            assertThat(record.trials()[1].countries()).contains(country1, country2)
        }
    }

    @Test
    fun `Should filter trials that are not available in a certain country but keep those which are`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)

        val trial1 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country1))
        val trial2 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country2))

        val database = createServeDatabase(evidence, listOf(trial1, trial2))

        val filteredDatabase = ServeFilter.filterCountriesInServeDatabase(database, setOf(COUNTRY_1))

        filteredDatabase.records().values.forEach { record ->
            assertThat(record.trials().size).isEqualTo(1)
            assertThat(record.trials().first().countries()).contains(country1)
        }
    }

    @Test
    fun `Should filter trials that are not available in certain countries but keep those which are`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)

        val trial1 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country1))
        val trial2 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country3))

        val database = createServeDatabase(evidence, listOf(trial1, trial2))

        database.records().plus(database.records())
        val filteredDatabase = ServeFilter.filterCountriesInServeDatabase(database, setOf(COUNTRY_1, COUNTRY_2))

        filteredDatabase.records().values.forEach { record ->
            assertThat(record.trials().size).isEqualTo(1)
            assertThat(record.trials().first().countries()).contains(country1)
        }
    }

    @Test
    fun `Should keep trials that are available in in multiple countries specified in filter`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)

        val trial1 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country1, country2))
        val trial2 = TestServeTrialFactory.create(anyMolecularCriteria = emptySet(), countries = setOf(country3))

        val database = createServeDatabase(evidence, listOf(trial1, trial2))

        database.records().plus(database.records())
        val filteredDatabase = ServeFilter.filterCountriesInServeDatabase(database, setOf(COUNTRY_1, COUNTRY_2))

        filteredDatabase.records().values.forEach { record ->
            assertThat(record.trials().size).isEqualTo(1)
            assertThat(record.trials().first().countries()).contains(country1)
            assertThat(record.trials().first().countries()).contains(country2)

        }
    }

}