package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.pdf.tables.TableGenerator

interface ActinTrialsGenerator : TableGenerator {

    fun getCohortSize(): Int
}