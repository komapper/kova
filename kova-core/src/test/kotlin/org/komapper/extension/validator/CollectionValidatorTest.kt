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
                result.messages[0].constraintId shouldBe "kova.collection.notEmpty"
            }
        }

        context("length") {
            val validator = Kova.list<String>().length(2)

            test("success") {
                val result = validator.tryValidate(listOf("1", "2"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe listOf("1", "2")
            }

            test("failure with too few elements") {
                val result = validator.tryValidate(listOf("1"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.length"
            }

            test("failure with too many elements") {
                val result = validator.tryValidate(listOf("1", "2", "3"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.length"
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
                result.messages[0].constraintId shouldBe "kova.collection.min"
                result.messages[1].constraintId shouldBe "kova.collection.min"
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
            val validator = Kova.list<String>().onEach { it.length(3) }

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
                        "Some elements do not satisfy the constraint: [must be exactly 3 characters, must be exactly 3 characters]"
                    it.shouldBeInstanceOf<Message.Collection>()
                    it.elements.size shouldBe 2
                    it.elements[0].messages[0].constraintId shouldBe "kova.string.length"
                    it.elements[0]
                        .messages[0]
                        .context.input shouldBe "4567"
                    it.elements[1].messages[0].constraintId shouldBe "kova.string.length"
                    it.elements[1]
                        .messages[0]
                        .context.input shouldBe "8910"
                }
            }

            test("failure when failFast is true") {
                val result = validator.tryValidate(listOf("123", "4567", "8910"), ValidationConfig(failFast = true))
                result.isFailure().mustBeTrue()
                result.messages[0].let {
                    it.constraintId shouldBe "kova.collection.onEach"
                    it.text shouldBe
                        "Some elements do not satisfy the constraint: [must be exactly 3 characters]"
                    it.shouldBeInstanceOf<Message.Collection>()
                    it.elements.size shouldBe 1
                    it.elements[0].messages[0].constraintId shouldBe "kova.string.length"
                }
            }
        }

        context("contains") {
            val validator = Kova.list<String>().contains("foo")

            test("success") {
                val result = validator.tryValidate(listOf("foo", "bar"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe listOf("foo", "bar")
            }

            test("failure") {
                val result = validator.tryValidate(listOf("bar", "baz"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.contains"
            }

            test("failure with empty list") {
                val result = validator.tryValidate(emptyList())
                result.isFailure().mustBeTrue()
            }
        }

        context("notContains") {
            val validator = Kova.list<String>().notContains("foo")

            test("success") {
                val result = validator.tryValidate(listOf("bar", "baz"))
                result.isSuccess().mustBeTrue()
                result.value shouldBe listOf("bar", "baz")
            }

            test("failure") {
                val result = validator.tryValidate(listOf("foo", "bar"))
                result.isFailure().mustBeTrue()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.notContains"
            }

            test("success with empty list") {
                val result = validator.tryValidate(emptyList())
                result.isSuccess().mustBeTrue()
            }
        }

        context("property") {
            data class ListHolder(
                val list: List<String>,
            )

            val schema =
                object : ObjectSchema<ListHolder>({
                    ListHolder::list { e -> e.onEach { it.length(3) } }
                }) {}

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
                    it.constraintId shouldBe "kova.string.length"
                }
            }
        }
    })
