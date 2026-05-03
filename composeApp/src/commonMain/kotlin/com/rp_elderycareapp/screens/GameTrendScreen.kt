package com.rp_elderycareapp.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.api.GameApi
import com.rp_elderycareapp.api.SessionHistoryItem
import com.rp_elderycareapp.data.GameRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTrendScreen(
    userId: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember { GameRepository(GameApi()) }
    var sessions by remember { mutableStateOf<List<SessionHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        repository.getSessionHistory(userId)
            .onSuccess { response ->
                // API returns newest-first; reverse to chronological for charts
                sessions = response.sessions.reversed().takeLast(15)
                isLoading = false
            }
            .onFailure { e ->
                errorMsg = e.message ?: "Failed to load session history"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cognitive Trends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0C4A6E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFDCFCE7))
                    )
                )
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingPlaceholder()
                errorMsg != null -> ErrorPlaceholder(errorMsg!!, onNavigateBack)
                sessions.isEmpty() -> EmptyPlaceholder(onNavigateBack)
                else -> TrendContent(sessions = sessions)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// State placeholders
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF0EA5E9))
            Spacer(Modifier.height(16.dp))
            Text("Loading your trend data…", color = Color(0xFF475569))
        }
    }
}

@Composable
private fun ErrorPlaceholder(message: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("⚠️", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color(0xFF475569), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onBack) { Text("Go Back") }
        }
    }
}

@Composable
private fun EmptyPlaceholder(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📊", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "No game sessions yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0C4A6E),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Complete at least one game session to see your cognitive trend.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onBack) { Text("← Play Now") }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrendContent(sessions: List<SessionHistoryItem>) {
    val riskScores  = sessions.map { it.riskScore.toFloat() }
    val sacValues   = sessions.map { it.sac.toFloat() }
    val iesValues   = sessions.map { it.ies.toFloat() }
    val accValues   = sessions.map { (it.accuracy * 100).toFloat() }
    val hdrValues   = sessions.map { (it.hintDependencyRate * 100).toFloat() }
    val xLabels     = sessions.mapIndexed { i, _ -> "S${i + 1}" }

    val riskSlope = trendSlope(riskScores)
    val (trendLabel, trendColor, trendEmoji) = when {
        riskSlope > 2f  -> Triple("Risk Increasing", Color(0xFFEF4444), "↑")
        riskSlope < -2f -> Triple("Risk Declining",  Color(0xFF10B981), "↓")
        else            -> Triple("Risk Stable",     Color(0xFFF59E0B), "→")
    }

    val latest = sessions.last()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Summary card ──────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp),
                    ambientColor = Color(0xFF0EA5E9).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Sessions Tracked",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            "${sessions.size}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0C4A6E)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = trendColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(trendEmoji, fontSize = 20.sp)
                            Text(
                                trendLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = trendColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(Modifier.height(16.dp))

                Text(
                    "Latest Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0C4A6E)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniMetric("Risk", "${latest.riskScore.toInt()}/100",
                        riskColor(latest.riskLevel))
                    MiniMetric("SAC",  "%.3f".format(latest.sac),   Color(0xFF0EA5E9))
                    MiniMetric("IES",  "%.2f".format(latest.ies),   Color(0xFFF59E0B))
                    MiniMetric("Acc",  "${(latest.accuracy * 100).toInt()}%", Color(0xFF10B981))
                    MiniMetric("Hints","${(latest.hintDependencyRate * 100).toInt()}%", Color(0xFFD97706))
                }
            }
        }

        // ── Risk Score chart ──────────────────────────────────────────────────
        TrendCard(
            title = "Risk Score",
            subtitle = "Cognitive decline risk  (0 = safe · 100 = high risk)",
            values = riskScores,
            xLabels = xLabels,
            lineColor = Color(0xFFEF4444),
            fillColor = Color(0xFFEF4444).copy(alpha = 0.08f),
            yMin = 0f,
            yMax = maxOf(100f, (riskScores.maxOrNull() ?: 100f) * 1.1f),
            yLabelFmt = { "${it.toInt()}" },
            goodDirection = "lower"
        )

        // ── SAC chart ─────────────────────────────────────────────────────────
        TrendCard(
            title = "Speed-Accuracy (SAC)",
            subtitle = "Higher = better cognitive efficiency",
            values = sacValues,
            xLabels = xLabels,
            lineColor = Color(0xFF0EA5E9),
            fillColor = Color(0xFF0EA5E9).copy(alpha = 0.08f),
            yMin = 0f,
            yMax = maxOf((sacValues.maxOrNull() ?: 1f) * 1.2f, 0.5f),
            yLabelFmt = { "%.2f".format(it) },
            goodDirection = "higher"
        )

        // ── IES chart ─────────────────────────────────────────────────────────
        TrendCard(
            title = "Inverse Efficiency (IES)",
            subtitle = "Lower = better  (time cost per correct response)",
            values = iesValues,
            xLabels = xLabels,
            lineColor = Color(0xFFF59E0B),
            fillColor = Color(0xFFF59E0B).copy(alpha = 0.08f),
            yMin = 0f,
            yMax = minOf((iesValues.maxOrNull() ?: 5f) * 1.2f, 20f),
            yLabelFmt = { "%.1f".format(it) },
            goodDirection = "lower"
        )

        // ── Accuracy chart ────────────────────────────────────────────────────
        TrendCard(
            title = "Session Accuracy",
            subtitle = "Total correct responses per session (incl. hint-assisted)",
            values = accValues,
            xLabels = xLabels,
            lineColor = Color(0xFF10B981),
            fillColor = Color(0xFF10B981).copy(alpha = 0.08f),
            yMin = 0f,
            yMax = 100f,
            yLabelFmt = { "${it.toInt()}%" },
            goodDirection = "higher"
        )

        // ── Hint Dependency chart ─────────────────────────────────────────────
        TrendCard(
            title = "Hint Dependency Rate",
            subtitle = "% of trials where hint assistance was needed",
            values = hdrValues,
            xLabels = xLabels,
            lineColor = Color(0xFFD97706),
            fillColor = Color(0xFFD97706).copy(alpha = 0.08f),
            yMin = 0f,
            yMax = 100f,
            yLabelFmt = { "${it.toInt()}%" },
            goodDirection = "lower"
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TrendCard wrapper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrendCard(
    title: String,
    subtitle: String,
    values: List<Float>,
    xLabels: List<String>,
    lineColor: Color,
    fillColor: Color,
    yMin: Float,
    yMax: Float,
    yLabelFmt: (Float) -> String,
    goodDirection: String
) {
    val slope = trendSlope(values)
    val improving = (goodDirection == "higher" && slope >= 0f) ||
                    (goodDirection == "lower"  && slope <= 0f)
    val trendText = when {
        goodDirection == "higher" && slope >  0.005f -> "Improving ↑"
        goodDirection == "higher" && slope < -0.005f -> "Declining ↓"
        goodDirection == "lower"  && slope < -0.005f -> "Improving ↓"
        goodDirection == "lower"  && slope >  0.005f -> "Worsening ↑"
        else -> "Stable →"
    }
    val trendBadgeColor = when {
        trendText.contains("Stable")  -> Color(0xFFF59E0B)
        improving                     -> Color(0xFF10B981)
        else                          -> Color(0xFFEF4444)
    }

    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 0f
    val avg = if (values.isEmpty()) 0f else values.average().toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C4A6E)
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = trendBadgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        trendText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = trendBadgeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (values.size == 1) {
                // Single session — no line yet
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            yLabelFmt(values[0]),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = lineColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Session 1", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(
                            "Play more sessions to see your trend",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LineChartCanvas(
                    values = values,
                    xLabels = xLabels,
                    lineColor = lineColor,
                    fillColor = fillColor,
                    yMin = yMin,
                    yMax = yMax,
                    yLabelFmt = yLabelFmt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            // Min / Avg / Max / Latest row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("Min",    yLabelFmt(min),           Color(0xFF64748B))
                StatChip("Avg",    yLabelFmt(avg),           lineColor)
                StatChip("Max",    yLabelFmt(max),           Color(0xFF64748B))
                StatChip("Latest", yLabelFmt(values.last()), lineColor)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure-Canvas line chart (no external library)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LineChartCanvas(
    values: List<Float>,
    xLabels: List<String>,
    lineColor: Color,
    fillColor: Color,
    yMin: Float,
    yMax: Float,
    yLabelFmt: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val gridSteps = 4
    val yRange = (yMax - yMin).coerceAtLeast(0.0001f)

    Column(modifier = modifier) {
        // Chart body — Y-axis labels + canvas
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Y-axis labels (top-to-bottom = yMax→yMin)
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (step in 0..gridSteps) {
                    val value = yMax - step.toFloat() / gridSteps * yRange
                    Text(
                        text = yLabelFmt(value),
                        fontSize = 8.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.End),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Canvas — draws grid, fill, line, dots
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val w = size.width
                val h = size.height
                val n = values.size

                fun toX(i: Int) = (i.toFloat() / (n - 1).coerceAtLeast(1)) * w
                fun toY(v: Float) = h * (1f - (v.coerceIn(yMin, yMax) - yMin) / yRange)
                fun pt(i: Int) = Offset(toX(i), toY(values[i]))

                // Horizontal grid lines
                for (step in 0..gridSteps) {
                    val y = h * step.toFloat() / gridSteps
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 0.8.dp.toPx()
                    )
                }

                // Fill area under line
                val fillPath = Path().apply {
                    moveTo(toX(0), h)
                    lineTo(toX(0), toY(values[0]))
                    for (i in 1 until n) lineTo(toX(i), toY(values[i]))
                    lineTo(toX(n - 1), h)
                    close()
                }
                drawPath(fillPath, color = fillColor)

                // Line
                val linePath = Path().apply {
                    moveTo(pt(0).x, pt(0).y)
                    for (i in 1 until n) lineTo(pt(i).x, pt(i).y)
                }
                drawPath(
                    linePath,
                    color = lineColor,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Data point dots (white fill + coloured ring)
                for (i in 0 until n) {
                    drawCircle(Color.White,   5.dp.toPx(), pt(i))
                    drawCircle(lineColor, 4.dp.toPx(), pt(i),
                        style = Stroke(2.dp.toPx()))
                }
            }
        }

        // X-axis labels — show first / middle(s) / last
        Spacer(Modifier.height(4.dp))
        val n = values.size
        val showAt: List<Int> = when {
            n <= 5  -> (0 until n).toList()
            n <= 9  -> listOf(0, n / 2, n - 1)
            else    -> listOf(0, n / 3, 2 * n / 3, n - 1)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Evenly distribute visible labels using Spacers between them
            showAt.forEachIndexed { idx, dataIndex ->
                Text(
                    text = xLabels.getOrElse(dataIndex) { "S${dataIndex + 1}" },
                    fontSize = 8.sp,
                    color = Color(0xFF94A3B8)
                )
                if (idx < showAt.lastIndex) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MiniMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.1f)) {
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun riskColor(level: String): Color = when (level) {
    "LOW"    -> Color(0xFF10B981)
    "MEDIUM" -> Color(0xFFF59E0B)
    "HIGH"   -> Color(0xFFEF4444)
    else     -> Color(0xFF94A3B8)
}

/** Linear regression slope of a list of floats. */
private fun trendSlope(values: List<Float>): Float {
    if (values.size < 2) return 0f
    val n = values.size
    val xMean = (n - 1) / 2f
    val yMean = values.average().toFloat()
    var num = 0f
    var den = 0f
    values.forEachIndexed { i, v ->
        val dx = i - xMean
        num += dx * (v - yMean)
        den += dx * dx
    }
    return if (den == 0f) 0f else num / den
}
