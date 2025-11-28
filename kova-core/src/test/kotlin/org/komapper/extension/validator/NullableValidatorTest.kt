package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NullableValidatorTest :
    FunSpec({

        data class Request(
            private val map: Map<String, String>,
        ) : Map<String, String> by map

        context("nullable") {
            val nullable = Kova.nullable<Int>()

            test("success - null") {
                val result = nullable.tryValidate(null)
                result.isSuccess().mustBeTrue()
            }

            test("success - non null") {
                val result = nullable.tryValidate(123)
                result.isSuccess().mustBeTrue()
            }
        }

        context("isNull") {
            val isNull = Kova.int().isNull()

            test("success") {
                val result = isNull.tryValidate(null)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = isNull.tryValidate(4)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value 4 must be null"
            }

            test("failure - min constraint violated") {
                val result = isNull.tryValidate(2)
                result.isFailure().mustBeTrue()
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
                result.isSuccess().mustBeTrue()
            }

            test("success - 3") {
                val result = isNullOrMin3Max3.tryValidate(3)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = isNullOrMin3Max3.tryValidate(5)
                result.isFailure().mustBeTrue()
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
                result.isSuccess().mustBeTrue()
            }

            test("success - non-null") {
                val result = isNullOrMin3Max3.tryValidate(3)
                result.isSuccess().mustBeTrue()
            }

            test("failure - isNull and max3 constraints violated") {
                val result = isNullOrMin3Max3.tryValidate(5)
                result.isFailure().mustBeTrue()
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
                result.isSuccess().mustBeTrue()
            }

            test("success - min3 constraint satisfied") {
                val result = isNullOrMin3OrMin5.tryValidate(3)
                result.isSuccess().mustBeTrue()
            }

            test("success - min3 and min5 constraints satisfied") {
                val result = isNullOrMin3OrMin5.tryValidate(5)
                result.isSuccess().mustBeTrue()
            }

            test("failure - all constraints violated") {
                val result = isNullOrMin3OrMin5.tryValidate(2)
                result.isFailure().mustBeTrue()
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
                result.isSuccess().mustBeTrue()
            }

            test("success - min3 constraint satisfied") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(3)
                result.isSuccess().mustBeTrue()
            }

            test("success - max4 constraint failed") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(5)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 5 must be less than or equal to 4"
            }

            test("failure - all constraints violated") {
                val result = isNullOrMin3OrMin5AndThenMax4.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 3
                result.messages[0].content shouldBe "Value 2 must be null"
                result.messages[1].content shouldBe "Number 2 must be greater than or equal to 3"
                result.messages[2].content shouldBe "Number 2 must be greater than or equal to 5"
            }
        }

        context("notNull and") {
            val max5 = Kova.int().max(5)
            val min3 = Kova.int().min(3)
            val notNullAndMin3AndMax3 = Kova.int().notNull() and min3.asNullable() and max5.asNullable()

            test("success") {
                val result = notNullAndMin3AndMax3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("failure - notNull constraint is violated") {
                val result = notNullAndMin3AndMax3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = notNullAndMin3AndMax3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }

            test("failure - max5 constraint violated") {
                val result = notNullAndMin3AndMax3.tryValidate(6)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 6 must be less than or equal to 5"
            }
        }

        context("notNullAnd - 1 arg") {
            val min3 = Kova.int().min(3)
            val notNullAndMin3 = Kova.int().notNullAnd(min3)

            test("success") {
                val result = notNullAndMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("failure - notNull constraint violated") {
                val result = notNullAndMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint violated") {
                val result = notNullAndMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("notNullAnd - 2 args") {
            val max5 = Kova.int().max(5)
            val min3 = Kova.int().min(3)
            val notNullAndMin3AndMax3 = Kova.int().notNullAnd(min3, max5)

            test("success") {
                val result = notNullAndMin3AndMax3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("failure - notNull constraint violated") {
                val result = notNullAndMin3AndMax3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint violated") {
                val result = notNullAndMin3AndMax3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }

            test("failure - max5 constraint violated") {
                val result = notNullAndMin3AndMax3.tryValidate(6)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 6 must be less than or equal to 5"
            }
        }

        context("notNullAnd - map and andThen") {
            val schema =
                object : ObjectSchema<Request>() {
                    private val notNullAndLength3 = Kova.string().notNullAnd(Kova.string().length(3))
                    val a = map { it["a"] }.andThen(notNullAndLength3)
                }

            test("failure - null") {
                val request = Request(emptyMap())
                val result = schema.a.tryValidate(request)
                result.isFailure().mustBeTrue()
            }

            test("success - non-null") {
                val request = Request(mapOf("a" to "abc"))
                val result = schema.a.tryValidate(request)
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val request = Request(mapOf("a" to "abcd"))
                val result = schema.a.tryValidate(request)
                result.isFailure().mustBeTrue()
            }
        }

        context("whenNotNull") {
            val min3 = Kova.int().min(3)
            val whenNotNullMin3 = Kova.nullable<Int>().whenNotNull(min3)

            test("success - non-null") {
                val result = whenNotNullMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
            }

            test("success - null") {
                val result = whenNotNullMin3.tryValidate(null)
                result.isSuccess().mustBeTrue()
            }

            test("failure - min 3constraint violated") {
                val result = whenNotNullMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
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
                result.isSuccess().mustBeTrue()
            }

            test("success - null") {
                val result = onEachWhenNotNullMin3.tryValidate(listOf(null, null))
                result.isSuccess().mustBeTrue()
            }

            test("failure - min3　constraint violated") {
                val result = onEachWhenNotNullMin3.tryValidate(listOf(2, null))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("notNull and then whenNotNull") {
            val min3 = Kova.int().min(3)
            val notNullAndMin3 =
                Kova
                    .int()
                    .notNull()
                    .whenNotNull(min3)

            test("success - non-null") {
                val result = notNullAndMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int? = result.value // The type is "Int?" instead of "Int"
                value shouldBe 4
            }

            test("failure - notNull constraint is violated") {
                val result = notNullAndMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = notNullAndMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullable without notNull") {
            val min3 = Kova.int().min(3)
            val nullableMin3 =
                min3
                    .asNullable()
                    .asNonNullable()

            test("success - non-null") {
                val result = nullableMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure - null") {
                val result = nullableMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = nullableMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullable with notNull") {
            val min3 = Kova.int().min(3)
            val nullableMin3 =
                min3
                    .asNullable()
                    .notNull()
                    .asNonNullable()

            test("success - non-null") {
                val result = nullableMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int = result.value // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure - null") {
                val result = nullableMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = nullableMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullableThen with notNull") {
            val min3 = Kova.int().min(3)
            val notNullAndMin3 =
                Kova
                    .int()
                    .asNullable()
                    .notNull()
                    .asNonNullableThen(min3)

            test("success - non-null") {
                val result = notNullAndMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int = result.value // The type is Int instead of Int?
                value shouldBe 4
            }

            test("failure - notNull constraint is violated") {
                val result = notNullAndMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = notNullAndMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("asNonNullableThen without notNull") {
            val min3 = Kova.int().min(3)
            val notNullAndMin3 =
                Kova
                    .int()
                    .asNullable()
                    .asNonNullableThen(min3)

            test("success - non-null") {
                val result = notNullAndMin3.tryValidate(4)
                result.isSuccess().mustBeTrue()
                val value: Int = result.value // The type is Int instead of Int?
                value shouldBe 4
            }

            test("failure - notNull constraint is violated") {
                val result = notNullAndMin3.tryValidate(null)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                val result = notNullAndMin3.tryValidate(2)
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }
    })
