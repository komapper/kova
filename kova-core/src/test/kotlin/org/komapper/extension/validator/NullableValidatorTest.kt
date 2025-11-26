package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.test.assertTrue

class NullableValidatorTest :
    FunSpec({

        data class Request(
            private val map: Map<String, String>,
        ) : Map<String, String> by map

        context("nullable") {
            val nullable = Kova.nullable<Int>()

            test("success - null") {
                val result = nullable.tryValidate(null)
                assertTrue(result.isSuccess())
            }

            test("success - non null") {
                val result = nullable.tryValidate(123)
                assertTrue(result.isSuccess())
            }
        }

        context("isNull") {
            val isNull = Kova.int().isNull()

            test("success") {
                val result = isNull.tryValidate(null)
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = isNull.tryValidate(4)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value 4 must be null"
            }

            test("failure - min constraint violated") {
                val result = isNull.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value 2 must be null"
            }
        }

        context("isNull or") {
            val isNull = Kova.int().isNull()
            val isNullOrMin3Max3 =
                isNull.or(
                    Kova
                        .int()
                        .min(3)
                        .max(3)
                        .asNullable(),
                )

            test("success - null") {
                val result = isNullOrMin3Max3.tryValidate(null)
                assertTrue(result.isSuccess())
            }

            test("success - 3") {
                val result = isNullOrMin3Max3.tryValidate(3)
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val result = isNullOrMin3Max3.tryValidate(5)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "Value 5 must be null"
                result.messages[1].content shouldBe "Number 5 must be less than or equal to 3"
            }
        }

        context("isNullOr - 1 arg") {
            val min3 = Kova.int().min(3)
            val max3 = Kova.int().max(3)
            val isNullOrMin3Max3 = Kova.int().isNullOr(min3 and max3)

            test("success - null") {
                val result = isNullOrMin3Max3.tryValidate(null)
                assertTrue(result.isSuccess())
            }

            test("success - non-null") {
                val result = isNullOrMin3Max3.tryValidate(3)
                assertTrue(result.isSuccess())
            }

            test("failure - isNull and max3 constraints violated") {
                val result = isNullOrMin3Max3.tryValidate(5)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "Value 5 must be null"
                result.messages[1].content shouldBe "Number 5 must be less than or equal to 3"
            }
        }

        context("isNullOr - 2 args") {
            val min3 = Kova.int().min(3)
            val min5 = Kova.int().min(5)
            val isNullOrMin3OrMin5 = Kova.int().isNullOr(min3, min5)

            test("success - isNull constraint satisfied") {
                val result = isNullOrMin3OrMin5.tryValidate(null)
                assertTrue(result.isSuccess())
            }

            test("success - min3 constraint satisfied") {
                val result = isNullOrMin3OrMin5.tryValidate(3)
                assertTrue(result.isSuccess())
            }

            test("success - min3 and min5 constraints satisfied") {
                val result = isNullOrMin3OrMin5.tryValidate(5)
                assertTrue(result.isSuccess())
            }

            test("failure - all constraints violated") {
                val result = isNullOrMin3OrMin5.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 3
                result.messages[0].content shouldBe "Value 2 must be null"
                result.messages[1].content shouldBe "Number 2 must be greater than or equal to 3"
                result.messages[2].content shouldBe "Number 2 must be greater than or equal to 5"
            }
        }

        context("isNullOr - andThen") {
            val min3 = Kova.int().min(3)
            val min5 = Kova.int().min(5)
            val max4 = Kova.int().max(4)
            val isNullOrMin3OrMin5AndThenMax4 =
                Kova
                    .int()
                    .isNullOr(min3, min5)
                    .andThen(max4.asNullable())

            test("success - isNull constraint satisfied") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(null)
                assertTrue(result.isSuccess())
            }

            test("success - min3 constraint satisfied") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(3)
                assertTrue(result.isSuccess())
            }

            test("success - max4 constraint failed") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(5)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 5 must be less than or equal to 4"
            }

            test("failure - all constraints violated") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 3
                result.messages[0].content shouldBe "Value 2 must be null"
                result.messages[1].content shouldBe "Number 2 must be greater than or equal to 3"
                result.messages[2].content shouldBe "Number 2 must be greater than or equal to 5"
            }
        }

        context("isNotNull and") {
            val max5 = Kova.int().max(5)
            val min3 = Kova.int().min(3)
            val isNotNullAndMin3AndMax3 = Kova.int().isNotNull() and min3.asNullable() and max5.asNullable()

            test("success") {
                val result = isNotNullAndMin3AndMax3.tryValidate(4)
                assertTrue(result.isSuccess())
            }

            test("failure - isNotNull constraint is violated") {
                val result = isNotNullAndMin3AndMax3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = isNotNullAndMin3AndMax3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }

            test("failure - max5 constraint violated") {
                val result = isNotNullAndMin3AndMax3.tryValidate(6)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 6 must be less than or equal to 5"
            }
        }

        context("isNotNullAnd - 1 arg") {
            val min3 = Kova.int().min(3)
            val isNotNullAndMin3 = Kova.int().isNotNullAnd(min3)

            test("success") {
                val result = isNotNullAndMin3.tryValidate(4)
                assertTrue(result.isSuccess())
            }

            test("failure - isNotNull constraint violated") {
                val result = isNotNullAndMin3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint violated") {
                val result = isNotNullAndMin3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("isNotNullAnd - 2 args") {
            val max5 = Kova.int().max(5)
            val min3 = Kova.int().min(3)
            val isNotNullAndMin3AndMax3 = Kova.int().isNotNullAnd(min3, max5)

            test("success") {
                val result = isNotNullAndMin3AndMax3.tryValidate(4)
                assertTrue(result.isSuccess())
            }

            test("failure - isNotNull constraint violated") {
                val result = isNotNullAndMin3AndMax3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint violated") {
                val result = isNotNullAndMin3AndMax3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }

            test("failure - max5 constraint violated") {
                val result = isNotNullAndMin3AndMax3.tryValidate(6)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 6 must be less than or equal to 5"
            }
        }

        context("isNotNullAnd - map and andThen") {
            val isNotNullAndLength3 = Kova.string().isNotNullAnd(Kova.string().length(3))
            val validator = Kova.obj<Request>().map { it["a"] }.andThen(isNotNullAndLength3)

            test("failure - null") {
                val request = Request(emptyMap())
                val result = validator.tryValidate(request)
                assertTrue(result.isFailure())
            }

            test("success - non-null") {
                val request = Request(mapOf("a" to "abc"))
                val result = validator.tryValidate(request)
                assertTrue(result.isSuccess())
            }

            test("failure") {
                val request = Request(mapOf("a" to "abcd"))
                val result = validator.tryValidate(request)
                assertTrue(result.isFailure())
            }
        }

        context("whenNotNull") {
            val min3 = Kova.int().min(3)
            val whenNotNullMin3 = Kova.nullable<Int>().whenNotNull(min3)

            test("success - non-null") {
                val result = whenNotNullMin3.tryValidate(4)
                assertTrue(result.isSuccess())
            }

            test("success - null") {
                val result = whenNotNullMin3.tryValidate(null)
                assertTrue(result.isSuccess())
            }

            test("failure - min 3constraint violated") {
                val result = whenNotNullMin3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("whenNotNull - each List element") {
            val min3 = Kova.int().min(3)
            val whenNotNullMin3 = Kova.nullable<Int>().whenNotNull(min3)
            val onEachWhenNotNullMin3 = Kova.list<Int?>().onEach(whenNotNullMin3)

            test("success - non-null") {
                val result = onEachWhenNotNullMin3.tryValidate(listOf(4, 5))
                assertTrue(result.isSuccess())
            }

            test("success - null") {
                val result = onEachWhenNotNullMin3.tryValidate(listOf(null, null))
                assertTrue(result.isSuccess())
            }

            test("failure - min3　constraint violated") {
                val result = onEachWhenNotNullMin3.tryValidate(listOf(2, null))
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("isNotNull and then whenNotNull") {
            val min3 = Kova.int().min(3)
            val isNotNullAndMin3 =
                Kova
                    .int()
                    .isNotNull()
                    .whenNotNull(min3)

            test("success - non-null") {
                val result = isNotNullAndMin3.tryValidate(4)
                assertTrue(result.isSuccess())
                val value: Int? = result.value // The type is "Int?" instead of "Int"
                value shouldBe 4
            }

            test("failure - isNotNull constraint is violated") {
                val result = isNotNullAndMin3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = isNotNullAndMin3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullable without isNotNull") {
            val min3 = Kova.int().min(3)
            val nullableMin3 =
                min3
                    .asNullable()
                    .asNonNullable()

            test("success - non-null") {
                val result = nullableMin3.tryValidate(4)
                assertTrue(result.isSuccess())
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure - null") {
                val result = nullableMin3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = nullableMin3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullable with isNotNull") {
            val min3 = Kova.int().min(3)
            val nullableMin3 =
                min3
                    .asNullable()
                    .isNotNull()
                    .asNonNullable()

            test("success - non-null") {
                val result = nullableMin3.tryValidate(4)
                assertTrue(result.isSuccess())
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure - null") {
                val result = nullableMin3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = nullableMin3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullableThen with isNotNull") {
            val min3 = Kova.int().min(3)
            val isNotNullAndMin3 =
                Kova
                    .int()
                    .asNullable()
                    .isNotNull()
                    .asNonNullableThen(min3)

            test("success - non-null") {
                val result = isNotNullAndMin3.tryValidate(4)
                assertTrue(result.isSuccess())
                val value: Int = result.value // The type is Int instead of Int?
                value shouldBe 4
            }

            test("failure - isNotNull constraint is violated") {
                val result = isNotNullAndMin3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = isNotNullAndMin3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullableThen without isNotNull") {
            val min3 = Kova.int().min(3)
            val isNotNullAndMin3 =
                Kova
                    .int()
                    .asNullable()
                    .asNonNullableThen(min3)

            test("success - non-null") {
                val result = isNotNullAndMin3.tryValidate(4)
                assertTrue(result.isSuccess())
                val value: Int = result.value // The type is Int instead of Int?
                value shouldBe 4
            }

            test("failure - isNotNull constraint is violated") {
                val result = isNotNullAndMin3.tryValidate(null)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = isNotNullAndMin3.tryValidate(2)
                assertTrue(result.isFailure())
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }
    })
