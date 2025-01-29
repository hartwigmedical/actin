package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class BodyLocationCategory(private val display: String): Displayable {
    BLADDER("bladder"),
    BONE("bone"),
    BONE_MARROW("bone marrow"),
    BRAIN("brain"),
    BREAST("breast"),
    CNS("CNS"),
    COLORECTUM("colorectum"),
    ESOPHAGUS("esophagus"),
    GALLBLADDER("gallbladder"),
    HEAD_AND_NECK("head and neck"),
    KIDNEY("kidney"),
    LIVER("liver"),
    LUNG("lung"),
    LYMPH_NODE("lymph node"),
    OMENTUM("omentum"),
    PANCREAS("pancreas"),
    PERITONEUM("peritoneum"),
    REPRODUCTIVE_SYSTEM("reproductive system"),
    SPLEEN("spleen"),
    STOMACH("stomach"),
    THYROID_GLAND("thyroid gland"),
    VASCULAR("vascular");

    override fun display(): String {
        return display
    }
}

