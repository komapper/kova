package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class MessagePropertiesJaTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.JAPAN)
        }

        context("kova.charSequence") {
            test("ensureMinLength") {
                val result = tryValidate { "abc".ensureMinLength(5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "5文字以上である必要があります"
            }
        }

        context("kova.collection") {
            test("ensureMinSize") {
                val result = tryValidate { listOf("a", "b").ensureMinSize(3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "コレクション(サイズ2)は少なくとも3個の要素を持つ必要があります"
            }
        }

        context("kova.comparable") {
            test("ensureInRange") {
                val result = tryValidate { 5.ensureInOpenEndRange(1..<5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "1..4の範囲内である必要があります"
            }
        }
    })
