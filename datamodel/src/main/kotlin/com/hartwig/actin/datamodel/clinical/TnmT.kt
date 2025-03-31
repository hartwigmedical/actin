package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class TnmT(val category: TnmT?) : Displayable {
    T0(null),
    T1(null),
    T1A(T1),
    T1B(T1),
    T1C(T1),
    T2(null),
    T2A(T2),
    T2B(T2),
    T3(null),
    T4(null),
    M1(null),
    M1A(M1),
    M1B(M1),
    M1C(M1);

    override fun display(): String {
        return this.toString()
    }
}