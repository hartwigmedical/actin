package com.hartwig.actin.datamodel.molecular.panel

import java.time.LocalDate

data class TestVersion(
    val versionDate: LocalDate? = null,
    val testDateIsBeforeOldestTestVersion: Boolean = false
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestVersion

        return versionDate == other.versionDate
    }

    override fun hashCode(): Int {
        return versionDate?.hashCode() ?: 0
    }
}