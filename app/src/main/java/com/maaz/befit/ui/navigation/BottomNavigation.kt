package com.maaz.befit.ui.navigation

import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maaz.befit.ui.theme.BeFitTheme
import com.maaz.befit.ui.theme.HealthBlue
import com.maaz.befit.ui.theme.HealthGreen
import com.maaz.befit.ui.theme.HealthOrange
import com.maaz.befit.ui.theme.HealthPurple
import com.maaz.befit.ui.theme.HealthTeal

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val color: Color
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home, Icons.Default.Home, HealthBlue)
    object Water : Screen("water", "Water", Icons.Default.LocalDrink, Icons.Default.WaterDrop, HealthTeal)
    object Steps : Screen("steps", "Steps", Icons.Default.DirectionsWalk, Icons.Default.DirectionsRun, HealthGreen)
    object Sleep : Screen("sleep", "Sleep", Icons.Default.Bedtime, Icons.Default.Bedtime, HealthPurple)
    object Reports : Screen("reports", "Reports", Icons.Default.Analytics, Icons.Default.BarChart, HealthOrange)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings, Icons.Default.Settings, Color.Gray)
}

@Composable
fun BottomNavigationBar(
    currentRoute : String?,
    onNavigate : (String) -> Unit // StateHosting - This function is called when a navigation item is clicked, and it takes a Screen object as a parameter
) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Water,
        Screen.Steps,
        Screen.Sleep,
        Screen.Reports,
        Screen.Settings
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(
                // Edge-to-Edge UI! Naye Android phones ke niche navigation bar/buttons hote hain, ye padding add karta hai taaki bottom bar Android ke system buttons ke niche na chhupe.
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // Top edges par halka sa soft shadow/elevation deta hai. for navigation bottom bar
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip( // round corners
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .background(Color.White)
    ) {
        Row( // space divides evenly among the items in the row, and centers them vertically
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route // check current route is same as selected route then its selected

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(screen.route) }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        // apply selected icon if available, else use default icon
                        imageVector = if (isSelected && screen.selectedIcon != null) screen.selectedIcon else screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) screen.color else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp)) // Icon aur Text ke beech me 4dp ka gap banane ke liye.

                    Text(
                        text = screen.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) screen.color else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                        // maxLines = 1 & overflow = TextOverflow.Ellipsis: Agar kisi screen ka naam lamba ho,
                        // toh UI break nahi hoga; text 1 line me rahega aur strict case me ... ban jayega.
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    BeFitTheme {
        BottomNavigationBar(
            currentRoute = Screen.Dashboard.route,
            onNavigate = {}
        )
    }
}