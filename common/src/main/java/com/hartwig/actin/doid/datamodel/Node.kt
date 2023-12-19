package com.hartwig.actin.doid.datamodel

data class Node(
    val doid: String,
    val url: String,
    val term: String?,
    val type: String?,
    val metadata: Metadata?
) {

    fun snomedConceptId(): String? {
        return metadata?.snomedConceptId
    }
}
