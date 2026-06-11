package com.hartwig.actin.molecular.findings

import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.findings.finding.ConversionUtil
import com.hartwig.hmftools.findings.finding.datamodel.FindingRecord

object FindingRecordFactory {

    fun create(orangeRecord: OrangeRecord): FindingRecord {
        return ConversionUtil. and morangeRecordToFindingRecord(orangeRecord, null, null, null)
    }
}