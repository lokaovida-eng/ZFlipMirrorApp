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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

        // Impede que a tela apague automaticamente
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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

    // Estado do Botão 1: Espelhamento Direto da Tela Principal na Capa
    var isMirroringActive by remember { mutableStateOf(false) }

    // Estado do Botão 2: Manter a Tela de Capa Ligada quando o Z Flip for ABERTO
    var keepCoverOnWhenOpened by remember { mutableStateOf(false) }

    fun triggerHaptic() {
        try {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }

    val displayManager = remember {
        context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    }

    val displays = remember {
        displayManager?.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION) ?: emptyArray()
    }

    val hasCoverDisplay = displays.isNotEmpty()

    val button1Color by animateColorAsState(
        targetValue = if (isMirroringActive) Color(0xFF00FF66) else Color(0xFF00F0FF),
        animationSpec = tween(durationMillis = 300),
        label = "Button1ColorAnim"
    )

    val button2Color by animateColorAsState(
        targetValue = if (keepCoverOnWhenOpened) Color(0xFFFFD700) else Color(0xFFFF3366),
        animationSpec = tween(durationMillis = 300),
        label = "Button2ColorAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabeçalho Principal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "📱 ZFLIP COVER MIRROR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00F0FF),
                letterSpacing = 2.sp
            )

            Text(
                text = "Espelhamento Direto da Tela Grande na Tela de Capa",
                fontSize = 11.5.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OS 2 BOTÕES DE CONTROLE
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // ----------------------------------------------------
            // BOTÃO 1: ESPELHAR TELA PRINCIPAL NA CAPA
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1726))
                    .border(2.dp, button1Color, RoundedCornerShape(16.dp))
                    .clickable {
                        triggerHaptic()
                        isMirroringActive = !isMirroringActive

                        if (isMirroringActive) {
                            CoverMirrorService.startMirroring(context, keepCoverOnWhenOpened)
                        } else {
                            CoverMirrorService.stopMirroring(context)
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(button1Color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isMirroringActive) "⏸" else "🖥️",
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "1. ESPELHAR TELA NA CAPA",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (isMirroringActive) "▶ Espelhamento Ativo" else "Toque para transmitir tela principal na capa",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Switch(
                    checked = isMirroringActive,
                    onCheckedChange = {
                        triggerHaptic()
                        isMirroringActive = it
                        if (isMirroringActive) {
                            CoverMirrorService.startMirroring(context, keepCoverOnWhenOpened)
                        } else {
                            CoverMirrorService.stopMirroring(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FF66),
                        checkedTrackColor = Color(0xFF00FF66).copy(alpha = 0.3f)
                    )
                )
            }

            // ----------------------------------------------------
            // BOTÃO 2: MANTER TELA DE CAPA LIGADA QUANDO O Z FLIP FOR ABERTO
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1726))
                    .border(2.dp, button2Color, RoundedCornerShape(16.dp))
                    .clickable {
                        triggerHaptic()
                        keepCoverOnWhenOpened = !keepCoverOnWhenOpened

                        if (isMirroringActive) {
                            CoverMirrorService.startMirroring(context, keepCoverOnWhenOpened)
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(button2Color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (keepCoverOnWhenOpened) "🔒" else "🔓",
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. MANTER CAPA LIGADA AO ABRIR",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (keepCoverOnWhenOpened) "🔥 Impede a tela externa de apagar quando abrir o cel" else "Evita que a tela externa apague ao abrir o Z Flip",
                            fontSize = 9.5.sp,
                            color = Color.Gray
                        )
                    }
                }

                Switch(
                    checked = keepCoverOnWhenOpened,
                    onCheckedChange = {
                        triggerHaptic()
                        keepCoverOnWhenOpened = it
                        if (isMirroringActive) {
                            CoverMirrorService.startMirroring(context, keepCoverOnWhenOpened)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFD700),
                        checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indicador de Status dos 2 Recurso
        Surface(
            color = if (isMirroringActive) Color(0xFF00FF66).copy(alpha = 0.15f) else Color(0xFF00F0FF).copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isMirroringActive) Color(0xFF00FF66) else Color(0xFF00F0FF)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isMirroringActive) "▶ ESPELHAMENTO EM EXECUÇÃO" else "⏸ PRONTO PARA ESPELHAR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (keepCoverOnWhenOpened) "🔥 MODO CAPA LIGADA AO ABRIR O CELULAR: [ATIVADO]" else "❄️ MODO PADRÃO DE DESLIGAMENTO: [PADRÃO]",
                    fontSize = 10.sp,
                    color = if (keepCoverOnWhenOpened) Color(0xFFFFD700) else Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rodapé de Informações Técnicas e Apoio
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = if (hasCoverDisplay) "✓ Tela de Capa Z Flip Detectada" else "ℹ️ Compatível com Galaxy Z Flip 5 / 6 / 7",
                fontSize = 10.5.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                    .height(42.dp)
            ) {
                Text(
                    text = "Apoiar Criador via PayPal",
                    color = Color(0xFF003087),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
