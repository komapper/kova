package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Locale

class IterableValidatorTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("ensureNotEmpty") {
            test("success") {
                val result = tryValidate { listOf("1").ensureNotEmpty() }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { emptyList<Nothing>().ensureNotEmpty() }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.notEmpty"
            }
        }

        context("ensureEach") {
            test("success") {
                val result = tryValidate { listOf("123", "456").ensureEach { it.ensureLength(3) } }
                result.shouldBeSuccess()
            }

            test("failure") {
                val input = listOf("123", "4567", "8910")
                val result = tryValidate { input.ensureEach { it.ensureLength(3) } }
                result.shouldBeFailure()
                result.messages.single().let {
                    it.constraintId shouldBe "kova.iterable.each"
                    it.text shouldBe
                        "Some elements do not satisfy the constraint: [must be exactly 3 characters, must be exactly 3 characters]"
                    it.input shouldBe input
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
                        listOf("123", "4567", "8910").ensureEach {
                            it.ensureLength(
                                3,
                            )
                        }
                    }
                result.shouldBeFailure()
                result.messages[0].let {
                    it.constraintId shouldBe "kova.iterable.each"
                    it.text shouldBe
                        "Some elements do not satisfy the constraint: [must be exactly 3 characters]"
                    it.input shouldBe listOf("123", "4567", "8910")
                    it.args
                        .single()
                        .shouldBeInstanceOf<List<Message>>()
                        .single()
                        .constraintId shouldBe "kova.charSequence.length"
                }
            }
        }

        context("ensureHas") {
            test("success") {
                val result = tryValidate { listOf("foo", "bar").ensureHas("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("bar", "baz").ensureHas("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.contains"
            }

            test("failure with ensureEmpty list") {
                val result = tryValidate { emptyList<Nothing>().ensureHas("foo") }
                result.shouldBeFailure()
            }
        }

        context("ensureContains") {
            test("success") {
                val result = tryValidate { listOf("foo", "bar").ensureContains("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("bar", "baz").ensureContains("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.contains"
            }

            test("failure with ensureEmpty list") {
                val result = tryValidate { emptyList<Nothing>().ensureContains("foo") }
                result.shouldBeFailure()
            }
        }

        context("ensureNotContains") {
            test("success") {
                val result = tryValidate { listOf("bar", "baz").ensureNotContains("foo") }
                result.shouldBeSuccess()
            }

            test("failure") {
                val result = tryValidate { listOf("foo", "bar").ensureNotContains("foo") }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].constraintId shouldBe "kova.iterable.notContains"
            }

            test("success with ensureEmpty list") {
                val result = tryValidate { emptyList<Nothing>().ensureNotContains("foo") }
                result.shouldBeSuccess()
            }
        }

        context("property") {
            data class ListHolder(
                val list: List<String>,
            )

            context(_: Validation)
            fun validate(holder: ListHolder) = holder.schema { holder::list { e -> e.ensureEach { it.ensureLength(3) } } }

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
