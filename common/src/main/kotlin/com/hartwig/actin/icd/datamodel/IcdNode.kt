package com.hartwig.actin.icd.datamodel

data class IcdNode(
    val foundationUri: String? = null,
    val linearizationUri: String,
    val code: String,
    val blockId: String? = null,
    val title: String,
    val classKind: ClassKind,
    val depthInKind: Int,
    val isResidual: Boolean,
    val chapterNo: String,
    val browserLink: String,
    val isLeaf: Boolean,
    val primaryTabulation: Boolean? = null,
    val grouping1: String? = null,
    val grouping2: String? = null,
    val grouping3: String? = null,
    val grouping4: String? = null,
    val grouping5: String? = null
)