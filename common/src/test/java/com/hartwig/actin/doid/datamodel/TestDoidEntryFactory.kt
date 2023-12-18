package com.hartwig.actin.doid.datamodel

object TestDoidEntryFactory {
    @JvmStatic
    fun createMinimalTestDoidEntry(): DoidEntry {
        return ImmutableDoidEntry.builder().id("TEST-DOID").metadata(ImmutableGraphMetadata.builder().build()).build()
    }
}
