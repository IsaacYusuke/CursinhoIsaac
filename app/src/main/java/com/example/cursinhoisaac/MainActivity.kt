package com.example.cursinhoisaac

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EscolinhaApp()
        }
    }
}

@Composable
fun EscolinhaApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "tela1") {
        composable("tela1") {
            Tela1(onIniciarClick = { navController.navigate("tela2") })
        }
        composable("tela2") {
            // Obtém os títulos dos gráficos do arquivo Data.kt
            Tela2(
                listaDeBotoes = graficos.map { it.titulo },
                onBotaoClick = { index -> navController.navigate("grafico/${index - 1}") }
            )
        }
        composable("grafico/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            // Obtém os dados do gráfico correspondente de Data.kt
            val grafico = graficos.getOrNull(index)
            TelaGraficoI(
                estadoJson = grafico?.arquivoJson ?: "arquivo_padrao.json",
                videoUrl = grafico?.videoUrl ?: "https://www.youtube.com/watch?v=dQw4w9WgXcQ" // URL padrão
            )
        }
    }
}


@Composable
fun Tela1(onIniciarClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF2E7D32)) // Cor verde escuro de fundo
    ) {
        // Imagem de fundo
        Image(
            painter = rememberImagePainter(R.drawable.imagem1),
            contentDescription = "Fundo",
            contentScale = ContentScale.FillBounds, // Estica a imagem para ocupar todo o espaço
            modifier = Modifier.fillMaxSize()
        )
        Button(
            onClick = onIniciarClick,
            shape = RectangleShape, // Botão quadrado
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32), // Cor verde
                contentColor = Color.White // Cor do texto
            ),
            border = BorderStroke(3.dp, Color.Black), // Contorno preto
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "Iniciar",
                fontSize = 30.sp // Tamanho da fonte
            )
        }
    }
}

@Composable
fun Tela2(listaDeBotoes: List<String>, onBotaoClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF2E7D32)) // Cor verde escuro de fundo
    ) {
        // Imagem de fundo
        Image(
            painter = rememberImagePainter(R.drawable.imagem1),
            contentDescription = "Fundo",
            contentScale = ContentScale.FillBounds, // Estica a imagem para ocupar todo o espaço
            modifier = Modifier.fillMaxSize()
        )

        // Centralização da LazyColumn
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center // Centraliza o LazyColumn no meio da tela
        ) {
            LazyColumn(
                modifier = Modifier
                    .width(200.dp), // Define largura fixa para os botões
                verticalArrangement = Arrangement.spacedBy(8.dp) // Espaçamento vertical
            ) {
                itemsIndexed(listaDeBotoes) { index, texto ->
                    Button(
                        onClick = { onBotaoClick(index + 1) },
                        shape = RectangleShape, // Botões com formato quadrado
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32), // Cor verde
                            contentColor = Color.White // Cor do texto
                        ),
                        border = BorderStroke(3.dp, Color.Black), // Contorno preto
                        modifier = Modifier
                            .fillMaxWidth() // Ocupa a largura definida pelo LazyColumn
                            .padding(horizontal = 8.dp) // Padding horizontal
                    ) {
                        Text(
                            text = texto,
                            fontSize = 18.sp, // Tamanho da fonte
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelaGraficoI(estadoJson: String, videoUrl: String) {
    val context = LocalContext.current
    val server = remember { LocalWebServer(context) }
    var isServerReady by remember { mutableStateOf(false) } // Estado para controlar a inicialização do servidor

    // Inicializa o servidor de forma assíncrona
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (!server.isAlive) {
                server.start()
                while (!server.isAlive) {
                    delay(100) // Aguarda de forma assíncrona sem bloquear a UI
                }
            }
            isServerReady = true // Marca o servidor como pronto
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // WebView para exibir o gráfico
        if (isServerReady) { // Carrega a WebView apenas quando o servidor estiver pronto
            AndroidView(factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    webViewClient = WebViewClient()
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    loadUrl("http://localhost:12346/grafico.html?json=$estadoJson")
                }
            }, modifier = Modifier.fillMaxSize()) // WebView ocupa a tela inteira
        }

        // Botão no canto inferior direito
        Button(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse(videoUrl) // Define a URL do vídeo
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32), // Cor verde para o botão
                contentColor = Color.White // Cor do texto
            ),
            modifier = Modifier
                .align(Alignment.TopEnd) // Alinha o botão no canto superior direito
                .padding(16.dp) // Padding para afastar da borda
        ) {
            Text(text = "Abrir Vídeo")
        }
    }

    // Stop the server when leaving the composable
    DisposableEffect(Unit) {
        onDispose {
            server.stop()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewTela1() {
    Tela1(onIniciarClick = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewTela2() {
    val listaDeBotoes = listOf("Gráfico 1", "Gráfico 2", "Gráfico 3")
    Tela2(listaDeBotoes = listaDeBotoes, onBotaoClick = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaGraficoI() {
    TelaGraficoI(
        estadoJson = "DaviEGolias.json",
        videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ" // URL padrão
    )
}
