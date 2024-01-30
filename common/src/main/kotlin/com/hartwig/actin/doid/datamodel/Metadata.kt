package com.hartwig.actin.doid.datamodel

data class Metadata(
    val definition: Definition?,
    val subsets: List<String>?,
    val xrefs: List<Xref>?,
    val synonyms: List<Synonym>?,
    val basicPropertyValues: List<BasicPropertyValue>?,
    val snomedConceptId: String?,
    val deprecated: Boolean?,
    val comments: List<String>?
)
