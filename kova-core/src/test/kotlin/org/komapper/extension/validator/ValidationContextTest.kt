package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidationContextTest :
    FunSpec({

        context("addRoot") {
            test("no root") {
                val context = ValidationContext(path = "c")
                val newContext = context.addRoot(root = "a")
                newContext shouldBe ValidationContext("a", "c")
            }

            test("already has root") {
                val context = ValidationContext(root = "a", path = "c")
                val newContext = context.addRoot("b")
                newContext shouldBe ValidationContext("a", "c")
            }

            test("add empty") {
                val context = ValidationContext(path = "c")
                val newContext = context.addRoot(root = "")
                newContext shouldBe ValidationContext("", "c")
            }
        }

        context("addPath") {
            test("no path") {
                val context = ValidationContext("a")
                val newContext = context.addPath("b")
                newContext shouldBe ValidationContext("a", "b")
            }

            test("already has path") {
                val context = ValidationContext("a", "b")
                val newContext = context.addPath("c")
                newContext shouldBe ValidationContext("a", "b.c")
            }

            test("add empty") {
                val context = ValidationContext("a")
                val newContext = context.addPath("")
                newContext shouldBe ValidationContext("a", "")
            }
        }

        context("appendPath") {
            test("no path") {
                val context = ValidationContext("a")
                val newContext = context.appendPath("b")
                newContext shouldBe ValidationContext("a", "b")
            }

            test("already has path") {
                val context = ValidationContext("a", "b")
                val newContext = context.appendPath("c")
                newContext shouldBe ValidationContext("a", "bc")
            }

            test("add empty") {
                val context = ValidationContext("a")
                val newContext = context.appendPath("")
                newContext shouldBe ValidationContext("a", "")
            }
        }
    })
