package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

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
                result.messages[0].text shouldBe "Collection [] must not be empty"
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
                result.messages[0].text shouldBe "Collection [1] must have exactly 2 elements"
            }

            test("failure - too many elements") {
                val result = validator.tryValidate(listOf("1", "2", "3"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "Collection [1, 2, 3] must have exactly 2 elements"
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
                result.messages[0].text shouldBe "Collection(size=1) must have at least 2 elements"
                result.messages[1].text shouldBe "Collection(size=1) must have at least 3 elements"
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
                result.messages[0].text shouldBe "Constraint failed"
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
                result.messages.size shouldBe 1
                result.messages[0].let {
                    it.constraintId shouldBe "kova.collection.onEach"
                    it.text shouldBe
                        "Some elements in the collection do not satisfy the constraint: [\"4567\" must be exactly 3 characters, \"8910\" must be exactly 3 characters]"
                    it.shouldBeInstanceOf<Message.Collection>()
                    it.elements.size shouldBe 2
                    it.elements[0].messages[0].text shouldBe "\"4567\" must be exactly 3 characters"
                    it.elements[1].messages[0].text shouldBe "\"8910\" must be exactly 3 characters"
                }
            }

            test("failure - failFast is true") {
                val result = validator.tryValidate(listOf("123", "4567", "8910"), ValidationConfig(failFast = true))
                result.isFailure().mustBeTrue()
                result.messages[0].let {
                    it.constraintId shouldBe "kova.collection.onEach"
                    it.text shouldBe
                        "Some elements in the collection do not satisfy the constraint: [\"4567\" must be exactly 3 characters]"
                    it.shouldBeInstanceOf<Message.Collection>()
                    it.elements.size shouldBe 1
                    it.elements[0].messages[0].text shouldBe "\"4567\" must be exactly 3 characters"
                }
            }
        }

        context("property") {
            data class ListHolder(
                val list: List<String>,
            )

            val schema =
                object : ObjectSchema<ListHolder>() {
                    val list =
                        ListHolder::list {
                            Kova.list<String>().onEach(Kova.string().length(3))
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
                result.messages.size shouldBe 1
                val message = result.messages[0]
                message.shouldBeInstanceOf<Message.Collection>()
                message.elements.size shouldBe 1
                message.elements[0].messages[0].let {
                    it.root shouldBe "ListHolder"
                    it.path.fullName shouldBe "list[1]<collection element>"
                    it.text shouldBe "\"4567\" must be exactly 3 characters"
                }
            }
        }
    })
