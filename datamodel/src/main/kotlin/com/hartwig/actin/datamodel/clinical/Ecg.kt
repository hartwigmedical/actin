package com.hartwig.actin.datamodel.clinical

data class Ecg(
    val hasSigAberrationLatestECG: Boolean,
    override val name: String?,
    val qtcfMeasure: EcgMeasure?,
    val jtcMeasure: EcgMeasure?,
    override val icdCodes: Set<IcdCode> = emptySet(),
    override val year: Int? = null,
    override val month: Int? = null
) : Comorbidity {
    override val comorbidityClass = ComorbidityClass.ECG

    override fun withDefaultYearAndMonth(defaultYear: Int, defaultMonth: Int): Comorbidity = if (year != null) this else {
        copy(year = defaultYear, month = defaultMonth)
    }
}
