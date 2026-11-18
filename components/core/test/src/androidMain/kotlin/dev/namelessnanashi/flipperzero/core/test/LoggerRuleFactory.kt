package dev.namelessnanashi.flipperzero.core.test

actual object LoggerRuleFactory {
    actual fun create(): LoggerRule {
        return TimberRule()
    }
}
