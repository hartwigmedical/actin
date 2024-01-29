package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable

interface TreatmentType : Displayable {
    val category: TreatmentCategory
}
