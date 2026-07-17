sed -i 's/Slate700/Slate900/g' app/src/main/java/com/example/ui/screens/ProfileScreen.kt
sed -i 's/Slate500/Slate600/g' app/src/main/java/com/example/ui/screens/ProfileScreen.kt
sed -i 's/CategoryColors\[category\]/getCategoryColor(category)/g' app/src/main/java/com/example/ui/screens/ProfileScreen.kt
cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/screens/ProfileScreen.kt

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food" -> Color(0xFFF97316)
        "Transport" -> Color(0xFF38BDF8)
        "Shopping" -> Color(0xFF8B5CF6)
        "Bills" -> Color(0xFFF43F5E)
        "Entertainment" -> Color(0xFF10B981)
        else -> Color(0xFF94A3B8)
    }
}
INNER_EOF
