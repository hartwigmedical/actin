package com.hartwig.actin.datamodel.trial

data class EligibilityFunction(
    val rule: EligibilityRule,
    val parameters: List<Any> = emptyList()
) : Comparable<EligibilityFunction> {

    override fun compareTo(other: EligibilityFunction): Int {
        return Comparator.comparing(EligibilityFunction::rule)
            .thenComparing({ it.parameters.size }, Int::compareTo)
            .thenComparing({ it.parameters.firstOrNull()?.toString() ?: "" }, String::compareTo)
            .compare(this, other)
    }
}
