package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.Displayable

enum class VirusType(private val display: String) : Displayable {
    EBV("Epstein-Barr virus"),
    HBV("Hepatitis B virus"),
    HHV8("Human herpesvirus 8"),
    HPV("Human papillomavirus"),
    MCV("Merkel cell polyomavirus"),
    OTHER("Other");

    override fun display(): String = display
}
