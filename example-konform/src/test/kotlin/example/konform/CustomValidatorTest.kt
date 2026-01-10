package example.konform

import io.konform.validation.path.ValidationPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureLengthAtLeast
import org.komapper.extension.validator.ensureNotBlank
import org.komapper.extension.validator.ensureNotContains
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.text
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import io.konform.validation.Validation as KonformValidation

class CustomValidatorTest :
    FunSpec({
        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        data class UserProfile(
            val fullName: String,
            val age: Int?,
        )

        val invalidUserProfile = UserProfile(" \t ", 20)

        context("konform") {
            // See https://www.konform.io/

            val validateUser =
                KonformValidation {
                    UserProfile::fullName {
                        constrain("Name cannot contain a tab") { !it.contains("\t") }
                        constrain("Name must have a non-whitespace character", path = ValidationPath.of("trimmedName")) {
                            it.trim().isNotEmpty()
                        }
                        constrain("Must have 5 characters", userContext = Severity.ERROR) {
                            it.length >= 5
                        }
                    }
                }

            test("invalid") {
                val result = validateUser(invalidUserProfile)
                result.isValid shouldBe false
                result.errors.size shouldBe 3
                result.errors[0].let {
                    it.message shouldBe "Name cannot contain a tab"
                    it.dataPath shouldBe ".fullName"
                }
                result.errors[1].let {
                    it.message shouldBe "Name must have a non-whitespace character"
                    it.dataPath shouldBe ".fullName.trimmedName"
                }
                result.errors[2].let {
                    it.message shouldBe "Must have 5 characters"
                    it.dataPath shouldBe ".fullName"
                    it.userContext shouldBe Severity.ERROR
                }
            }
        }

        context("kova") {
            context(_: Validation)
            fun UserProfile.validate() =
                schema {
                    ::fullName {
                        it.ensureNotContains("\t") { text("Name cannot contain a tab") }
                        it.ensureNotBlank { text("Name must have a non-whitespace character") }
                        it.ensureLengthAtLeast(5) { text("Must have 5 characters") }
                    }
                }

            test("invalid") {
                val result = tryValidate { invalidUserProfile.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 3
                result.messages[0].let {
                    it.text shouldBe "Name cannot contain a tab"
                    it.root shouldBe "UserProfile"
                    it.path.fullName shouldBe "fullName"
                }
                result.messages[1].let {
                    it.text shouldBe "Name must have a non-whitespace character"
                    it.root shouldBe "UserProfile"
                    it.path.fullName shouldBe "fullName"
                }
                result.messages[2].let {
                    it.text shouldBe "Must have 5 characters"
                    it.root shouldBe "UserProfile"
                    it.path.fullName shouldBe "fullName"
                }
            }
        }
    }) {
    enum class Severity { WARNING, ERROR }
}
