package com.wael.astimal.pos.core.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.NotifierManager.Listener
import com.mmk.kmpnotifier.notification.PayloadData
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.NavigationEvent
import com.wael.astimal.pos.core.base.ObserveEffect
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.getString
import com.wael.astimal.pos.core.data.SyncManager
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.domain.navigation.isTopLevelRoute
import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.features.user.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pos.app.generated.resources.Res
import pos.app.generated.resources.back_online
import pos.app.generated.resources.no_internet


@Composable
private fun rememberDelayedDestination(navController: NavController): State<NavBackStackEntry?> {
    val immediateDestination by navController.currentBackStackEntryAsState()
    val delayedDestination = remember { mutableStateOf(immediateDestination) }

    LaunchedEffect(immediateDestination) {
        delayedDestination.value = immediateDestination
    }
    return delayedDestination
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    startDestination: Destination = Destination.Main
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val snackbarController: SnackbarController = koinInject()


    val syncManager: SyncManager = koinInject()
    val connectivity: Connectivity = koinInject()
    val state by connectivity.statusUpdates.collectAsStateWithLifecycle(
        Connectivity.Status.Connected(
            connectionType = Connectivity.ConnectionType.Unknown
        )
    )

    var isReconnected by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state.isDisconnected) {
            snackbarController.sendEvent(
                SnackbarEvent(message = StringResource.FromResource(Res.string.no_internet))
            )
            isReconnected = true
        }

        if (isReconnected && state.isConnected) {
            snackbarController.sendEvent(
                SnackbarEvent(message = StringResource.FromResource(Res.string.back_online))
            )
            syncManager.requestSync()
        }
    }


    ObserveEffect(snackbarController.events, snackbarHostState) { event ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                getString(event.message),
                event.action?.name?.let {
                    getString(it)
                },
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }

    LaunchedEffect(Unit) {
        initializeNotifications()
    }

    val navigationController: NavigationController = koinInject()
    ObserveEffect(navigationController.events, navController) { event ->
        when (event) {
            is NavigationEvent.NavigateTo -> {
                navController.navigate(event.destination) {
                    event.popUpToRoute?.let {
                        popUpTo(it) {
                            this.inclusive = event.inclusive
                        }
                    }
                }
            }

            is NavigationEvent.NavigateBack -> {
                navController.popBackStack()
            }
        }
    }


    val navBackStackEntry by rememberDelayedDestination(navController)
    val currentDestination = navBackStackEntry?.destination


    val isOnTopLevelRoute = isTopLevelRoute(currentDestination)

    NavigationSuiteScaffold(
        layoutType = if (isOnTopLevelRoute) {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                currentWindowAdaptiveInfo()
            )
        } else {
            NavigationSuiteType.None
        },
        navigationSuiteItems = {
            mainNavigationItems(
                onDestinationSelected = { destination ->
                    navController.navigate(destination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                navBackStackEntry = navBackStackEntry,
            )
        },
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { it ->
            AppNavHost(
                startDestination = startDestination,
                navController = navController,
                modifier = Modifier.padding(it)
            )
        }
    }
}

object AppKoinComponent : KoinComponent {
    val notificationRepository: NotificationRepository by inject()
    val snackbarController: SnackbarController by inject()
}

/**
 * Initializes KMPNotifier and sets up listeners for tokens and messages.
 */
fun initializeNotifications() {
    // Initialize Napier for logging if you haven't already
//    Napier.base(DebugAntilog())


    // Create a coroutine scope for background tasks
    val scope = CoroutineScope(Dispatchers.Default)

    // Listen for new push notification tokens and save them to Supabase
    NotifierManager.addListener(
        object : Listener {
            override fun onNewToken(token: String) {
                super.onNewToken(token)
                println("New FCM token received: $token")
                scope.launch {
                    AppKoinComponent.notificationRepository.saveFcmToken(token)
                        .onFailure {
                            println("Failed to save FCM token")
                        }
                }
            }

            override fun onPushNotificationWithPayloadData(
                title: String?,
                body: String?,
                data: PayloadData
            ) {
                super.onPushNotificationWithPayloadData(title, body, data)
                println("Received push notification with payload data: $data")
                scope.launch {
                    // Process the payload data as needed
                    val message = body ?: "You have a new notification"
                    AppKoinComponent.snackbarController.sendEvent(
                        SnackbarEvent(StringResource.Dynamic(message))
                    )
                }
            }
        }
    )
}