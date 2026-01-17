package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

class MessagePropertiesSyncTest :
    FunSpec({

        test("English and Japanese property files should have the same keys") {
            val englishProps = Properties()
            val japaneseProps = Properties()

            MessagePropertiesSyncTest::class.java
                .getResourceAsStream("/kova.properties")
                ?.use { englishProps.load(it) }
                ?: error("Failed to load kova.properties")

            MessagePropertiesSyncTest::class.java
                .getResourceAsStream("/kova_ja.properties")
                ?.use { japaneseProps.load(it) }
                ?: error("Failed to load kova_ja.properties")

            val englishKeys = englishProps.stringPropertyNames()
            val japaneseKeys = japaneseProps.stringPropertyNames()

            val missingInJapanese = englishKeys - japaneseKeys
            val missingInEnglish = japaneseKeys - englishKeys

            if (missingInJapanese.isNotEmpty() || missingInEnglish.isNotEmpty()) {
                val message =
                    buildString {
                        if (missingInJapanese.isNotEmpty()) {
                            appendLine("Keys missing in kova_ja.properties: $missingInJapanese")
                        }
                        if (missingInEnglish.isNotEmpty()) {
                            appendLine("Keys missing in kova.properties: $missingInEnglish")
                        }
                    }
                error(message)
            }

            englishKeys shouldBe japaneseKeys
        }
    })
