package com.hartwig.actin.molecular.findings

import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.finding.ConversionUtil
import com.hartwig.hmftools.finding.datamodel.FindingRecord

object FindingRecordFactory {

    fun create(orangeRecord: OrangeRecord): FindingRecord {
        return ConversionUtil.orangeRecordToFindingRecord(orangeRecord, null, null, null)
    }
}