package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

class MessagePropertiesJaTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.JAPAN)
        }

        context("kova.charSequence") {
            test("minLength") {
                val result = tryValidate { minLength("abc", 5) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "5文字以上である必要があります"
            }
        }

        context("kova.collection") {
            test("minSize") {
                val result = tryValidate { minSize(listOf("a", "b"), 3) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "コレクション(サイズ2)は少なくとも3個の要素を持つ必要があります"
            }
        }
    })
