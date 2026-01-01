package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Locale

class IterableValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("notEmpty") {
            test("success") {
                val result = tryValidate { notEmpty(listOf("1")) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { notEmpty(emptyList<Nothing>()) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.notEmpty"
            }
        }

        context("onEach") {
            test("success") {
                val result = tryValidate { onEach(listOf("123", "456")) { length(it, 3) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { onEach(listOf("123", "4567", "8910")) { length(it, 3) } }
                result.shouldBeFailure()
                result.messages.single().let {
                    it.constraintId shouldBe "kova.iterable.onEach"
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
                val result =
                    tryValidate(ValidationConfig(failFast = true)) {
                        onEach(listOf("123", "4567", "8910")) {
                            length(
                                it,
                                3,
                            )
                        }
                    }
                result.shouldBeFailure()
                result.messages[0].let {
                    it.constraintId shouldBe "kova.iterable.onEach"
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

        context("has") {
            test("success") {
                val result = tryValidate { has(listOf("foo", "bar"), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { has(listOf("bar", "baz"), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.contains"
            }

            test("failure with empty list") {
                val result = tryValidate { has(emptyList<Nothing>(), "foo") }
                result.shouldBeFailure()
            }
        }

        context("contains") {
            test("success") {
                val result = tryValidate { contains(listOf("foo", "bar"), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { contains(listOf("bar", "baz"), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.contains"
            }

            test("failure with empty list") {
                val result = tryValidate { contains(emptyList<Nothing>(), "foo") }
                result.shouldBeFailure()
            }
        }

        context("notContains") {
            test("success") {
                val result = tryValidate { notContains(listOf("bar", "baz"), "foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { notContains(listOf("foo", "bar"), "foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.notContains"
            }

            test("success with empty list") {
                val result = tryValidate { notContains(emptyList<Nothing>(), "foo") }
                result.shouldBeSuccess()
            }
        }

        context("property") {
            data class ListHolder(
                val list: List<String>,
            )

            fun Validation.validate(holder: ListHolder) = holder.schema { holder::list { e -> onEach(e) { length(it, 3) } } }

            test("success") {
                val result = tryValidate { validate(ListHolder(listOf("123", "456"))) }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { validate(ListHolder(listOf("123", "4567"))) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                val message = result.messages[0]
                message.args.single().shouldBeInstanceOf<List<Message>>().single().let {
                    it.root shouldBe "ListHolder"
                    it.path.fullName shouldBe "list[1]<iterable element>"
                    it.constraintId shouldBe "kova.charSequence.length"
                }
            }
        }
    })
