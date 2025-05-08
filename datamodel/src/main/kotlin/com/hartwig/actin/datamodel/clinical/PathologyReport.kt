package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

private const val INTERNAL_SOURCE = "internal"

data class PathologyReport(
    val tissueId: String? = null,
    val reportRequested: Boolean,
    val source: String,
    val lab: String? = null,
    val diagnosis: String,
    val externalDate: LocalDate? = null,
    val tissueDate: LocalDate? = null,
    val authorisationDate: LocalDate? = null,
    val report: String
) {
    val isSourceInternal: Boolean
        get() = source.equals(INTERNAL_SOURCE, ignoreCase = true)
}
