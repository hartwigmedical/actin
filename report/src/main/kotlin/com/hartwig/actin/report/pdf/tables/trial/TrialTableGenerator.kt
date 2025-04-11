package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.pdf.tables.TableGenerator

interface TrialTableGenerator : TableGenerator {

    fun getCohortSize(): Int
}