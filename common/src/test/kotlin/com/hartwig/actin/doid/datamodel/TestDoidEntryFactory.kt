package com.hartwig.actin.doid.datamodel

object TestDoidEntryFactory {

    fun createMinimalTestDoidEntry(): DoidEntry {
        return DoidEntry(
            id = "TEST-DOID",
            metadata = GraphMetadata(null, ""),
            nodes = emptyList(),
            edges = emptyList(),
            logicalDefinitionAxioms = emptyList()
        )
    }
}
