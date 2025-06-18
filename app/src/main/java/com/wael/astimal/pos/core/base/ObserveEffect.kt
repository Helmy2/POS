package com.wael.astimal.pos.core.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Observes a [Flow] and executes [onEffect] for each emitted value.
 *
 * This Composable function uses [LaunchedEffect] and [repeatOnLifecycle] to ensure that
 * the flow collection is started when the Composable enters the `STARTED` lifecycle state
 * and is cancelled when it leaves that state.
 *
 * The [onEffect] lambda is executed on the main thread (immediate dispatcher).
 *
 * @param T The type of data emitted by the [flow].
 * @param flow The [Flow] to observe.
 * @param key1 An optional key that, if changed, will cause the effect to restart.
 * @param key2 An optional key that, if changed, will cause the effect to restart.
 * @param onEffect A suspend lambda that will be invoked with each value emitted by the [flow].
 */
@Composable
fun <T> ObserveEffect(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEffect: suspend (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle, key1, key2, flow) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEffect)
            }
        }
    }
}