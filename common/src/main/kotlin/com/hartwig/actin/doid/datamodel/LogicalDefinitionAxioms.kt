package com.hartwig.actin.doid.datamodel

data class LogicalDefinitionAxioms(
    val definedClassId: String,
    val genusIds: List<String>,
    val restrictions: List<Restriction>?
)
