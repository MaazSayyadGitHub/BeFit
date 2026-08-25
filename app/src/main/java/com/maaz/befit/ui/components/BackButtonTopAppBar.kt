package com.maaz.befit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.maaz.befit.ui.theme.BeFitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackButtonTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = contentColor
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        modifier = modifier
    )
}

/**
 * HEALTH THEMED BACK BUTTON TOP APP BAR
 *
 * A specialized version of the BackButtonTopAppBar with health app theming.
 * Uses the app's health color scheme for better visual consistency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthBackButtonTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    healthColor: Color = MaterialTheme.colorScheme.primary,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = healthColor
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = healthColor
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = healthColor,
            navigationIconContentColor = healthColor,
            actionIconContentColor = healthColor
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun BackButtonTopAppBarPreview() {
    BeFitTheme {
        BackButtonTopAppBar(
            title = "BackButton",
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HealthBackButtonTopAppBarPreview() {
    BeFitTheme {
        HealthBackButtonTopAppBar(
            title = "Health AppBar",
            onBackClick = {}
        )
    }
}