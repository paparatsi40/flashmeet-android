// In com.carlitoswy.flashmeet.domain.model/UserProfile.kt

package com.carlitoswy.flashmeet.domain.model

data class UserProfile(
    val id: String = "",
    val displayName: String?, // <-- Made nullable (String?)
    val email: String?,     // <-- Made nullable (String?)
    val photoUrl: String?,    // <-- Made nullable (String?)
    val interests: List<String> = emptyList(),
    val reputation: Int = 0,
    val uid: String,
    val name: String? // <-- Also need to address 'name' - made nullable or give default
)
