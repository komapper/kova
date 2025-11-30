package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

class CollectionValidatorTest :
    FunSpec({
        context("notEmpty") {
            val validator = Kova.list<String>().notEmpty()

            test("success") {
                val result = validator.tryValidate(listOf("1"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe listOf("1")
            }

            test("failure") {
                val result = validator.tryValidate(emptyList())
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Collection [] must not be empty"
            }
        }

        context("length") {
            val validator = Kova.list<String>().length(2)

            test("success") {
                val result = validator.tryValidate(listOf("1", "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe listOf("1", "2")
            }

            test("failure - too few elements") {
                val result = validator.tryValidate(listOf("1"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Collection [1] must have exactly 2 elements"
            }

            test("failure - too many elements") {
                val result = validator.tryValidate(listOf("1", "2", "3"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Collection [1, 2, 3] must have exactly 2 elements"
            }
        }

        context("plus") {
            val validator = Kova.list<String>().min(2).min(3)

            test("success") {
                val result = validator.tryValidate(listOf("1", "2", "3"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe listOf("1", "2", "3")
            }

            test("failure") {
                val result = validator.tryValidate(listOf("1"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 2
                result.messages[0].content shouldBe "Collection(size=1) must have at least 2 elements"
                result.messages[1].content shouldBe "Collection(size=1) must have at least 3 elements"
            }
        }

        context("constrain") {
            val validator =
                Kova.list<String>().constrain("test") {
                    satisfies(it.input.size == 1, "Constraint failed")
                }

            test("success") {
                val result = validator.tryValidate(listOf("1"))
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(listOf("1", "2"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].content shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            val validator = Kova.list<String>().onEach(Kova.string().length(3))

            test("success") {
                val result = validator.tryValidate(listOf("123", "456"))
                result.isSuccess().mustBeTrue()
            }

            test("failure") {
                val result = validator.tryValidate(listOf("123", "4567", "8910"))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 2
                result.details[0].let {
                    it.root shouldBe ""
                    it.path shouldBe "[1]<collection element>"
                    it.message.constraintId shouldBe "kova.string.length"
                    it.message.content shouldBe "\"4567\" must be exactly 3 characters"
                }
                result.details[1].let {
                    it.root shouldBe ""
                    it.path shouldBe "[2]<collection element>"
                    it.message.constraintId shouldBe "kova.string.length"
                    it.message.content shouldBe "\"8910\" must be exactly 3 characters"
                }
            }

            test("failure - failFast is true") {
                val result = validator.tryValidate(listOf("123", "4567", "8910"), failFast = true)
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldBe ""
                    it.path shouldBe "[1]<collection element>"
                    it.message.content shouldBe "\"4567\" must be exactly 3 characters"
                }
            }
        }

        // TODO
        context("obj and rule") {
            val schema =
                object : ObjectSchema<ListHolder>() {
                    val list =
                        ListHolder::list {
                            Kova.list<String>().onEach(Kova.string().length(3)).constrain("", {
                                ConstraintResult.Satisfied
                            })
                        }
                }

            test("success") {
                val result = schema.tryValidate(ListHolder(listOf("123", "456")))
                result.isSuccess().mustBeTrue()
                result.value shouldBe ListHolder(listOf("123", "456"))
            }

            test("failure") {
                val result = schema.tryValidate(ListHolder(listOf("123", "4567")))
                result.isFailure().mustBeTrue()
                result.details.size shouldBe 1
                result.details[0].let {
                    it.root shouldEndWith $$"$ListHolder"
                    it.path shouldBe "list[1]<collection element>"
                    it.message.content shouldBe "\"4567\" must be exactly 3 characters"
                }
            }
        }
    }) {
    data class ListHolder(
        val list: List<String>,
    )
}
