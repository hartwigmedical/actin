package com.hartwig.actin.trial.config

import com.hartwig.actin.datamodel.trial.TrialLocation
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class TrialConfigDatabaseUtilTest {

    @Test
    fun `Should convert to reference ids`() {
        assertThat(TrialConfigDatabaseUtil.toReferenceIds("all")).hasSize(1)
        assertThat(TrialConfigDatabaseUtil.toReferenceIds("I-01")).hasSize(1)
        assertThat(TrialConfigDatabaseUtil.toReferenceIds("")).isEmpty()
        assertThat(TrialConfigDatabaseUtil.toReferenceIds("I-01, I-02")).containsExactlyInAnyOrder("I-01", "I-02")
    }

    @Test
    fun `Should convert to cohorts`() {
        assertThat(TrialConfigDatabaseUtil.toCohorts("all")).hasSize(0)
        assertThat(TrialConfigDatabaseUtil.toCohorts("A")).hasSize(1)
        assertThat(TrialConfigDatabaseUtil.toCohorts("A, B")).containsExactlyInAnyOrder("A", "B")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when cohorts are missing`() {
        TrialConfigDatabaseUtil.toCohorts("")
    }

    @Test
    fun `Should return false when location input is null or empty`() {
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation(null)).isTrue()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation("")).isTrue()
    }

    @Test
    fun `Should return true when input is valid`() {
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation("1,A A")).isTrue()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation("1,A A:2,B B")).isTrue()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation("1,Erasmus MC")).isTrue()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation("1,Erasmus MC:2,Antoni van Leeuwenhoek")).isTrue()
    }

    @Test
    fun `Should return false when input is invalid`() {
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation("1,:2,")).isFalse()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation(",")).isFalse()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation(":")).isFalse()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation(",Erasmus MC:,Antoni van Leeuwenhoek")).isFalse()
        assertThat(TrialConfigDatabaseUtil.validateTrialLocation("1:2,Antoni van Leeuwenhoek")).isFalse()
    }

    @Test
    fun `Should return empty when input is null or empty`() {
        assertThat(TrialConfigDatabaseUtil.toTrialLocation(null)).isEmpty()
        assertThat(TrialConfigDatabaseUtil.toTrialLocation("")).isEmpty()
    }

    @Test
    fun `Should return a list with one location when input is valid`() {
        val locations = TrialConfigDatabaseUtil.toTrialLocation("1,Erasmus MC")
        assertThat(locations).isNotEmpty
        assertThat(locations.size).isEqualTo(1)
        assertThat(locations).containsExactly(TrialLocation(1, "Erasmus MC"))
    }

    @Test
    fun `Should return a list of locations when input is valid`() {
        val locations = TrialConfigDatabaseUtil.toTrialLocation("1,Erasmus MC:2,Antoni van Leeuwenhoek")
        assertThat(locations).isNotEmpty
        assertThat(locations.size).isEqualTo(2)
        assertThat(locations).containsExactly(TrialLocation(1, "Erasmus MC"), TrialLocation(2, "Antoni van Leeuwenhoek"))
    }

    @Test
    fun `Should throw exception when input is invalid`() {
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocation("1,:2,") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocation(",Erasmus MC:,Antoni van Leeuwenhoek") }.isInstanceOf(
            IllegalArgumentException::class.java
        )
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocation("1:2,Antoni van Leeuwenhoek") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocation("A,1:B,2") }.isInstanceOf(IllegalArgumentException::class.java)
    }

}