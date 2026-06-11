package com.hartwig.actin.molecular.findings

import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.findings.finding.ConversionUtil
import com.hartwig.hmftools.findings.finding.datamodel.FindingRecord

object FindingRecordFactory {

    fun create(orangeRecord: OrangeRecord): FindingRecord {
        return ConversionUtil.orangeRecordToFindingRecord(orangeRecord, null, null, null)
    }
}