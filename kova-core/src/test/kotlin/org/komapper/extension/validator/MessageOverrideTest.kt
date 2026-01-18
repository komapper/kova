package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import java.util.Locale

/**
 * Tests for the message override mechanism.
 *
 * The override mechanism allows users to create a `kova.properties` file in their
 * project's resources to override specific message keys from `kova-default.properties`.
 *
 * To avoid affecting other tests, this test uses French locale (kova_fr.properties)
 * which contains overrides for:
 * - kova.charSequence.notBlank
 * - kova.number.positive
 */
class MessageOverrideTest :
    FunSpec({

        context("user override with French locale") {

            beforeTest {
                Locale.setDefault(Locale.FRENCH)
            }

            afterTest {
                Locale.setDefault(Locale.US)
            }

            test("uses user message when key exists in kova_fr.properties") {
                // kova.charSequence.notBlank is overridden in test kova_fr.properties
                val result = tryValidate { "  ".ensureNotBlank() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Ce champ est requis"
            }

            test("uses user message for number.positive") {
                // kova.number.positive is overridden in test kova_fr.properties
                val result = tryValidate { (-5).ensurePositive() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "Veuillez entrer un nombre positif"
            }

            test("falls back to kova-default.properties when key not in user file") {
                // kova.number.negative is NOT overridden, should fall back to default (English)
                val result = tryValidate { 5.ensureNegative() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be negative"
            }

            test("falls back to default for non-overridden keys") {
                // kova.comparable.atLeast is NOT overridden
                val result = tryValidate { 5.ensureAtLeast(10) }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be greater than or equal to 10"
            }
        }

        context("default messages without user override") {

            beforeTest {
                Locale.setDefault(Locale.US)
            }

            test("uses default English message when no user override exists") {
                val result = tryValidate { "  ".ensureNotBlank() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must not be blank"
            }

            test("uses default English message for number.positive") {
                val result = tryValidate { (-5).ensurePositive() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "must be positive"
            }
        }

        context("default messages with Japanese locale") {

            beforeTest {
                Locale.setDefault(Locale.JAPANESE)
            }

            afterTest {
                Locale.setDefault(Locale.US)
            }

            test("uses default Japanese message when no user override exists") {
                val result = tryValidate { "  ".ensureNotBlank() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "空白であってはいけません"
            }

            test("uses default Japanese message for number.positive") {
                val result = tryValidate { (-5).ensurePositive() }
                result.shouldBeFailure()
                val message = result.messages.single()
                message.text shouldBe "正の数である必要があります"
            }
        }
    })
