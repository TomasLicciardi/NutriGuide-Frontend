package com.tesis.nutriguideapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tesis.nutriguideapp.model.TriggerIngredient

/**
 * Chips compactos con los ingredientes que dispararon una restricción.
 * Cada chip muestra SOLO el nombre; al tocarlo se abre un modal con la
 * explicación completa (ver [TriggerExplanationDialog]).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TriggerIngredientChips(
    triggers: List<TriggerIngredient>,
    accentColor: Color = Color(0xFFFF9800),
    onTriggerClick: (TriggerIngredient) -> Unit
) {
    FlowRow(modifier = Modifier.fillMaxWidth()) {
        triggers.forEach { trigger ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .padding(end = 6.dp, top = 6.dp)
                    .clickable { onTriggerClick(trigger) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = trigger.name,
                        fontSize = 12.sp,
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Ver explicación de ${trigger.name}",
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modal con la explicación de un ingrediente trigger.
 * Renderizar siempre; no muestra nada si [trigger] es null.
 */
@Composable
fun TriggerExplanationDialog(
    trigger: TriggerIngredient?,
    onDismiss: () -> Unit
) {
    if (trigger == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFFF9800)
            )
        },
        title = {
            Text(
                text = trigger.name.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = trigger.explanation.ifBlank {
                        "Este ingrediente no es compatible con tus restricciones alimenticias."
                    },
                    fontSize = 14.sp
                )
                if (trigger.allergen.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Relacionado con: ${trigger.allergen}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}
