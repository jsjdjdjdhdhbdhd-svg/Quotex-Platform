package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.CalculatedTradingDay
import com.example.ui.QuotexUiState
import com.example.ui.QuotexViewModel
import com.example.ui.components.DoughnutChart
import com.example.ui.components.PerformanceBarChart
import com.example.ui.theme.LocalThemeIsDark
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import java.util.Locale
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun getAppBackground(): Color = if (LocalThemeIsDark.current) Color(0xFF0A0F1D) else Color(0xFFF1F5F9)

@Composable
fun getCardBackground(): Color = if (LocalThemeIsDark.current) Color(0xFF121A30) else Color(0xFFFFFFFF)

@Composable
fun getBorderColor(): Color = if (LocalThemeIsDark.current) Color(0xFF1E294B) else Color(0xFFE2E8F0)

@Composable
fun getTextColor(): Color = if (LocalThemeIsDark.current) Color.White else Color(0xFF0F172A)

@Composable
fun getSubtextColor(): Color = if (LocalThemeIsDark.current) Color(0xFF94A3B8) else Color(0xFF475569)

enum class Tab(val title: String, val icon: ImageVector) {
    Analytics("Analytics", Icons.Default.Analytics),
    Journal("Journal", Icons.Default.Book),
    Settings("Settings", Icons.Default.Settings),
    CreatorInfo("Creator Info", Icons.Default.Person)
}

@Composable
fun MainDashboard(
    uiState: QuotexUiState,
    viewModel: QuotexViewModel,
    modifier: Modifier = Modifier
) {
    if (!uiState.isAppUnlocked) {
        PinLockScreen(uiState = uiState, viewModel = viewModel)
        return
    }

    var currentTab by remember { mutableStateOf(Tab.Journal) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = getAppBackground(),
        bottomBar = {
            NavigationBar(
                containerColor = getCardBackground(),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            unselectedIconColor = getSubtextColor(),
                            unselectedTextColor = getSubtextColor(),
                            indicatorColor = getBorderColor()
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                Tab.Analytics -> AnalyticsTab(uiState = uiState)
                Tab.Journal -> JournalTab(uiState = uiState, viewModel = viewModel)
                Tab.Settings -> SettingsTab(uiState = uiState, viewModel = viewModel)
                Tab.CreatorInfo -> CreatorInfoTab()
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// SECURITY PIN LOCK SCREEN
// -----------------------------------------------------------------------------------
@Composable
fun PinLockScreen(
    uiState: QuotexUiState,
    viewModel: QuotexViewModel
) {
    var enteredPin by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Secure, beautiful dark slate background
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color(0xFF10B981)),
                    modifier = Modifier.size(80.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    AsyncImage(
                        model = R.drawable.quotex_platform_premium_logo_1779459028557,
                        contentDescription = "Quotex Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Text(
                    text = "QUOTEX PLATFORM",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "App is PIN Locked",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // PIN Indicator Circles
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = if (isFilled) Color(0xFF10B981) else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(2.dp, Color(0xFF1E294B), CircleShape)
                    )
                }
            }

            // Number Pad Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val buttonRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )

                buttonRows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.8f)
                                    .background(Color(0xFF1D293B), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                    .clickable {
                                        when (char) {
                                            "⌫" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            }
                                            "C" -> {
                                                enteredPin = ""
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += char
                                                    if (enteredPin.length == 4) {
                                                        val unlocked = viewModel.verifyPinAndUnlock(enteredPin)
                                                        if (!unlocked) {
                                                            Toast.makeText(context, "Incorrect Security PIN. Please try again.", Toast.LENGTH_SHORT).show()
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// ANALYTICS TAB
// -----------------------------------------------------------------------------------
@Composable
fun AnalyticsTab(uiState: QuotexUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(title = "Analytics", subtitle = "Live Trading Cycles & Performance Metrics")

        // Dashboard Summary Card Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Capital",
                value = "$${String.format(Locale.US, "%.2f", uiState.totalCapital)}",
                subtitle = "Active Balance",
                accentColor = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Accumulated Profit",
                value = "$${String.format(Locale.US, "%.2f", uiState.accumulatedProfit)}",
                subtitle = "Net Earnings",
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Accumulated Loss",
                value = "$${String.format(Locale.US, "%.2f", uiState.accumulatedLoss)}",
                subtitle = "Deficit Recorded",
                accentColor = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
            val currentProgress = if (uiState.totalDays > 0) {
                val closedDays = uiState.days.count { it.status != null }
                "$closedDays / ${uiState.totalDays}"
            } else "0/0"
            StatCard(
                title = "Days Active",
                value = currentProgress,
                subtitle = "Cycle Progress",
                accentColor = Color(0xFFEAB308),
                modifier = Modifier.weight(1f)
            )
        }

        // Charts Section Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A30)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E294B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Capital Profit vs. Loss Ratio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                DoughnutChart(
                    profit = uiState.accumulatedProfit,
                    loss = uiState.accumulatedLoss,
                    modifier = Modifier
                        .size(170.dp)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem("Accumulated Profit", Color(0xFF10B981))
                    LegendItem("Accumulated Loss", Color(0xFFEF4444))
                }
            }
        }

        // Performance Bar Chart Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A30)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E294B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Historical Win/Loss Days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(20.dp))
                PerformanceBarChart(
                    profitDays = uiState.profitDaysCount,
                    lossDays = uiState.lossDaysCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
    }
}

// -----------------------------------------------------------------------------------
// JOURNAL TAB
// -----------------------------------------------------------------------------------
@Composable
fun JournalTab(
    uiState: QuotexUiState,
    viewModel: QuotexViewModel
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var searchInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(title = "Trading History", subtitle = "Review past outcomes and manage pending active tasks")

        // Jump to row row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121A30), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF1E294B)), RoundedCornerShape(12.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = {
                    Text(
                        "Search Day S.N.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                maxLines = 1,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            Button(
                onClick = {
                    val targetDay = searchInput.toIntOrNull()
                    if (targetDay != null && targetDay in 1..uiState.days.size) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(targetDay - 1)
                        }
                    } else {
                        Toast.makeText(context, "S.N. must be between 1 and ${uiState.days.size}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = "SEARCH",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }

        // Lazy list of Days
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(uiState.days) { index, day ->
                TradingDayCard(
                    day = day,
                    isActive = day.sn == uiState.activeDaySn,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TradingDayCard(
    day: CalculatedTradingDay,
    isActive: Boolean,
    viewModel: QuotexViewModel
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    // Manual amount/profit text states
    var customAmountStr by remember(day.amount) { mutableStateOf(String.format(Locale.US, "%.2f", day.amount)) }
    var customProfitStr by remember(day.profit) { mutableStateOf(String.format(Locale.US, "%.2f", day.profit)) }
    var customLossStr by remember(day.currentLoss) { mutableStateOf(String.format(Locale.US, "%.2f", day.currentLoss)) }

    // Card colors depending on state
    val borderColor = when {
        day.status == "complete" -> Color(0xFF10B981)
        day.status == "loss" -> Color(0xFFEF4444)
        day.status == "fail" -> Color(0xFF64748B)
        isActive -> Color(0xFF3B82F6)
        else -> Color(0xFF1E294B)
    }

    val cardColor = if (isActive) Color(0xFF142340) else Color(0xFF121A30)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(if (isActive) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cyclic Day Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = when {
                                    day.status == "complete" -> Color(0x3310B981)
                                    day.status == "loss" -> Color(0x33EF4444)
                                    day.status == "fail" -> Color(0x3364748B)
                                    isActive -> Color(0x333B82F6)
                                    else -> Color(0x11FFFFFF)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "DAY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${day.sn}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    day.status == "complete" -> Color(0xFF10B981)
                                    day.status == "loss" -> Color(0xFFEF4444)
                                    day.status == "fail" -> Color(0xFFFFFFFFF) // wait
                                    else -> Color.White
                                }
                            )
                        }
                    }

                    // Task status description
                    Column {
                        val titleText = when (day.status) {
                            "complete" -> "CLOSED: COMPLETED"
                            "loss" -> "CLOSED: LOSS"
                            "fail" -> "CLOSED: NOT COMPLETED"
                            else -> if (isActive) "ACTIVE: PENDING" else "PENDING QUEUE"
                        }
                        val titleColor = when (day.status) {
                            "complete" -> Color(0xFF10B981)
                            "loss" -> Color(0xFFEF4444)
                            "fail" -> Color(0xFF94A3B8)
                            else -> if (isActive) Color(0xFF3B82F6) else Color(0xFF94A3B8)
                        }
                        Text(
                            text = titleText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = titleColor
                        )

                        Text(
                            text = if (day.status == "loss") {
                                "Recorded Loss: $${String.format(Locale.US, "%.2f", day.currentLoss)}"
                            } else {
                                "Target: $${String.format(Locale.US, "%.2f", day.amount)} to $${String.format(Locale.US, "%.2f", day.profit)}"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Edit Button
                IconButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = "Edit values",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Inline Edit Form Panel
            if (isEditing) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "INLINE CUSTOM OVERRIDES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3B82F6),
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customAmountStr,
                            onValueChange = { customAmountStr = it },
                            label = { Text("Base ($)", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = customProfitStr,
                            onValueChange = { customProfitStr = it },
                            label = { Text("Profit Target ($)", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customLossStr,
                            onValueChange = { customLossStr = it },
                            label = { Text("Today's Loss ($)", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Button(
                                onClick = {
                                    val parsedAmount = customAmountStr.replace(',', '.').toDoubleOrNull()
                                    val parsedProfit = customProfitStr.replace(',', '.').toDoubleOrNull()
                                    val parsedLoss = customLossStr.replace(',', '.').toDoubleOrNull()

                                    val amount = if (parsedAmount != null && parsedAmount >= 0.0 && !parsedAmount.isNaN() && !parsedAmount.isInfinite()) parsedAmount else day.amount
                                    val profit = if (parsedProfit != null && parsedProfit >= 0.0 && !parsedProfit.isNaN() && !parsedProfit.isInfinite()) parsedProfit else day.profit
                                    val loss = if (parsedLoss != null && parsedLoss >= 0.0 && !parsedLoss.isNaN() && !parsedLoss.isInfinite()) parsedLoss else 0.0

                                    viewModel.updateManualAmount(day.sn, amount, true)
                                    viewModel.updateManualProfit(day.sn, profit, true)
                                    viewModel.logLoss(day.sn, loss)

                                    isEditing = false
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("APPLY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    viewModel.resetInlineEditField(day.sn, "amount")
                                    viewModel.resetInlineEditField(day.sn, "profit")
                                    viewModel.resetInlineEditField(day.sn, "loss")
                                    isEditing = false
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("RESET AUTO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Expanded active controls
            if (isActive && !isEditing) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF1E294B))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "CURRENT BALANCE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            "$${String.format(Locale.US, "%.2f", day.amount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "TARGET PROFIT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            "$${String.format(Locale.US, "%.2f", day.profit)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.updateStatus(day.sn, "complete") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            "COMPLETE TASK",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { viewModel.updateStatus(day.sn, "fail") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            "NOT COMPLETED",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var localLossInput by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = localLossInput,
                        onValueChange = { localLossInput = it },
                        placeholder = { Text("Loss ($)", color = Color(0xFF5A667A), fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val parsedLoss = localLossInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                            val lossAmount = if (parsedLoss >= 0.0 && !parsedLoss.isNaN() && !parsedLoss.isInfinite()) parsedLoss else 0.0
                            if (lossAmount > 0.0) {
                                viewModel.logLoss(day.sn, lossAmount)
                                localLossInput = ""
                            } else {
                                Toast.makeText(context, "Please enter a valid positive loss amount", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("LOG LOSS", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Action for resetting completed/non-active day
            if (!isActive && day.status != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { viewModel.updateStatus(day.sn, null) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Reset to Active", tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                            Text("Reopen Task", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------------
// SETTINGS TAB
// -----------------------------------------------------------------------------------
@Composable
fun SettingsTab(
    uiState: QuotexUiState,
    viewModel: QuotexViewModel
) {
    var daysInput by remember(uiState.totalDays) { mutableStateOf(uiState.totalDays.toString()) }
    var startAmountInput by remember(uiState.startAmount) { mutableStateOf(String.format(Locale.US, "%.2f", uiState.startAmount)) }
    var profitIncInput by remember(uiState.profitIncrement) { mutableStateOf(String.format(Locale.US, "%.2f", uiState.profitIncrement)) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Security PIN Setup/Change state variables
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinConfirmInput by remember { mutableStateOf("") }
    var isSettingNewPin by remember { mutableStateOf(false) }
    
    // Security PIN Disable verification variables
    var showDisableVerifyDialog by remember { mutableStateOf(false) }
    var disableVerifyPinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(title = "Settings", subtitle = "Configure core trading parameters and dynamic tables")

        // App Theme Configuration (Day & Night Sun)
        Card(
            colors = CardDefaults.cardColors(containerColor = getCardBackground()),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, getBorderColor()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "App Theme Mode (Day & Night)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = getTextColor()
                )

                Text(
                    text = "Configure light theme (represented by the Sun) or dark theme (represented by the Moon) according to your preference.",
                    fontSize = 11.sp,
                    color = getSubtextColor(),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        Triple("light", "Light (Sun)", Icons.Default.WbSunny),
                        Triple("dark", "Dark (Moon)", Icons.Default.NightsStay),
                        Triple("system", "System", Icons.Default.SettingsSuggest)
                    )

                    themeOptions.forEach { (mode, label, icon) ->
                        val isSelected = uiState.themeMode == mode
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF10B981) else if (LocalThemeIsDark.current) Color(0xFF1E294B) else Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981) else getBorderColor()),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateThemeMode(mode) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color.White else if (LocalThemeIsDark.current) Color(0xFF94A3B8) else Color(0xFF475569),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else getTextColor(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Security PIN Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = getCardBackground()),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, getBorderColor()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Security PIN Protection",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = getTextColor()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isPinEnabled) "PIN Lock Enabled" else "PIN Lock Disabled",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isPinEnabled) Color(0xFF10B981) else getTextColor()
                        )
                        Text(
                            text = "Require 4-digit PIN access on application startup.",
                            fontSize = 11.sp,
                            color = getSubtextColor(),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = uiState.isPinEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                isSettingNewPin = true
                                pinInput = ""
                                pinConfirmInput = ""
                                showPinDialog = true
                            } else {
                                disableVerifyPinInput = ""
                                showDisableVerifyDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = if (LocalThemeIsDark.current) Color(0xFF1E294B) else Color(0xFFCBD5E1)
                        )
                    )
                }

                if (uiState.isPinEnabled) {
                    HorizontalDivider(color = getBorderColor(), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                isSettingNewPin = false
                                pinInput = ""
                                pinConfirmInput = ""
                                showPinDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (LocalThemeIsDark.current) Color(0xFF1E294B) else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "CHANGE PIN",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = getTextColor()
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.relockApp()
                                Toast.makeText(context, "App locks instantly", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "LOCK NOW",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Core Parameter Layout Card
        Card(
            colors = CardDefaults.cardColors(containerColor = getCardBackground()),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, getBorderColor()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Core Parameter Configuration",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = getTextColor()
                )

                OutlinedTextField(
                    value = daysInput,
                    onValueChange = { daysInput = it },
                    label = { Text("Total Cycle Days", fontWeight = FontWeight.Bold, color = getSubtextColor()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = getTextColor(),
                        unfocusedTextColor = getTextColor(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startAmountInput,
                    onValueChange = { startAmountInput = it },
                    label = { Text("Starting Balance ($)", fontWeight = FontWeight.Bold, color = getSubtextColor()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = getTextColor(),
                        unfocusedTextColor = getTextColor(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = profitIncInput,
                    onValueChange = { profitIncInput = it },
                    label = { Text("Base Profit Increment ($)", fontWeight = FontWeight.Bold, color = getSubtextColor()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = getTextColor(),
                        unfocusedTextColor = getTextColor(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val days = daysInput.toIntOrNull()
                        val amount = startAmountInput.replace(',', '.').toDoubleOrNull()
                        val profit = profitIncInput.replace(',', '.').toDoubleOrNull()
                        
                        if (days == null || days !in 1..365) {
                            Toast.makeText(context, "Cycle Days must be between 1 and 365", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amount == null || amount < 0.01 || amount.isNaN() || amount.isInfinite()) {
                            Toast.makeText(context, "Starting Balance must be at least 0.01", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (profit == null || profit < 0.01 || profit.isNaN() || profit.isInfinite()) {
                            Toast.makeText(context, "Profit Increment must be at least 0.01", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.updateSettings(days, amount, profit)
                        Toast.makeText(context, "Parameters Updated successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("UPDATE TABLE STRUCTURE", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }

        // Import & Export Backup Area
        Card(
            colors = CardDefaults.cardColors(containerColor = getCardBackground()),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, getBorderColor()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Backup, Portability & System Reset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = getTextColor()
                )

                Text(
                    "Export your status layout as robust JSON payload or paste an existing dump to import state cascades.",
                    fontSize = 11.sp,
                    color = getSubtextColor(),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val jsonString = viewModel.exportDataToString()
                            if (jsonString.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(jsonString))
                                Toast.makeText(context, "JSON Data copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("EXPORT DATA", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("IMPORT DATA", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("RESET ALL LOCAL DATA", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }

    // PIN Setup or Change Dialog Box
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = getCardBackground(),
            title = {
                Text(
                    text = if (isSettingNewPin) "Set 4-Digit Security PIN" else "Change 4-Digit PIN",
                    color = getTextColor(),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Please enter and confirm a 4-digit PIN for locks access.",
                        fontSize = 12.sp,
                        color = getSubtextColor()
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("Enter 4-digit PIN", color = getSubtextColor()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = getTextColor(),
                            unfocusedTextColor = getTextColor()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pinConfirmInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinConfirmInput = it },
                        label = { Text("Confirm PIN", color = getSubtextColor()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = getTextColor(),
                            unfocusedTextColor = getTextColor()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length != 4 || pinConfirmInput.length != 4) {
                            Toast.makeText(context, "PIN code must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        if (pinInput != pinConfirmInput) {
                            Toast.makeText(context, "PIN codes do not match", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        viewModel.enablePin(pinInput)
                        showPinDialog = false
                        Toast.makeText(context, "Security PIN code initialized!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("SAVE PIN", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("CANCEL", color = getSubtextColor(), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Disable PIN verification dialog
    if (showDisableVerifyDialog) {
        AlertDialog(
            onDismissRequest = { showDisableVerifyDialog = false },
            containerColor = getCardBackground(),
            title = {
                Text(
                    text = "Verify Current PIN",
                    color = getTextColor(),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "To turn off PIN security protection, please enter your current 4-digit security PIN.",
                        fontSize = 12.sp,
                        color = getSubtextColor()
                    )

                    OutlinedTextField(
                        value = disableVerifyPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) disableVerifyPinInput = it },
                        label = { Text("Current PIN", color = getSubtextColor()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = getTextColor(),
                            unfocusedTextColor = getTextColor()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (disableVerifyPinInput == uiState.pinCode) {
                            viewModel.disablePin()
                            showDisableVerifyDialog = false
                            Toast.makeText(context, "App PIN security turned off successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Incorrect PIN code. Authorization denied.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("DISABLE PIN", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableVerifyDialog = false }) {
                    Text("CANCEL", color = getSubtextColor(), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Direct string copy import dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup Payload", fontWeight = FontWeight.Black, color = getTextColor()) },
            containerColor = getCardBackground(),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste JSON backup payload here:", fontSize = 12.sp, color = getSubtextColor())
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = getTextColor(),
                            unfocusedTextColor = getTextColor()
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importDataFromString(importJsonText)
                        if (success) {
                            Toast.makeText(context, "System Data Restored!", Toast.LENGTH_SHORT).show()
                            showImportDialog = false
                            importJsonText = ""
                        } else {
                            Toast.makeText(context, "Malformed backup payload", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("IMPORT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("CANCEL", color = getSubtextColor(), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Confirm System Reset", fontWeight = FontWeight.Black, color = getTextColor()) },
            containerColor = getCardBackground(),
            text = {
                Text("This action is completely destructive and resets the backup metrics, database logs, and configuration matrices to standard default levels. Continue?", fontSize = 13.sp, color = getSubtextColor())
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        Toast.makeText(context, "System Reset Activated", Toast.LENGTH_SHORT).show()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("YES, RESET", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("CANCEL", color = getSubtextColor(), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}


// -----------------------------------------------------------------------------------
// CREATOR INFO TAB
// -----------------------------------------------------------------------------------
@Composable
fun CreatorInfoTab() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(title = "Platform Credits", subtitle = "Development guidelines and architect insights")

        // 1. Creators Showcase Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF1E294B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Creation Date Block
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CREATE DATE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "May 9, 2026",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Profile Image with Emerald outer ring
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(122.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFF3B82F6))
                                )
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://scontent.fbir4-1.fna.fbcdn.net/v/t39.30808-1/514371398_694654166535018_578283301784184910_n.jpg?stp=dst-jpg_s200x200_tt6&_nc_cat=110&ccb=1-7&_nc_sid=1d2534&_nc_eui2=AeE69TxJpoMDC65aV1MrXV2pnAn_m7Z0RyucCf-btnRHK8TW0tXSCzpO3L60G9G7jLws8KKfUo1Ec1ctzfQaNigN&_nc_ohc=rswFwBly4ZwQ7kNvwHlFzf4&_nc_oc=AdoInuAtBqGjMEGl3hmAT6pwciJ7n4MaiHCH9bkaNDu1QdedxcCG31Khyk2QPkoC2DPQCGR_-Bc8_u5vMDTsub2l&_nc_zt=24&_nc_ht=scontent.fbir4-1.fna&_nc_gid=qd-FDGnALeuSzm86oakY5Q&_nc_ss=7b2a8&oh=00_Af6USY9sJ9WsW0DFVnelKNVdoCbu6CsZo-P4em82FM32fw&oe=6A160963",
                            contentDescription = "Ganesh Kumar Das",
                            modifier = Modifier
                                .size(112.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(2.dp, Color(0xFF0F172A), CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                // Heading Texts
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CREATOR BY:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ganesh Kumar Das",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // 2. Custom Brown themed Social Button Capsule Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5C3D2E)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialButton(
                    painter = painterResource(R.drawable.ic_facebook),
                    bgColor = Color(0xFF1877F2),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/ganesh.das.276950")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No web browser found to open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))

                SocialButton(
                    painter = painterResource(R.drawable.ic_instagram),
                    bgColor = Color(0xFFE1306C),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/ganeshdas__63/")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No web browser found to open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))

                SocialButton(
                    icon = Icons.Default.Mail,
                    bgColor = Color(0xFFEA4335),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:mrganeshdas2082@gmail.com")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Mail client not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // 3. Robust stacked Specification blocks
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A30)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E294B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .drawBehind {
                        drawRect(
                            color = Color(0xFF3B82F6),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                        )
                    }
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF3B82F6), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Platform Info",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "PLATFORM INFORMATION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        Text(
                            text = "SYSTEM CORE PURPOSE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Precision-engineered for traders to visualize growth cycles and maintain strict financial management through iterative performance tasks.",
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = "ARCHITECTURE DETAILS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Utilizes local Room database persistence, high-fidelity drawn Canvas, and clean StateFlow models optimized for real-time reactivity.",
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = "USER EXPERIENCE FOCUS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Optimized for both high-intensity mobile monitoring and task tracking with adaptive visual feedback.",
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // DEVELOPMENT CREDITS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E19)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF134E5E).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .drawBehind {
                        drawRect(
                            color = Color(0xFF10B981),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                        )
                    }
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF10B981), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "DEVELOPMENT CREDITS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E294B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("PRODUCTION START", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("January 2026", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E294B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("STABLE VERSION", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("V 3.4.1 Stable", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }

        PlatformHelpSection()
    }
}

@Composable
fun PlatformHelpSection() {
    var expandedSection by remember { mutableStateOf<Int?>(1) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "PLATFORM HANDBOOK & GUIDES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        // Accordion Item 1: Trading Mechanics
        HelpAccordionItem(
            title = "1. Trading Cycles & Mechanics",
            icon = Icons.Default.TrendingUp,
            isExpanded = expandedSection == 1,
            onClick = { expandedSection = if (expandedSection == 1) null else 1 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "A step-by-step masterclass on operating the Quotex Platform dashboard:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                BulletPoint("Dynamic Roll Balance: Every day builds upon the outcome of your prior day. The starting balance flows continuously downward, establishing a realistic growth matrix.")
                BulletPoint("Managing Days: In the 'Journal' tab, select a day's target and click 'Complete' to log success (rolling your capital to current day's profit level) or 'Not Complete' (re-rolling active balance without change).")
                BulletPoint("Log Today Loss: Tap 'Today Loss' on any day to record specific deficits. Deficits dynamically subtract from the active day's balance to accurately configure downward curves.")
                BulletPoint("Automatic Row Search: Use the quick jump-search bar at the top with a Day number (S.N.) to automatically scroll directly down to that specific index with eye-catching highlight animations.")
            }
        }

        // Accordion Item 2: Features Directory
        HelpAccordionItem(
            title = "2. Complete Features Directory",
            icon = Icons.Default.Book,
            isExpanded = expandedSection == 2,
            onClick = { expandedSection = if (expandedSection == 2) null else 2 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BulletPoint("Journal Ledger: Detailed tabular list of all cycle Sn, transaction values, custom targets, and action controls.")
                BulletPoint("Inline Cell Modification: Tap or click directly on Total Amount, Profit, or Loss values on any row in the list to trigger immediate inline text editors with yellow focal indicator rings.")
                BulletPoint("Analytics Suite: Clean doughnut chart visually representing profit-loss parameters, alongside vertical bar charts comparing cumulative profit and loss days.")
                BulletPoint("Custom Tuning Panel: Adjust total cycle duration (1 to 365 days), custom initial deposits, and incremental base profits inside the fully validated settings drawer.")
                BulletPoint("Cold Backup Serialization: Export and import complete transaction logs as standard JSON strings to enable effortless device porting and physical record maintenance.")
                BulletPoint("Blue Light Guard & Accent: Features warm eye-friendly auto-sepia brightness modes for midnight chart reading.")
            }
        }

        // Accordion Item 3: Social Profile Details
        HelpAccordionItem(
            title = "3. Developer Profile & Integrations",
            icon = Icons.Default.Person,
            isExpanded = expandedSection == 3,
            onClick = { expandedSection = if (expandedSection == 3) null else 3 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "The architect profile and integration avenues of this platform:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                BulletPoint("Author: Ganesh Kumar Das — Chief Systems Developer engineering robust client-side software and reactive trade analytics visualizers.")
                BulletPoint("Aesthetics & Design: Inspired by physical dashboard aesthetics with dark premium canvases, high-density telemetry, and warm dynamic accents.")
                BulletPoint("Social Handle Verification:")
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("• Facebook: @ganesh.das.276950", fontSize = 11.sp, color = Color(0xFF1877F2), fontWeight = FontWeight.Bold)
                    Text("• Instagram: @ganeshdas__63", fontSize = 11.sp, color = Color(0xFFE1306C), fontWeight = FontWeight.Bold)
                }
                BulletPoint("Technical Support: For continuous platform updates or suggestions, directly contact support via email inside the social button row.")
            }
        }
    }
}

@Composable
fun HelpAccordionItem(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A30)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E294B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            lineHeight = 16.sp
        )
    }
}

@Composable
fun SocialButton(
    painter: androidx.compose.ui.graphics.painter.Painter,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = "Social link",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SocialButton(
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    SocialButton(
        painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon),
        bgColor = bgColor,
        onClick = onClick
    )
}


// -----------------------------------------------------------------------------------
// GENERAL COMMON STYLING COMPONENTS
// -----------------------------------------------------------------------------------
@Composable
fun HeaderSection(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF121A30), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF1E294B)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = "Growth logo icon",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A30)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF1E294B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .drawBehind {
                    // Left glow border accent line
                    drawRect(
                        color = accentColor,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height)
                    )
                }
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF94A3B8),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF5A667A),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
