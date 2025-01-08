package com.hartwig.actin.icd.datamodel

import com.fasterxml.jackson.annotation.JsonProperty

data class SerializedIcdNode(
    @JsonProperty("Foundation URI") val foundationUri: String? = null,
    @JsonProperty("Linearization URI") val linearizationUri: String,
    @JsonProperty("Code") val code: String? = null,
    @JsonProperty("BlockId") val blockId: String? = null,
    @JsonProperty("Title") val title: String,
    @JsonProperty("ClassKind") val classKind: ClassKind,
    @JsonProperty("DepthInKind") val depthInKind: Int,
    @JsonProperty("IsResidual") val isResidual: Boolean,
    @JsonProperty("ChapterNo") val chapterNo: String,
    @JsonProperty("BrowserLink") val browserLink: String,
    @JsonProperty("isLeaf") val isLeaf: Boolean,
    @JsonProperty("Primary tabulation") val primaryTabulation: Boolean? = null,
    @JsonProperty("Grouping1") val grouping1: String? = null,
    @JsonProperty("Grouping2") val grouping2: String? = null,
    @JsonProperty("Grouping3") val grouping3: String? = null,
    @JsonProperty("Grouping4") val grouping4: String? = null,
    @JsonProperty("Grouping5") val grouping5: String? = null
)


