package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.trial.Country
import com.hartwig.serve.datamodel.trial.Hospital
import com.hartwig.serve.datamodel.trial.ImmutableCountry
import com.hartwig.serve.datamodel.trial.ImmutableHospital

object TestServeFactory {

    fun createEmptyIndication(): Indication {
        return createIndicationWithDoid("")
    }

    fun createIndicationWithDoid(doid: String): Indication {
        return ImmutableIndication.builder()
            .applicableType(ImmutableCancerType.builder().name(doid).doid(doid).build())
            .excludedSubTypes(emptySet())
            .build()
    }

    fun createIndicationWithDoidAndExcludedDoid(doid: String, excludedDoid: String): Indication {
        return ImmutableIndication.builder()
            .applicableType(ImmutableCancerType.builder().name(doid).doid(doid).build())
            .addExcludedSubTypes(ImmutableCancerType.builder().name(excludedDoid).doid(excludedDoid).build())
            .build()
    }

    fun createIndicationWithTypeAndExcludedTypes(type: String = "", excludedTypes: Set<String> = emptySet()): Indication {
        return ImmutableIndication.builder()
            .applicableType(ImmutableCancerType.builder().name(type).doid("").build())
            .excludedSubTypes(excludedTypes.map { ImmutableCancerType.builder().name(it).doid("").build() })
            .build()
    }

    fun createCountry(
        name: String = "",
        hospitalsPerCity: Map<String, Set<Hospital>> = emptyMap()
    ): Country {
        return ImmutableCountry.builder().name(name).hospitalsPerCity(hospitalsPerCity).build()
    }


    fun createHospital(name: String = "", isChildrenHospital: Boolean? = null): Hospital {
        return ImmutableHospital.builder().name(name).isChildrensHospital(isChildrenHospital).build()
    }
}
