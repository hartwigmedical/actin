package com.hartwig.actin.molecular.paver

import com.hartwig.actin.Displayable

enum class PaveRefGenomeVersion(private val text: String) : Displayable {
    V37("37"),
    V38("38");

    override fun display(): String {
        return text;
    }
}