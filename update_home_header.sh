cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/HomeScreen.kt.diff
--- app/src/main/java/com/example/ui/screens/HomeScreen.kt
+++ app/src/main/java/com/example/ui/screens/HomeScreen.kt
@@ -237,13 +237,18 @@
 }
 
 @Composable
-fun HomeHeader(onNavigateToProfile: () -> Unit) {
+fun HomeHeader(onNavigateToProfile: () -> Unit, authViewModel: com.example.viewmodel.AuthViewModel? = null) {
+    val currentUser by (authViewModel?.currentUser ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
+    val name = currentUser?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "Guest User"
+    
     Row(
         modifier = Modifier.fillMaxWidth(),
         horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.CenterVertically
     ) {
         Column {
             Text(
                 text = "Good Morning,",
                 fontSize = 14.sp,
                 color = Slate400
             )
             Text(
-                text = "Guest User",
+                text = name,
                 fontSize = 20.sp,
                 fontWeight = FontWeight.Bold,
                 color = Slate900
INNER_EOF
patch -p0 < app/src/main/java/com/example/ui/screens/HomeScreen.kt.diff
