package com.hartwig.actin.datamodel.clinical.treatment

import com.hartwig.actin.datamodel.Displayable

interface TreatmentType : Displayable {
    val category: TreatmentCategory
}
