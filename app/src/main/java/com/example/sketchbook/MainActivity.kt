package com.example.sketchbook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.example.sketchbook.ui.theme.SketchbookTheme
import kotlin.math.roundToInt
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay




private val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFBB86FC),
    onSurface = Color.Black
)

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {


    // Здесь можно добавить любой контент для загрузочного экрана
    Column(
        modifier = Modifier.fillMaxSize(),
horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.your_image), // Замените на ваше изображение
            contentDescription = "Загрузка",
            modifier = Modifier.size(200.dp) // Установите размер изображения по вашему желанию
        )
        // Например, логотип или текст
        Text("Загрузка...", fontSize = 24.sp)
    }

    // Используем LaunchedEffect для выполнения кода после загрузки
    LaunchedEffect(Unit) {
        // Задержка для имитации загрузки данных
        delay(2000) // 2 секунды
        onSplashComplete() // Завершение загрузки
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                SketchbookTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var isLoading by remember { mutableStateOf(true) }

                        // Отображаем загрузочный экран или основной контент
                        if (isLoading) {
                            SplashScreen { isLoading = false }
                        } else {
                            SketchbookApp() // Ваше основное приложение
                        }
                    }
                }
            }

        }
    }
}

enum class Mode { DRAWING, TEXT }


@Composable
fun DrawerContent(
    onDrawingClick: () -> Unit,
    onTextClick: () -> Unit,
    onClearClick: () -> Unit,
    onColorClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onDrawingClick) { Text("Рисование") }
        Button(onClick = onTextClick) { Text("Текст") }
        Button(onClick = onClearClick) { Text("Очистить") }
        Button(onClick = onColorClick) { Text("Цвет") }
        Button(onClick = onSaveClick) { Text("Сохранить") }
    }
}
fun saveCanvasAsImage(
    context: Context,
    paths: List<Pair<Path, Color>>,
    backgroundColor: Color,
    textElements: List<Pair<String, Offset>> // Список текстов и их позиций
) {
    val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(backgroundColor.toArgb()) // Используйте цвет фона

    // Рисуем пути
    paths.forEach { (path, color) ->
        val paint = android.graphics.Paint().apply {
            this.color = color.toArgb()
            strokeWidth = 8f
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }
        canvas.drawPath(path.asAndroidPath(), paint)
    }

    // Рисуем текст
    textElements.forEach { (text, position) ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 30f
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawText(text, position.x, position.y, paint)
    }

    // Сохранение изображения
    val uri = MediaStore.Images.Media.insertImage(
        context.contentResolver, bitmap, "SketchbookImage", "Sketch created in Sketchbook app"
    )

    if (uri != null) {
        Toast.makeText(context, "Изображение сохранено", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Ошибка при сохранении изображения", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ColorPickerDialog(onColorSelected: (Color) -> Unit) {
    val colors = listOf(
        Color.Red, Color.Green, Color.Blue,
        Color.Yellow, Color.Magenta, Color.Cyan,
        Color.Black, Color.White
    )

    AlertDialog(
        onDismissRequest = { /* Действие при закрытии диалога */ },
        title = { Text("Выберите цвет") }, // Заголовок диалога
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(color)
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { /* Действие при подтверждении */ }) {
                Text("ОК")
            }
        },
        dismissButton = {
            TextButton(onClick = { /* Действие при закрытии */ }) {
                Text("Отмена")
            }
        }
    )
}
@Composable
fun SketchbookApp() {
    var currentMode by remember { mutableStateOf(Mode.DRAWING) }
    var paths by remember { mutableStateOf(listOf<Pair<Path, Color>>()) }
    var currentPath by remember { mutableStateOf(Path()) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var text by remember { mutableStateOf("") }
    var textPosition by remember { mutableStateOf(Offset(100f, 100f)) }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showColorPicker by remember { mutableStateOf(false) }

    var textElements by remember { mutableStateOf(listOf<Pair<String, Offset>>()) }

    if (showColorPicker) {
        ColorPickerDialog { selectedColor ->
            currentColor = selectedColor
            showColorPicker = false // Закрыть палитру после выбора цвета
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onDrawingClick = { currentMode = Mode.DRAWING },
                onTextClick = { currentMode = Mode.TEXT },
                onClearClick = {
                    paths = emptyList()
                    currentPath = Path()
                    text = ""
                    textPosition = Offset(100f, 100f)
                },
                onColorClick = { showColorPicker = true},
                onSaveClick = { saveCanvasAsImage(context, paths, Color.White, textElements) }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(onClick = { scope.launch { drawerState.open() } }) {
                        Text("Меню")
                    }
                }

                if (currentMode == Mode.TEXT) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(fontSize = 30.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    LaunchedEffect(text) {
                        if (text.isNotEmpty()) {
                            textElements = listOf(Pair(text, textPosition))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .pointerInput(currentMode) {
                            if (currentMode == Mode.DRAWING) {
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
                                        currentPath = Path()
                                    }
                                )
                            }
                        }
                ) {
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

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    textPosition.x.roundToInt(),
                                    textPosition.y.roundToInt()
                                )
                            }
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
                            fontSize = 30.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
        }}


