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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
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

    // Botão 1: Espelhamento Direto da Tela Principal na Capa
    var isMirroringActive by remember { mutableStateOf(false) }

    // Botão 2: Manter a Tela de Capa Ligada quando o Z Flip for ABERTO
    var keepCoverOnWhenOpened by remember { mutableStateOf(false) }

    // Botão 3: Estado do Serviço de Multitarefas
    var isRecentsActive by remember { mutableStateOf(false) }

    // Estado do Pinch-to-Zoom
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }

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

    val button3Color by animateColorAsState(
        targetValue = if (isRecentsActive) Color(0xFF00FF66) else Color(0xFFFF007F),
        animationSpec = tween(durationMillis = 300),
        label = "Button3ColorAnim"
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabeçalho Principal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_zflip_logo),
                contentDescription = "ZFlip Cover Mirror Logo",
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF00F0FF), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "ZFLIP COVER MIRROR",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00F0FF),
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "Espelhamento, Multitarefas e Zoom na Capa",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CAIXA DE PRÉVIA & ZOOM POR PINÇA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070B14))
                .border(2.dp, if (zoomScale > 1.05f) Color(0xFFFFD700) else Color(0xFF00F0FF), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (zoomScale * zoom).coerceIn(1.0f, 4.0f)
                        zoomScale = newScale

                        val maxOffsetX = (size.width * (zoomScale - 1f)) / 2f
                        val maxOffsetY = (size.height * (zoomScale - 1f)) / 2f

                        zoomOffset = Offset(
                            x = (zoomOffset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                            y = (zoomOffset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            triggerHaptic()
                            zoomScale = 1.0f
                            zoomOffset = Offset.Zero
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomScale,
                        scaleY = zoomScale,
                        translationX = zoomOffset.x,
                        translationY = zoomOffset.y
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = if (isMirroringActive) "📱 ESPELHAMENTO & TOUCH ATIVO" else "🔍 PRÉVIA & ZOOM POR PINÇA",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "• Touch 1:1 Calibrado na Capa\n• Faça PINÇA (2 dedos) para Zoom (${"%.1f".format(zoomScale)}x)\n• Duplo toque reseta o Zoom",
                        fontSize = 9.5.sp,
                        color = Color(0xFF00F0FF),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (zoomScale > 1.05f) {
                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "ZOOM ${"%.1f".format(zoomScale)}x",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // OS 3 BOTÕES DE CONTROLE
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // BOTÃO 1: ESPELHAR TELA PRINCIPAL NA CAPA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1726))
                    .border(2.dp, button1Color, RoundedCornerShape(12.dp))
                    .clickable {
                        triggerHaptic()
                        isMirroringActive = !isMirroringActive

                        if (isMirroringActive) {
                            CoverMirrorService.startMirroring(context, keepCoverOnWhenOpened)
                        } else {
                            CoverMirrorService.stopMirroring(context)
                        }
                    }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(button1Color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isMirroringActive) "⏸" else "🖥️",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "1. ESPELHAR TELA NA CAPA",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (isMirroringActive) "▶ Espelhamento Touch 1:1 Ativo" else "Transmita a tela grande na capa",
                            fontSize = 8.5.sp,
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

            // BOTÃO 2: MANTER TELA DE CAPA LIGADA QUANDO O Z FLIP FOR ABERTO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1726))
                    .border(2.dp, button2Color, RoundedCornerShape(12.dp))
                    .clickable {
                        triggerHaptic()
                        keepCoverOnWhenOpened = !keepCoverOnWhenOpened

                        if (isMirroringActive) {
                            CoverMirrorService.startMirroring(context, keepCoverOnWhenOpened)
                        }
                    }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(button2Color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (keepCoverOnWhenOpened) "🔒" else "🔓",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. MANTER CAPA LIGADA AO ABRIR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (keepCoverOnWhenOpened) "🔥 Impede a tela externa de apagar" else "Evita que a tela externa apague ao abrir",
                            fontSize = 8.5.sp,
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

            // BOTÃO 3 (NOVO): MULTITAREFAS / VER APPS ABERTOS NA CAPA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1726))
                    .border(2.dp, button3Color, RoundedCornerShape(12.dp))
                    .clickable {
                        triggerHaptic()
                        val success = ZFlipAccessibilityService.openRecents(context)
                        isRecentsActive = success
                    }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(button3Color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3. MULTITAREFAS NA CAPA (APPS ABERTOS)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Abre o alternador de apps abertos na tela externa",
                            fontSize = 8.5.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = {
                        triggerHaptic()
                        val success = ZFlipAccessibilityService.openRecents(context)
                        isRecentsActive = success
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "ABRIR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Indicador de Status & Eficiência
        Surface(
            color = if (isMirroringActive) Color(0xFF00FF66).copy(alpha = 0.15f) else Color(0xFF00F0FF).copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isMirroringActive) Color(0xFF00FF66) else Color(0xFF00F0FF)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isMirroringActive) "▶ ESPELHAMENTO, TOUCH & ZOOM 1:1 ATIVO" else "⏸ PRONTO PARA ESPELHAR NA CAPA",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (keepCoverOnWhenOpened) "🔥 MODO CAPA LIGADA AO ABRIR: [ATIVADO]" else "⚡ MULTITAREFAS & BATERIA OTIMIZADOS [100% OK]",
                    fontSize = 8.5.sp,
                    color = if (keepCoverOnWhenOpened) Color(0xFFFFD700) else Color(0xFF00FF66),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rodapé de Apoio
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (hasCoverDisplay) "✓ Tela de Capa Z Flip Detectada" else "ℹ️ Compatível com Galaxy Z Flip 5 / 6 / 7",
                fontSize = 9.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Text(
                    text = "Apoiar Criador via PayPal",
                    color = Color(0xFF003087),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}
