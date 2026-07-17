package com.example.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

object Constants {
    val CATEGORIES = listOf(
        Category("Food", Icons.Outlined.Restaurant, Orange),
        Category("Travel", Icons.Outlined.DirectionsCar, SkyBlue),
        Category("Gifts", Icons.Outlined.CardGiftcard, Coral),
        Category("Entertainment", Icons.Outlined.Movie, Purple),
        Category("Shopping", Icons.Outlined.ShoppingBag, Coral),
        Category("Tickets", Icons.Outlined.ConfirmationNumber, Purple),
        Category("Coffee", Icons.Outlined.LocalCafe, SoftYellow),
        Category("Medical", Icons.Outlined.MedicalServices, Coral),
        Category("Household", Icons.Outlined.House, EmeraldGreen),
        Category("Education", Icons.Outlined.School, SkyBlue),
        Category("Work", Icons.Outlined.WorkOutline, TextSecondaryLight),
        Category("Bills", Icons.Outlined.Receipt, Orange),
        Category("Fuel", Icons.Outlined.LocalGasStation, Coral),
        Category("EMI", Icons.Outlined.CreditCard, Purple),
        Category("Charity", Icons.Outlined.VolunteerActivism, Coral),
        Category("Gaming", Icons.Outlined.SportsEsports, Purple),
        Category("Pets", Icons.Outlined.Pets, Orange),
        Category("Online Shopping", Icons.Outlined.ShoppingCart, SkyBlue),
        Category("Vacation", Icons.Outlined.FlightTakeoff, EmeraldGreen),
        Category("Party", Icons.Outlined.Celebration, Purple),
        Category("Saloon", Icons.Outlined.ContentCut, Orange),
        Category("Other", Icons.Outlined.Category, TextSecondaryLight)
    )

    val PAYMENT_MODES = listOf(
        "Cash", "UPI", "Credit Card", "Debit Card", "Net Banking", "Wallet"
    )
    
    fun getCategoryByName(name: String): Category {
        return CATEGORIES.find { it.name == name } ?: CATEGORIES.last()
    }
}

data class Category(val name: String, val icon: ImageVector, val color: Color)
