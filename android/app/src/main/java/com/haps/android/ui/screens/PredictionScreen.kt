package com.haps.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haps.android.model.ClinicalFormState
import com.haps.android.model.PredictionResponse
import com.haps.android.ui.viewmodel.PredictionViewModel
import com.haps.android.util.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    predictionViewModel: PredictionViewModel,
    onNavigateBack: () -> Unit,
    onPredictionResult: (Long) -> Unit
) {
    val formState by predictionViewModel.formState.collectAsState()
    val patientName by predictionViewModel.patientName.collectAsState()
    val isLoading by predictionViewModel.isLoading.collectAsState()
    val predictionResult by predictionViewModel.predictionResult.collectAsState()

    var showPresetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(predictionResult) {
        if (predictionResult is Result.Success) {
            val data = (predictionResult as Result.Success<PredictionResponse>).data
            data.id?.let { onPredictionResult(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cardiac Assessment Form") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showPresetDialog = true }) {
                        Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Presets")
                    }
                    IconButton(onClick = { predictionViewModel.clearForm() }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Clear Form")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Error banner if any
            if (predictionResult is Result.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (predictionResult as Result.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 1. Patient Demographics Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Patient Demographics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { predictionViewModel.setPatientName(it) },
                        label = { Text("Patient Name / Reference ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.age,
                            onValueChange = { predictionViewModel.updateForm { s -> s.copy(age = it) } },
                            label = { Text("Age (years)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biological Sex", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = formState.sex == 1,
                                    onClick = { predictionViewModel.updateForm { it.copy(sex = 1) } },
                                    label = { Text("Male") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = formState.sex == 0,
                                    onClick = { predictionViewModel.updateForm { it.copy(sex = 0) } },
                                    label = { Text("Female") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Cardiac Vitals & Diagnostic Measurements
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Cardiac Vitals & Diagnostics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text("Chest Pain Type (cp)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val cpOptions = listOf("Typical Angina", "Atypical Angina", "Non-Anginal", "Asymptomatic")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        cpOptions.forEachIndexed { idx, label ->
                            FilterChip(
                                selected = formState.chestPainType == idx,
                                onClick = { predictionViewModel.updateForm { it.copy(chestPainType = idx) } },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formState.restingBp,
                            onValueChange = { predictionViewModel.updateForm { s -> s.copy(restingBp = it) } },
                            label = { Text("Resting BP (mm Hg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = formState.cholesterol,
                            onValueChange = { predictionViewModel.updateForm { s -> s.copy(cholesterol = it) } },
                            label = { Text("Cholesterol (mg/dl)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formState.maxHeartRate,
                            onValueChange = { predictionViewModel.updateForm { s -> s.copy(maxHeartRate = it) } },
                            label = { Text("Max HR (bpm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = formState.oldpeak,
                            onValueChange = { predictionViewModel.updateForm { s -> s.copy(oldpeak = it) } },
                            label = { Text("ST Depression (oldpeak)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exercise Angina (exang)", fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = formState.exerciseAngina == 1,
                                    onClick = { predictionViewModel.updateForm { it.copy(exerciseAngina = 1) } },
                                    label = { Text("Yes") }
                                )
                                FilterChip(
                                    selected = formState.exerciseAngina == 0,
                                    onClick = { predictionViewModel.updateForm { it.copy(exerciseAngina = 0) } },
                                    label = { Text("No") }
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fasting BS > 120 mg/dl", fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = formState.fastingBloodSugar == 1,
                                    onClick = { predictionViewModel.updateForm { it.copy(fastingBloodSugar = 1) } },
                                    label = { Text("Yes") }
                                )
                                FilterChip(
                                    selected = formState.fastingBloodSugar == 0,
                                    onClick = { predictionViewModel.updateForm { it.copy(fastingBloodSugar = 0) } },
                                    label = { Text("No") }
                                )
                            }
                        }
                    }

                    Column {
                        Text("Major Vessels Colored by Fluoroscopy (ca: 0 to 3)", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (0..3).forEach { v ->
                                FilterChip(
                                    selected = formState.ca == v,
                                    onClick = { predictionViewModel.updateForm { it.copy(ca = v) } },
                                    label = { Text("$v") }
                                )
                            }
                        }
                    }

                    Column {
                        Text("Thalassemia (thal)", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1 to "Normal", 2 to "Fixed Defect", 3 to "Reversible").forEach { (code, name) ->
                                FilterChip(
                                    selected = formState.thal == code,
                                    onClick = { predictionViewModel.updateForm { it.copy(thal = code) } },
                                    label = { Text(name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Clinical Candidate Extras & Lifestyle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Lifestyle & Candidate Risk Factors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FilterChip(
                            selected = formState.smoking == 1,
                            onClick = { predictionViewModel.updateForm { it.copy(smoking = if (it.smoking == 1) 0 else 1) } },
                            label = { Text("Smoking") },
                            leadingIcon = { if (formState.smoking == 1) Icon(Icons.Default.Check, contentDescription = null) }
                        )
                        FilterChip(
                            selected = formState.hypertension == 1,
                            onClick = { predictionViewModel.updateForm { it.copy(hypertension = if (it.hypertension == 1) 0 else 1) } },
                            label = { Text("Hypertension") },
                            leadingIcon = { if (formState.hypertension == 1) Icon(Icons.Default.Check, contentDescription = null) }
                        )
                        FilterChip(
                            selected = formState.diabetes == 1,
                            onClick = { predictionViewModel.updateForm { it.copy(diabetes = if (it.diabetes == 1) 0 else 1) } },
                            label = { Text("Diabetes") },
                            leadingIcon = { if (formState.diabetes == 1) Icon(Icons.Default.Check, contentDescription = null) }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FilterChip(
                            selected = formState.obesity == 1,
                            onClick = { predictionViewModel.updateForm { it.copy(obesity = if (it.obesity == 1) 0 else 1) } },
                            label = { Text("Obesity / BMI") },
                            leadingIcon = { if (formState.obesity == 1) Icon(Icons.Default.Check, contentDescription = null) }
                        )
                        FilterChip(
                            selected = formState.familyHistory == 1,
                            onClick = { predictionViewModel.updateForm { it.copy(familyHistory = if (it.familyHistory == 1) 0 else 1) } },
                            label = { Text("Family History") },
                            leadingIcon = { if (formState.familyHistory == 1) Icon(Icons.Default.Check, contentDescription = null) }
                        )
                        FilterChip(
                            selected = formState.alcoholUse == 1,
                            onClick = { predictionViewModel.updateForm { it.copy(alcoholUse = if (it.alcoholUse == 1) 0 else 1) } },
                            label = { Text("Alcohol") },
                            leadingIcon = { if (formState.alcoholUse == 1) Icon(Icons.Default.Check, contentDescription = null) }
                        )
                    }
                }
            }

            // 4. Run AI Prediction Submit Button
            Button(
                onClick = {
                    predictionViewModel.runPrediction { response ->
                        response.id?.let { onPredictionResult(it) }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Evaluating ML Risk Engine...", fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Execute AI Cardiac Prediction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text("Select Clinical Patient Preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            predictionViewModel.loadSampleHighRisk()
                            showPresetDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("High Risk Clinical Profile (Age 63, Angina, ST Dep 3.2)")
                    }
                    TextButton(
                        onClick = {
                            predictionViewModel.loadSampleModerateRisk()
                            showPresetDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Moderate Risk Clinical Profile (Age 52, BP 138, ST Dep 1.2)")
                    }
                    TextButton(
                        onClick = {
                            predictionViewModel.loadSampleLowRisk()
                            showPresetDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Low Risk Healthy Profile (Age 34, Normal Vitals)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetDialog = false }) { Text("Cancel") }
            }
        )
    }
}
