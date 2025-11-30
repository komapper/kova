package org.komapper.extension.validator.pbt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.isSuccess
import org.komapper.extension.validator.tryValidate
import org.komapper.extension.validator.whenNotNullThen

class KovaNullableTest :
    FunSpec({

        test("whenNotNullThen") {
            checkAll(Arb.Companion.string().orNull(), Arb.Companion.int(-10..100)) { input, length ->
                val result =
                    Kova
                        .nullable<String>()
                        .whenNotNullThen(Kova.string().min(length))
                        .tryValidate(input)
                        .isSuccess()
                if (input == null || input.length >= length) {
                    result.shouldBeTrue()
                } else {
                    result.shouldBeFalse()
                }
            }
        }
    })
