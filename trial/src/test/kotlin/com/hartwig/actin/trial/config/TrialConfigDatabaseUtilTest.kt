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
    fun `Should return true when trial location input is null or empty`() {
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid(null)).isTrue()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("")).isTrue()
    }

    @Test
    fun `Should return true when trial location input is valid`() {
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("1:A A")).isTrue()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("1:A A,2:B B")).isTrue()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("1:Erasmus MC")).isTrue()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("1:Erasmus MC,2:Antoni van Leeuwenhoek")).isTrue()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("4:Radboud UMC")).isTrue()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("8:MUMC+")).isTrue()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("4:Radboud UMC,8:MUMC+,3:Erasmus MC,7:UMC Groningen")).isTrue()
    }

    @Test
    fun `Should return false when trial location input is invalid`() {
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("1:,2:")).isFalse()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid(",")).isFalse()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid(":")).isFalse()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid(":Erasmus MC,:Antoni van Leeuwenhoek")).isFalse()
        assertThat(TrialConfigDatabaseUtil.trialLocationInputIsValid("1,2:Antoni van Leeuwenhoek")).isFalse()
    }

    @Test
    fun `Should extract empty trial location list when input is null or empty`() {
        assertThat(TrialConfigDatabaseUtil.toTrialLocations(null)).isEmpty()
        assertThat(TrialConfigDatabaseUtil.toTrialLocations("")).isEmpty()
    }

    @Test
    fun `Should extract a list with one trial location when input has one valid location`() {
        val locations = TrialConfigDatabaseUtil.toTrialLocations("1:Erasmus MC")
        assertThat(locations).isNotEmpty
        assertThat(locations.size).isEqualTo(1)
        assertThat(locations).containsExactly(TrialLocation(1, "Erasmus MC"))
    }

    @Test
    fun `Should extract a list with one trial location when input has one valid location with special chart`() {
        val locations = TrialConfigDatabaseUtil.toTrialLocations("8:MUMC+")
        assertThat(locations).isNotEmpty
        assertThat(locations.size).isEqualTo(1)
        assertThat(locations).containsExactly(TrialLocation(8, "MUMC+"))
    }

    @Test
    fun `Should extract a list with multiple trial locations when input has multiple valid locations`() {
        val locations = TrialConfigDatabaseUtil.toTrialLocations("1:Erasmus MC,2:Antoni van Leeuwenhoek")
        assertThat(locations).isNotEmpty
        assertThat(locations.size).isEqualTo(2)
        assertThat(locations).containsExactly(TrialLocation(1, "Erasmus MC"), TrialLocation(2, "Antoni van Leeuwenhoek"))
    }

    @Test
    fun `Should throw exception when trial location input is invalid`() {
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocations("1:,2:") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocations(":Erasmus MC,:Antoni van Leeuwenhoek") }.isInstanceOf(
            IllegalArgumentException::class.java
        )
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocations("1,2:Antoni van Leeuwenhoek") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { TrialConfigDatabaseUtil.toTrialLocations("A:1,B:2") }.isInstanceOf(IllegalArgumentException::class.java)
    }

}