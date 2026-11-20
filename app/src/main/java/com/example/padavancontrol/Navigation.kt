package com.example.padavancontrol

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.padavancontrol.data.CredentialStore
import com.example.padavancontrol.data.DefaultPadavanRepository
import com.example.padavancontrol.data.SettingsDataStore
import com.example.padavancontrol.ui.components.BreadcrumbBar
import androidx.compose.foundation.layout.Column
import com.example.padavancontrol.ui.screens.DashboardScreen
import com.example.padavancontrol.ui.screens.DevicesScreen
import com.example.padavancontrol.ui.screens.LoginScreen
import com.example.padavancontrol.ui.screens.SettingsScreen
import com.example.padavancontrol.ui.screens.TrafficScreen
import com.example.padavancontrol.ui.screens.ShellConsoleScreen
import com.example.padavancontrol.ui.screens.LogViewerScreen
import com.example.padavancontrol.ui.screens.AdvancedSidebarContent
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch
import com.example.padavancontrol.ui.screens.AdvancedSettingFormPlaceholderScreen
import com.example.padavancontrol.ui.screens.AdvancedWirelessScreen
import com.example.padavancontrol.ui.screens.AdvancedLanScreen
import com.example.padavancontrol.ui.screens.AdvancedWanScreen
import com.example.padavancontrol.ui.screens.AdvancedFirewallScreen
import com.example.padavancontrol.ui.screens.AdvancedUsbScreen
import com.example.padavancontrol.ui.screens.AdvancedAdminScreen
import com.example.padavancontrol.ui.screens.AdvancedScriptScreen
import com.example.padavancontrol.ui.screens.AdvancedVpnScreen
import com.example.padavancontrol.ui.screens.TtydWebShellScreen
import com.example.padavancontrol.ui.viewmodels.DashboardViewModel
import com.example.padavancontrol.ui.viewmodels.LoginViewModel
import com.example.padavancontrol.ui.viewmodels.SettingsViewModel
import com.example.padavancontrol.ui.viewmodels.DevicesViewModel
import com.example.padavancontrol.ui.viewmodels.TrafficViewModel
import com.example.padavancontrol.ui.viewmodels.ShellConsoleViewModel
import com.example.padavancontrol.ui.viewmodels.LogViewerViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedWirelessViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedLanViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedWanViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedFirewallViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedUsbViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedAdminViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedScriptViewModel
import com.example.padavancontrol.ui.viewmodels.AdvancedVpnViewModel
import com.example.padavancontrol.ui.viewmodels.HardwareInfoViewModel
import com.example.padavancontrol.ui.screens.HardwareInfoScreen
import com.example.padavancontrol.data.t
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.padavancontrol.ui.screens.SplashLoadingScreen
import com.example.padavancontrol.network.RetrofitClient

import com.example.padavancontrol.ui.viewmodels.AppViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

sealed interface AppInitState {
    object Loading : AppInitState
    data class Success(
        val credentialStore: CredentialStore,
        val repository: DefaultPadavanRepository,
        val settingsDataStore: SettingsDataStore
    ) : AppInitState
}

/**
 * Lazily resolves activity-scoped ViewModels. This keeps every ViewModel
 * available to its screen while avoiding construction of all 16 of them at
 * app startup.
 */
private class AppViewModels(provider: ViewModelProvider) {
    val login: LoginViewModel by lazy { provider[LoginViewModel::class.java] }
    val dashboard: DashboardViewModel by lazy { provider[DashboardViewModel::class.java] }
    val settings: SettingsViewModel by lazy { provider[SettingsViewModel::class.java] }
    val devices: DevicesViewModel by lazy { provider[DevicesViewModel::class.java] }
    val traffic: TrafficViewModel by lazy { provider[TrafficViewModel::class.java] }
    val shellConsole: ShellConsoleViewModel by lazy { provider[ShellConsoleViewModel::class.java] }
    val logViewer: LogViewerViewModel by lazy { provider[LogViewerViewModel::class.java] }
    val hardwareInfo: HardwareInfoViewModel by lazy { provider[HardwareInfoViewModel::class.java] }
    val advancedWireless: AdvancedWirelessViewModel by lazy { provider[AdvancedWirelessViewModel::class.java] }
    val advancedLan: AdvancedLanViewModel by lazy { provider[AdvancedLanViewModel::class.java] }
    val advancedWan: AdvancedWanViewModel by lazy { provider[AdvancedWanViewModel::class.java] }
    val advancedFirewall: AdvancedFirewallViewModel by lazy { provider[AdvancedFirewallViewModel::class.java] }
    val advancedUsb: AdvancedUsbViewModel by lazy { provider[AdvancedUsbViewModel::class.java] }
    val advancedAdmin: AdvancedAdminViewModel by lazy { provider[AdvancedAdminViewModel::class.java] }
    val advancedScript: AdvancedScriptViewModel by lazy { provider[AdvancedScriptViewModel::class.java] }
    val advancedVpn: AdvancedVpnViewModel by lazy { provider[AdvancedVpnViewModel::class.java] }
}

@Composable
fun MainNavigation() {
    val context = LocalContext.current.applicationContext
    var initState by remember { mutableStateOf<AppInitState>(AppInitState.Loading) }

    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            val credentialStore = CredentialStore(context)
            val repository = DefaultPadavanRepository(credentialStore)
            val settingsDataStore = SettingsDataStore(context)
            initState = AppInitState.Success(credentialStore, repository, settingsDataStore)
        }
    }

    when (val state = initState) {
        is AppInitState.Loading -> {
            SplashLoadingScreen()
        }
        is AppInitState.Success -> {
            MainNavigationContent(
                credentialStore = state.credentialStore,
                repository = state.repository,
                settingsDataStore = state.settingsDataStore
            )
        }
    }
}

@Composable
fun MainNavigationContent(
    credentialStore: CredentialStore,
    repository: DefaultPadavanRepository,
    settingsDataStore: SettingsDataStore
) {
    val factory = remember(repository, credentialStore, settingsDataStore) {
        AppViewModelFactory(repository, credentialStore, settingsDataStore)
    }

    DisposableEffect(repository) {
        onDispose {
            repository.cancelActiveWork()
        }
    }

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner available for Padavan Control"
    }
    val viewModelProvider = remember(viewModelStoreOwner, factory) {
        ViewModelProvider(viewModelStoreOwner, factory)
    }
    val vms = remember(viewModelProvider) { AppViewModels(viewModelProvider) }

    val startKey =
        if (credentialStore.isRememberCredentials() && credentialStore.getPassword().isNotEmpty()) {
            Dashboard
        } else {
            Login
        }

    val backStack = rememberNavBackStack(startKey)
    val currentKey = backStack.lastOrNull()

    LaunchedEffect(credentialStore, backStack) {
        RetrofitClient.unauthorizedEvents.collect {
            credentialStore.clearCredentials()
            backStack.clear()
            backStack.add(Login)
        }
    }

    val showBottomBar = currentKey == Dashboard || currentKey == Devices || currentKey == Traffic || currentKey == HardwareInfo || currentKey == Settings

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentKey) {
        drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            if (showBottomBar) {
                AdvancedSidebarContent(
                    onNavigateToPage = { pageKey ->
                        scope.launch { drawerState.close() }
                        backStack.add(pageKey)
                    }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentKey == Dashboard,
                        onClick = {
                            if (currentKey != Dashboard) {
                                backStack.clear()
                                backStack.add(Dashboard)
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = t("Dashboard")) },
                        label = { Text(t("Dashboard")) }
                    )
                    NavigationBarItem(
                        selected = currentKey == Devices,
                        onClick = {
                            if (currentKey != Devices) {
                                backStack.add(Devices)
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = t("Devices")) },
                        label = { Text(t("Devices")) }
                    )
                    NavigationBarItem(
                        selected = currentKey == Traffic,
                        onClick = {
                            if (currentKey != Traffic) {
                                backStack.add(Traffic)
                            }
                        },
                        icon = { Icon(Icons.Filled.Info, contentDescription = t("Traffic")) },
                        label = { Text(t("Traffic")) }
                    )
                    NavigationBarItem(
                        selected = currentKey == HardwareInfo,
                        onClick = {
                            if (currentKey != HardwareInfo) {
                                backStack.add(HardwareInfo)
                            }
                        },
                        icon = { Icon(Icons.Filled.Refresh, contentDescription = t("Hardware")) },
                        label = { Text(t("Hardware")) }
                    )
                    NavigationBarItem(
                        selected = currentKey == Settings,
                        onClick = {
                            if (currentKey != Settings) {
                                backStack.add(Settings)
                            }
                        },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = t("Settings")) },
                        label = { Text(t("Settings")) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            BreadcrumbBar(
                backStack = backStack,
                onNavigateToKey = { key ->
                    val idx = backStack.indexOf(key)
                    if (idx >= 0) {
                        while (backStack.size > idx + 1) {
                            backStack.removeLastOrNull()
                        }
                    }
                }
            )
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                },
                modifier = Modifier.weight(1f),
                entryProvider = androidx.navigation3.runtime.entryProvider {
                entry<Login> {
                    LoginScreen(
                        viewModel = vms.login,
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Dashboard)
                        },
                        modifier = Modifier
                    )
                }
                entry<Dashboard> {
                    DashboardScreen(
                        viewModel = vms.dashboard,
                        onNavigateToSettings = {
                            backStack.add(Settings)
                        },
                        onNavigateToShellConsole = {
                            backStack.add(ShellConsole)
                        },
                        onNavigateToLogViewer = {
                            backStack.add(LogViewer)
                        },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                    )
                }
                entry<Devices> {
                    DevicesScreen(
                        viewModel = vms.devices,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                    )
                }
                entry<Traffic> {
                    TrafficScreen(
                        viewModel = vms.traffic,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                    )
                }
                entry<HardwareInfo> {
                    HardwareInfoScreen(
                        viewModel = vms.hardwareInfo,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                    )
                }
                entry<Settings> {
                    SettingsScreen(
                        viewModel = vms.settings,
                        onLogout = {
                            credentialStore.clearCredentials()
                            backStack.clear()
                            backStack.add(Login)
                        },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                    )
                }
                entry<ShellConsole> {
                    ShellConsoleScreen(
                        viewModel = vms.shellConsole,
                        onNavigateBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },
                        modifier = Modifier
                    )
                }
                entry<LogViewer> {
                    LogViewerScreen(
                        viewModel = vms.logViewer,
                        onNavigateBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },
                        modifier = Modifier
                    )
                }
                
                // Wireless 2.4GHz
                entry<Wifi2gGeneral> { AdvancedWirelessScreen(is5GHz = false, section = "General", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi2gGuest> { AdvancedWirelessScreen(is5GHz = false, section = "Guest", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi2gBridge> { AdvancedWirelessScreen(is5GHz = false, section = "Bridge", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi2gMacFilter> { AdvancedWirelessScreen(is5GHz = false, section = "MAC Filter", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi2gRadius> { AdvancedWirelessScreen(is5GHz = false, section = "RADIUS", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi2gProfessional> { AdvancedWirelessScreen(is5GHz = false, section = "Professional", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // Wireless 5GHz
                entry<Wifi5gGeneral> { AdvancedWirelessScreen(is5GHz = true, section = "General", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi5gGuest> { AdvancedWirelessScreen(is5GHz = true, section = "Guest", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi5gBridge> { AdvancedWirelessScreen(is5GHz = true, section = "Bridge", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi5gMacFilter> { AdvancedWirelessScreen(is5GHz = true, section = "MAC Filter", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi5gRadius> { AdvancedWirelessScreen(is5GHz = true, section = "RADIUS", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<Wifi5gProfessional> { AdvancedWirelessScreen(is5GHz = true, section = "Professional", viewModel = vms.advancedWireless, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // LAN
                entry<LanIp> { AdvancedLanScreen("IP", vms.advancedLan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<LanDhcp> { AdvancedLanScreen("DHCP", vms.advancedLan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<LanRoute> { AdvancedLanScreen("Route", vms.advancedLan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<LanIptv> { AdvancedLanScreen("IPTV", vms.advancedLan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<LanSwitch> { AdvancedLanScreen("Switch", vms.advancedLan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<LanWol> { AdvancedLanScreen("WOL", vms.advancedLan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // WAN
                entry<WanConnection> { AdvancedWanScreen("Connection", vms.advancedWan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<WanIpv6> { AdvancedWanScreen("IPv6", vms.advancedWan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<WanPortForward> { AdvancedWanScreen("PortForward", vms.advancedWan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<WanDmz> { AdvancedWanScreen("DMZ", vms.advancedWan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<WanDdns> { AdvancedWanScreen("DDNS", vms.advancedWan, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // Firewall
                entry<FirewallGeneral> { AdvancedFirewallScreen("General", viewModel = vms.advancedFirewall, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<FirewallNetfilter> { AdvancedFirewallScreen("Netfilter", viewModel = vms.advancedFirewall, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<FirewallUrlFilter> { AdvancedFirewallScreen("URLFilter", viewModel = vms.advancedFirewall, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<FirewallMacFilter> { AdvancedFirewallScreen("MACFilter", viewModel = vms.advancedFirewall, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<FirewallServicesFilter> { AdvancedFirewallScreen("ServicesFilter", viewModel = vms.advancedFirewall, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<FirewallDnsIpsetFilter> { AdvancedFirewallScreen("DnsIpsetFilter", viewModel = vms.advancedFirewall, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // USB Application
                entry<UsbCommon> { AdvancedUsbScreen("Common", viewModel = vms.advancedUsb, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<UsbSamba> { AdvancedUsbScreen("Samba", viewModel = vms.advancedUsb, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<UsbFtp> { AdvancedUsbScreen("FTP", viewModel = vms.advancedUsb, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<UsbModem> { AdvancedUsbScreen("Modem", viewModel = vms.advancedUsb, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<UsbPrinter> { AdvancedUsbScreen("Printer", viewModel = vms.advancedUsb, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // Administration
                entry<AdminSystem> { AdvancedAdminScreen("System", viewModel = vms.advancedAdmin, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, settingsDataStore = settingsDataStore, modifier = Modifier) }
                entry<AdminServices> { AdvancedAdminScreen("Services", viewModel = vms.advancedAdmin, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, onNavigateToTtydWebShell = { backStack.add(TtydWebShell) }, modifier = Modifier) }
                entry<AdminOpMode> { AdvancedAdminScreen("OpMode", viewModel = vms.advancedAdmin, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<AdminFirmware> { AdvancedAdminScreen("Firmware", viewModel = vms.advancedAdmin, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<AdminSettingsBackup> { AdvancedAdminScreen("Backup", viewModel = vms.advancedAdmin, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<AdminConsole> { ShellConsoleScreen(viewModel = vms.shellConsole, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<TtydWebShell> { TtydWebShellScreen(onNavigateBack = { backStack.removeLastOrNull() }, repository = repository, modifier = Modifier) }
                entry<AdminButtonsLed> { AdvancedAdminScreen("ButtonsLed", viewModel = vms.advancedAdmin, repository = repository, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // Customization
                entry<CustomScripts> { AdvancedScriptScreen("Scripts", viewModel = vms.advancedScript, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                entry<CustomDetector> { AdvancedScriptScreen("Detector", viewModel = vms.advancedScript, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
                
                // VPN
                entry<AdvancedVpn> { AdvancedVpnScreen(viewModel = vms.advancedVpn, onNavigateBack = { backStack.removeLastOrNull() }, modifier = Modifier) }
            }
        )
    }
}
}
}
