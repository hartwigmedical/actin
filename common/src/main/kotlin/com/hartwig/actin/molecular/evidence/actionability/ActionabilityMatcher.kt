package com.hartwig.actin.molecular.evidence.actionability

interface ActionabilityMatcher<T> {

    fun findMatches(event: T): ActionabilityMatch
}
