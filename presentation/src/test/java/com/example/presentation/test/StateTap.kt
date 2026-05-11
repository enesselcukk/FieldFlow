package com.example.presentation.test

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.yield

internal suspend fun TestScope.tapState(flow: StateFlow<*>) {
    backgroundScope.launch { flow.collect { } }
    yield()
}
