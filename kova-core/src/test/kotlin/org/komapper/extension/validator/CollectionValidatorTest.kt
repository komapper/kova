package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class CollectionValidatorTest :
    FunSpec({
        context("notEmpty") {
            test("success") {
                val result = tryValidate { listOf("1").notEmpty() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { emptyList<Nothing>().notEmpty() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.notEmpty"
            }
        }

        context("length") {
            test("success") {
                val result = tryValidate { listOf("1", "2").length(2) }
                result.shouldBeSuccess()
            }

            test("failure with too few elements") {
                val result = tryValidate { listOf("1").length(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.length"
            }

            test("failure with too many elements") {
                val result = tryValidate { listOf("1", "2", "3").length(2) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.length"
            }
        }

        context("plus") {
            context(_: Validation, _: Accumulate)
            fun List<*>.validate() = min(2) then { min(3) }

            test("success") {
                val result = tryValidate { listOf("1", "2", "3").validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("1").validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].constraintId shouldBe "kova.collection.min"
                result.messages[1].constraintId shouldBe "kova.collection.min"
            }
        }

        context("constrain") {
            context(_: Validation, _: Accumulate)
            fun List<*>.validate() = constrain("test") { satisfies(it.size == 1) { text("Constraint failed") } }

            test("success") {
                val result = tryValidate { listOf("1").validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("1", "2").validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            test("success") {
                val result = tryValidate { listOf("123", "456").onEach { it.length(3) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("123", "4567", "8910").onEach { it.length(3) } }
                result.shouldBeFailure()
                result.messages.single().let {
                    it.constraintId shouldBe "kova.collection.onEach"
                    it.text shouldBe
                        "Some elements do not satisfy the constraint: [must be exactly 3 characters, must be exactly 3 characters]"
                    it.args.single().shouldBeInstanceOf<List<Message>> { messages ->
                        messages.size shouldBe 2
                        messages[0].constraintId shouldBe "kova.charSequence.length"
                        messages[0].input shouldBe "4567"
                        messages[1].constraintId shouldBe "kova.charSequence.length"
                        messages[1].input shouldBe "8910"
                    }
                }
            }

            test("failure when failFast is true") {
                val result = tryValidate(ValidationConfig(failFast = true)) { listOf("123", "4567", "8910").onEach { it.length(3) } }
                result.shouldBeFailure()
                result.messages[0].let {
                    it.constraintId shouldBe "kova.collection.onEach"
                    it.text shouldBe
                        "Some elements do not satisfy the constraint: [must be exactly 3 characters]"
                    it.args
                        .single()
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                }
            }
        }

        context("contains") {
            test("success") {
                val result = tryValidate { listOf("foo", "bar").has("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("bar", "baz").has("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.contains"
            }

            test("failure with empty list") {
                val result = tryValidate { emptyList<Nothing>().has("foo") }
                result.shouldBeFailure()
            }
        }

        context("notContains") {
            test("success") {
                val result = tryValidate { listOf("bar", "baz").notContains("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("foo", "bar").notContains("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.collection.notContains"
            }

            test("success with empty list") {
                val result = tryValidate { emptyList<Nothing>().notContains("foo") }
                result.shouldBeSuccess()
            }
        }

        context("property") {
            data class ListHolder(
                val list: List<String>,
            )

            context(_: Validation, _: Accumulate)
            fun ListHolder.validate() = checking { ::list { e -> e.onEach { it.length(3) } } }

            test("success") {
                val result = tryValidate { ListHolder(listOf("123", "456")).validate() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { ListHolder(listOf("123", "4567")).validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                val message = result.messages[0]
                message.args.single().shouldBeInstanceOf<List<Message>>().single().let {
                    it.root shouldBe "ListHolder"
                    it.path.fullName shouldBe "list[1]<collection element>"
                    it.constraintId shouldBe "kova.charSequence.length"
                }
            }
        }
    })
