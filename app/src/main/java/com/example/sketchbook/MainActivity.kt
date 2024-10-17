package com.example.sketchbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.example.sketchbook.ui.theme.SketchbookTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SketchbookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SketchbookApp()
                }
            }
        }
    }
}

@Composable
fun SketchbookApp() {
    var isDrawingMode by remember { mutableStateOf(true) }
    var isTextMode by remember { mutableStateOf(false) }
    var paths by remember { mutableStateOf(listOf<Pair<Path, Color>>()) }
    var currentPath by remember { mutableStateOf(Path()) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var text by remember { mutableStateOf("Введите текст") }
    var textPosition by remember { mutableStateOf(Offset(100f, 100f)) }
    var isDraggingText by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(onClick = {
                isDrawingMode = true
                isTextMode = false
            }) {
                Text("Рисование")
            }
            Button(onClick = {
                isDrawingMode = false
                isTextMode = true
            }) {
                Text("Текст")
            }
            Button(onClick = {
                paths = emptyList()
                currentPath = Path()
                text = "Введите текст"
                textPosition = Offset(100f, 100f)
            }) {
                Text("Очистить")
            }
        }

        if (isTextMode) {
            // Ввод текста в режиме текстового поля
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(fontSize = 24.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Canvas для рисования
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(isDrawingMode) {
                    if (isDrawingMode) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath.moveTo(offset.x, offset.y)
                            },
                            onDrag = { change, _ ->
                                currentPath.lineTo(change.position.x, change.position.y)
                                change.consume()
                            },
                            onDragEnd = {
                                paths = paths + (currentPath to currentColor)
                                currentPath = Path() // Сброс текущего пути
                            }
                        )
                    }
                }
        ) {
            // Рисуем сохранённые пути
            Canvas(modifier = Modifier.fillMaxSize()) {
                paths.forEach { (path, color) ->
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = 8f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Рисуем текущий путь
                drawPath(
                    path = currentPath,
                    color = currentColor,
                    style = Stroke(
                        width = 8f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Текст, который можно перетаскивать
            Box(
                modifier = Modifier
                    .offset { IntOffset(textPosition.x.roundToInt(), textPosition.y.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                textPosition += change.positionChange()
                            }
                        )
                    }
            ) {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }
        }
    }
}
