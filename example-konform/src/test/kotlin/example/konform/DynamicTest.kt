package example.konform

import io.konform.validation.constraints.pattern
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureMatches
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import io.konform.validation.Validation as KonformValidation

class DynamicTest :
    FunSpec({
        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        data class Address(
            val countryCode: String,
            val postalCode: String,
        )

        val us = Address("US", "12345")
        val de = Address("DE", "ABC")

        context("konform") {
            // See https://www.konform.io/

            val validateAddress =
                KonformValidation {
                    dynamic { address ->
                        Address::postalCode {
                            when (address.countryCode) {
                                "US" -> pattern("[0-9]{5}")
                                else -> pattern("[A-Z]+")
                            }
                        }
                    }
                }

            test("valid - us") {
                val result = validateAddress(us)
                result.isValid shouldBe true
            }

            test("valid - de") {
                val result = validateAddress(de)
                result.isValid shouldBe true
            }
        }

        context("kova") {

            context(_: Validation)
            fun validate(address: Address) =
                address.schema {
                    address::postalCode {
                        when (address.countryCode) {
                            "US" -> ensureMatches(it, Regex("[0-9]{5}"))
                            else -> ensureMatches(it, Regex("[A-Z]+"))
                        }
                    }
                }

            test("valid - us") {
                val result = tryValidate { validate(us) }
                result.shouldBeSuccess()
            }

            test("valid - de") {
                val result = tryValidate { validate(de) }
                result.shouldBeSuccess()
            }
        }
    })
