package com.hartwig.actin.doid.datamodel

data class DoidEntry(
    val id: String,
    val nodes: List<Node>,
    val edges: List<Edge>,
    val metadata: GraphMetadata,
    val logicalDefinitionAxioms: List<LogicalDefinitionAxioms>? = null,
    val equivalentNodesSets: List<String>? = null,
    val domainRangeAxioms: List<String>? = null,
    val propertyChainAxioms: List<String>? = null
)
