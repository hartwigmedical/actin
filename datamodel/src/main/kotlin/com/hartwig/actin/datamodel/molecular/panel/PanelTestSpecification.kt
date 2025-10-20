package com.hartwig.actin.datamodel.molecular.panel

data class PanelTestSpecification(
    val testName: String,
    val testVersion: TestVersion = TestVersion(null, false)
) {

    override fun toString(): String = "$testName${testVersion.versionDate?.let { " version $it" } ?: ""}"
}
