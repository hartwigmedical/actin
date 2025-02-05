package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable
import java.time.LocalDate

interface Comorbidity : Displayable {
    val name: String?
    val year: Int?
    val month: Int?
    val icdCodes: Set<IcdCode>
    val comorbidityClass: ComorbidityClass

    override fun display(): String {
        return name ?: ""
    }

    fun withDefaultDate(date: LocalDate): Comorbidity
}