package com.rp_elderycareapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.ui.theme.AppTypography
import com.rp_elderycareapp.ui.theme.ThemeConfig

/**
 * Quick action button with solid color backgrounds
 * Large rounded buttons with high accessibility
 */
@Composable
fun QuickActionButton(
    text: String,
    icon: Any? = null, // Can be ImageVector or Painter
    backgroundColor: Color = AppColors.Primary,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .height(ThemeConfig.ButtonDimensions.Height)
            .background(
                color = if (isPressed) backgroundColor.copy(alpha = 0.8f) else backgroundColor,
                shape = RoundedCornerShape(ThemeConfig.ButtonDimensions.CornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .graphicsLayer(
                scaleX = if (isPressed) 0.98f else 1f,
                scaleY = if (isPressed) 0.98f else 1f
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                when (icon) {
                    is ImageVector -> Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    is Painter -> Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = text,
                style = AppTypography.ButtonText.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Secondary action button (outlined style)
 * For less prominent actions
 */
@Composable
fun SecondaryActionButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .height(ThemeConfig.ButtonDimensions.Height)
            .background(
                color = AppColors.CardBackground,
                shape = RoundedCornerShape(ThemeConfig.ButtonDimensions.CornerRadius)
            )
            .border(
                width = 2.dp,
                color = if (isPressed) AppColors.Primary else AppColors.LightBlue,
                shape = RoundedCornerShape(ThemeConfig.ButtonDimensions.CornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .graphicsLayer(
                scaleX = if (isPressed) 0.98f else 1f,
                scaleY = if (isPressed) 0.98f else 1f
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    style = AppTypography.ButtonText.copy(
                        color = AppColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = text,
                style = AppTypography.ButtonText.copy(
                    color = AppColors.Primary,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Outlined action button with white background and dark blue border
 */
@Composable
fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .height(ThemeConfig.ButtonDimensions.Height)
            .background(
                color = if (isPressed) Color.White.copy(alpha = 0.9f) else Color.White,
                shape = RoundedCornerShape(ThemeConfig.ButtonDimensions.CornerRadius)
            )
            .border(
                width = 2.dp,
                color = AppColors.DeepBlue,
                shape = RoundedCornerShape(ThemeConfig.ButtonDimensions.CornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .graphicsLayer(
                scaleX = if (isPressed) 0.98f else 1f,
                scaleY = if (isPressed) 0.98f else 1f
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTypography.ButtonText.copy(
                color = AppColors.DeepBlue,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Quick actions section with buttons
 * Displays action buttons
 */
@Composable
fun QuickActionsSection(
    onStartChat: () -> Unit = {},
    onPlayGames: () -> Unit = {},
    onViewProgress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ThemeConfig.Padding.Screen)
            .padding(vertical = ThemeConfig.Padding.Section),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Start Chat - White background with dark blue border
        OutlinedActionButton(
            text = "Chat with Hale",
            onClick = onStartChat,
            modifier = Modifier.fillMaxWidth()
        )

        // Settings Button - Gray background
        SettingsButton(
            onClick = onViewProgress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
expect fun SettingsButton(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
)

/**
 * Compact quick actions (2 buttons in a row)
 */
@Composable
fun CompactQuickActionsSection(
    onStartChat: () -> Unit = {},
    onPlayGames: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ThemeConfig.Padding.Screen)
            .padding(vertical = ThemeConfig.Padding.Section),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            text = "Chat with Hale",
            backgroundColor = AppColors.Primary,
            onClick = onStartChat,
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            text = "Play Games",
            backgroundColor = AppColors.LightBlue,
            onClick = onPlayGames,
            modifier = Modifier.weight(1f)
        )
    }
}
