package com.hartwig.actin.icd.datamodel

data class IcdNode(val code: String, val parentTreeCodes: List<String>, val title: String)
