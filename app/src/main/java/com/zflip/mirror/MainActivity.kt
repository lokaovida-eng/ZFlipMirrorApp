package com.zflip.mirror

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (_: Exception) {}

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E17)
                ) {
                    ZFlipMirrorScreen()
                }
            }
        }
    }
}

@Composable
fun ZFlipMirrorScreen() {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var isMirroringActive by remember { mutableStateOf(false) }

    fun triggerHaptic() {
        try {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }

    // Gerenciador de Displays para detectar a Tela de Capa do Galaxy Z Flip
    val displayManager = remember {
        context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    }

    val displays = remember {
        displayManager?.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION) ?: emptyArray()
    }

    val hasCoverDisplay = displays.isNotEmpty()

    val buttonColor by animateColorAsState(
        targetValue = if (isMirroringActive) Color(0xFF00FF66) else Color(0xFF00F0FF),
        animationSpec = tween(durationMillis = 300),
        label = "ButtonColorAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabeçalho Principal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "📱 ZFLIP COVER MIRROR",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00F0FF),
                letterSpacing = 2.sp
            )

            Text(
                text = "Espelhamento Direto da Tela Principal na Tela de Capa",
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // BOTAO PRINCIPAL DE UNICA OPCAO: ESPELHAR TELA NA CAPA
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                buttonColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(3.dp, buttonColor, CircleShape)
                    .clickable {
                        triggerHaptic()
                        isMirroringActive = !isMirroringActive

                        if (isMirroringActive) {
                            CoverMirrorService.startMirroring(context)
                        } else {
                            CoverMirrorService.stopMirroring(context)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isMirroringActive) "⏸" else "🖥️",
                        fontSize = 48.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isMirroringActive) "PARAR\nESPELHAMENTO" else "ESPELHAR\nTELA NA CAPA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador de Status
            Surface(
                color = if (isMirroringActive) Color(0xFF00FF66).copy(alpha = 0.15f) else Color(0xFF00F0FF).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMirroringActive) Color(0xFF00FF66) else Color(0xFF00F0FF)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isMirroringActive) Color(0xFF00FF66) else Color(0xFF00F0FF))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isMirroringActive) "ESPELHAMENTO ATIVO NA CAPA" else "PRONTO PARA ESPELHAR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Rodapé de Informações Técnicas e Apoio
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = if (hasCoverDisplay) "✓ Tela de Capa Z Flip Detectada" else "ℹ️ Compatível com Galaxy Z Flip 5 / 6 / 7",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    triggerHaptic()
                    try {
                        val paypalUrl = "https://www.paypal.com/donate/?hosted_button_id=REPB3R2VW4AVA"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paypalUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC439)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "Apoiar Criador via PayPal",
                    color = Color(0xFF003087),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
