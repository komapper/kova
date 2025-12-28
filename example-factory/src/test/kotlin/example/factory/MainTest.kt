package example.factory

import io.kotest.core.spec.style.FunSpec

class MainTest :
    FunSpec({

        context("main function execution") {
            test("main runs without errors") {
                // This test verifies that the main function executes successfully
                // The main function uses error() for unexpected branches,
                // so if it completes without throwing, all validations work as expected
                main()
            }
        }
    })
