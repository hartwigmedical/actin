package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.CriterionReference

class CriterionReferenceComparator() : Comparator<CriterionReference> {
    public override fun compare(reference1: CriterionReference, reference2: CriterionReference): Int {
        val ref1Preferred: Boolean = reference1.id().startsWith("I")
        val ref2Preferred: Boolean = reference2.id().startsWith("I")
        if (ref1Preferred && !ref2Preferred) {
            return -1
        } else if (!ref1Preferred && ref2Preferred) {
            return 1
        }
        if ((reference1.id() == reference2.id())) {
            return reference1.text().compareTo(reference2.text())
        } else {
            return reference1.id().compareTo(reference2.id())
        }
    }
}
