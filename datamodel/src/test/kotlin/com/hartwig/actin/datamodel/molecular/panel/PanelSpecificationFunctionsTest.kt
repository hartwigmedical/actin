package com.hartwig.actin.datamodel.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class PanelSpecificationFunctionsTest {

    private val referenceDate = LocalDate.of(2024, 1, 1)
    private val recentDate = referenceDate.minusMonths(1)
    private val olderDate = recentDate.minusMonths(1)
    private val testName = "ngs"
    private val feedTest = SequencingTest(test = testName, date = referenceDate)
    private val panelTestSpecs = setOf(
        PanelTestSpecification(testName, versionDate = recentDate),
        PanelTestSpecification(testName, versionDate = olderDate),
        PanelTestSpecification(testName, versionDate = referenceDate.plusMonths(1))
    )

    @Test
    fun `Should return most recent version with date before sequencing test date`() {
        val result = PanelSpecificationFunctions.determineTestVersion(feedTest, panelTestSpecs, registrationDate = referenceDate)
        assertThat(result).isEqualTo(Pair(recentDate, true))
    }

    @Test
    fun `Should prefer test date over registration date`() {
        val result = PanelSpecificationFunctions.determineTestVersion(feedTest, panelTestSpecs, registrationDate = olderDate)
        assertThat(result).isEqualTo(Pair(recentDate, true))
    }

    @Test
    fun `Should use registration date when test date is null`() {
        val result = PanelSpecificationFunctions.determineTestVersion(
            feedTest.copy(date = null),
            panelTestSpecs,
            registrationDate = olderDate
        )
        assertThat(result).isEqualTo(Pair(olderDate, true))
    }

    @Test
    fun `Should return null if panel spec has no date`() {
        val result = PanelSpecificationFunctions.determineTestVersion(
            feedTest,
            setOf(PanelTestSpecification(testName, versionDate = null)),
            registrationDate = olderDate
        )
        assertThat(result).isEqualTo(Pair(null, false))
    }

    @Test
    fun `Should filter out panel specs with non-matching name`() {
        val result = PanelSpecificationFunctions.determineTestVersion(
            feedTest,
            setOf(
                PanelTestSpecification("other_test", versionDate = recentDate),
                PanelTestSpecification(testName, versionDate = olderDate)
            ),
            referenceDate
        )
        assertThat(result).isEqualTo(Pair(olderDate, true))
    }

    @Test
    fun `Should return oldest version date and false when sequencing test date before oldest version date`() {
        val result = PanelSpecificationFunctions.determineTestVersion(
            feedTest.copy(date = recentDate.minusMonths(7)),
            setOf(
                PanelTestSpecification(testName, versionDate = recentDate),
                PanelTestSpecification(testName, versionDate = recentDate.plusMonths(1))
            ),
            referenceDate.minusMonths(7)
        )
        assertThat(result).isEqualTo(Pair(recentDate, false))
    }
}