package com.hartwig.actin.trial.input.datamodel

import com.hartwig.actin.datamodel.Displayable

enum class TumorTypeInput(private val doid: String) : Displayable {
    CARCINOMA("305"),
    ADENOCARCINOMA("299"),
    SQUAMOUS_CELL_CARCINOMA("1749"),
    MELANOMA("1909");

    fun doid(): String {
        return doid
    }

    override fun display(): String {
        return this.toString().replace("_".toRegex(), " ").lowercase()
    }

    companion object {
        fun fromString(string: String): TumorTypeInput {
            return valueOf(string.trim { it <= ' ' }.replace(" ".toRegex(), "_").uppercase())
        }
    }
}
