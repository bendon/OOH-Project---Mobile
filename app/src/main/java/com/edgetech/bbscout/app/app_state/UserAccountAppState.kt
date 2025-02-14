package com.diracks.app.app.app_state

data class UserAccountAppState(
    val mainAccount: UserState? = null,
   // val accessedAccount: UserState? = null,
    //val settings: List<SettingsEntity>?  = null,
    val appId: String? = null
)


data class UserState(
    val name: String? = null,
    val userId: String? = null,
    val permissions: List<String>? = null,
    val token: String? = null,
)