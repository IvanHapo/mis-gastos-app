package com.example.primerintentodeaplicacionsolo

import android.R.attr.label
import android.R.attr.onClick
import android.R.attr.text
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log.i
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primerintentodeaplicacionsolo.ui.theme.PrimerIntentoDeAplicacionSoloTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrimerIntentoDeAplicacionSoloTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Hapo",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class Gasto(
    val descripcion: String,
    val monto: Double
)

@SuppressLint("DefaultLocale")
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dataStore = remember { GastosDataStore(context) }
    val scope = rememberCoroutineScope()

    var totalGastos by remember { mutableStateOf(0.0) }
    var montoActual by remember { mutableStateOf("") }
    var descripcionActual by remember { mutableStateOf("") }
    var listaGastos by remember { mutableStateOf(listOf<Gasto>()) }

    LaunchedEffect(Unit) {
        dataStore.gastosFlow.collect { gastosGuardados ->
            listaGastos = gastosGuardados
            totalGastos = gastosGuardados.sumOf { it.monto }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = "Mis Gastos",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Total: $${String.format("%.2f", totalGastos)}",
            fontSize = 24.sp
        )

        OutlinedTextField(
            value = montoActual,
            onValueChange = { montoActual = it },
            label = { Text("Monto del gasto") },
            placeholder = { Text("150.50") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = descripcionActual,
            onValueChange = { descripcionActual = it },
            label = { Text("Descripción del gasto") },
            placeholder = { Text("Ej: Supermercado, Transporte, Gym...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val monto = montoActual.toDoubleOrNull()
            if (monto != null && monto > 0 && descripcionActual.isNotBlank()) {
                val nuevoGasto = Gasto(descripcionActual, monto)
                listaGastos = listaGastos + nuevoGasto
                totalGastos += monto
                montoActual = ""
                descripcionActual = ""

                scope.launch {
                    dataStore.guardarGastos(listaGastos)
                }
            }
        }) {
            Text("Agregar Gasto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Historial de Gastos: ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(listaGastos.size) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        text = "${index + 1}. ${listaGastos[index].descripcion} - $${
                            String.format(
                                "%.2f",
                                listaGastos[index].monto
                            )
                        }",
                        fontSize = 16.sp
                    )

                    IconButton(onClick = {
                        listaGastos = listaGastos.filterIndexed { i, _ -> i != index }
                        totalGastos = listaGastos.sumOf { it.monto }

                        scope.launch {
                            dataStore.guardarGastos(listaGastos)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar gasto",
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                totalGastos = 0.0
                listaGastos = listOf<Gasto>()

                scope.launch {
                    dataStore.guardarGastos(listOf())
                }
            }
        ) {
            Text("Limpiar Todo")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrimerIntentoDeAplicacionSoloTheme {
        Greeting("Android")
    }
}