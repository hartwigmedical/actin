package com.hartwig.actin.doid.datamodel

data class Edge(
    val subject: String,
    val subjectDoid: String,
    val `object`: String,
    val objectDoid: String,
    val predicate: String
)
