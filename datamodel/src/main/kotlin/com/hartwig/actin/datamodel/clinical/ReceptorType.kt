package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class ReceptorType : Displayable {
    ER,
    PR,
    HER2;

    override fun display(): String {
        return this.toString()
    }
}