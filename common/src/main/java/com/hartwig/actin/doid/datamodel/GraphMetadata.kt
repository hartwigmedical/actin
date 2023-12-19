package com.hartwig.actin.doid.datamodel

data class GraphMetadata(
    val subsets: List<String>? = null,
    val xrefs: List<Xref>? = null,
    val basicPropertyValues: List<BasicPropertyValue>? = null,
    val version: String? = null
)
