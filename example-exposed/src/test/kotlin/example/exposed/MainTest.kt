package example.exposed

import io.kotest.core.spec.style.FunSpec

class MainTest :
    FunSpec({
        context("main function execution") {
            test("main runs without errors") {
                main()
            }
        }
    })
