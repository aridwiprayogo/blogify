package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.annotations.Cardinality
import blogify.backend.persistence.postgres.orm.models.PropertyMapping.AssociativeMapping.Cardinality.*
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.ext.ok

import com.andreapivetta.kolor.red

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AssociativePropertyMapperTest {

    class Dummy

    data class TestClass1 (
        val name: String,
        val dummy: Dummy
    ) : Mapped()

    val dummyHandle = TestClass1::class.cachedPropMap().ok().values.first { it.name == "dummy" }

    @Test fun `should find one-to-one cardinality properly`() {
        val cardinality = AssociativePropertyMapper.findCardinality(dummyHandle, TestClass1::class)

        assertEquals(ONE_TO_ONE, cardinality)
    }

    data class TestClass2 (
        val name: String,
        val dummyNullable: Dummy?
    ) : Mapped()

    val dummyNullableHandle = TestClass2::class.cachedPropMap().ok().values.first { it.name == "dummyNullable" }

    @Test fun `should find one-to-one-or-none cardinality properly`() {
        val cardinality = AssociativePropertyMapper.findCardinality(dummyNullableHandle, TestClass2::class)

        assertEquals(ONE_TO_ONE_OR_NONE, cardinality)
    }

    data class TestClass3 (
        val name: String,
        val dummySet: Set<@Cardinality(ONE_TO_MANY) Dummy>
    ) : Mapped()

    val dummySetHandle = TestClass3::class.cachedPropMap().ok().values.first { it.name == "dummySet" }

    @Test fun `should find one-to-many cardinality properly`() {
        val cardinality = AssociativePropertyMapper.findCardinality(dummySetHandle, TestClass3::class)

        assertEquals(ONE_TO_MANY, cardinality)
    }

    data class TestClass4 (
        val name: String,
        val dummySetInvalid: Set<Dummy>
    ) : Mapped()

    val dummySetInvalidHandle = TestClass4::class.cachedPropMap().ok().values.first { it.name == "dummySetInvalid" }

    @Test fun `should throw an exception when checking cardinality of collection property with no cardinality annotation`() {
        assertThrows(IllegalStateException::class.java, {
            val cardinality = AssociativePropertyMapper.findCardinality(dummySetInvalidHandle, TestClass4::class)
        }, "fatal: no cardinality annotation on collection element type for property 'dummySetInvalid' of class 'TestClass4'".red())
    }

}