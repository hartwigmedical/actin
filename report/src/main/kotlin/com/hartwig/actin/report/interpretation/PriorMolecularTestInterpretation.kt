package com.hartwig.actin.report.interpretation

data class PriorMolecularTestInterpretation(
    val displaySections: List<MolecularTestDisplay>
)

data class MolecularTestDisplay(val type: String, val results: List<String>)

class PriorMolecularTestInterpretationBuilder {
    private val displaySections = mutableListOf<Triple<String, String, String>>()

    fun addTest(type: String, item: String, result: String) {
        displaySections.add(Triple(type, item, result))
    }

    fun build(): PriorMolecularTestInterpretation {
        return PriorMolecularTestInterpretation(displaySections.groupBy { it.first to it.third }
            .map {
                MolecularTestDisplay(it.key.first, it.value.map { t -> t.second })
            })
    }
}