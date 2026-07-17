package com.example.data.repository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.user.UserUpdateBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
suspend fun test(supabaseClient: SupabaseClient) {
    supabaseClient.auth.updateUser {
        data = buildJsonObject {
            put("full_name", "test")
        }
    }
}
