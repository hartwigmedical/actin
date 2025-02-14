package com.hartwig.actin.doid.datamodel

data class Synonym(
    val pred: String,
    val `val`: String,
    val xrefs: List<String>,
    val synonymType: String?
)
