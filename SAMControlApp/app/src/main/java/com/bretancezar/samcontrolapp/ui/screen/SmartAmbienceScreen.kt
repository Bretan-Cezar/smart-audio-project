package com.bretancezar.samcontrolapp.ui.screen

import androidx.annotation.RequiresPermission
import com.bretancezar.samcontrolapp.ui.theme.Red
import com.bretancezar.samcontrolapp.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bretancezar.samcontrolapp.ui.common.ProgressDialog
import com.bretancezar.samcontrolapp.ui.theme.Green
import com.bretancezar.samcontrolapp.utils.Screens
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import com.bretancezar.samcontrolapp.viewmodel.NavigationViewModel
import com.bretancezar.samcontrolapp.viewmodel.SmartAmbienceViewModel

@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun SmartAmbienceScreen(
    smartAmbienceViewModel: SmartAmbienceViewModel,
    navigationViewModel: NavigationViewModel
) {

    val onErrorAction = {
        navigationViewModel.setCurrentScreen(Screens.FIRST_SCREEN)
    }

    val awaitingResponse by smartAmbienceViewModel.awaitingResponse.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 100.dp, 10.dp, 130.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        ModeList(smartAmbienceViewModel, onErrorAction)
        PhrasesCard(smartAmbienceViewModel, onErrorAction)
    }

    if (awaitingResponse) {
        ProgressDialog()
    }
}

@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ModeCard(
    viewModel: SmartAmbienceViewModel,
    mode: SmartAmbienceMode,
    isSelected: Boolean,
    onErrorAction: () -> Unit
) {

    val backgroundColor = if (!isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.secondaryContainer

    Row(
        modifier = Modifier
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable {
                if (!isSelected)
                    viewModel.setSelectedMode(mode, onErrorAction)
            },

        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.2f)
                .fillMaxHeight()
                .background(
                    color = mode.color,
                    shape = RoundedCornerShape(16.dp, 0.dp, 0.dp, 16.dp),
                ),

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Image(
                painter = painterResource(mode.icon),
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(
                mode.displayTitle
            )
            Text (
                mode.description
            )
        }
    }
}

@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ModeList(
    viewModel: SmartAmbienceViewModel,
    onErrorAction: () -> Unit
) {
    val list: List<SmartAmbienceMode> = viewModel.modeList
    val selectedMode by viewModel.selectedMode.collectAsState()

    list.forEach { mode ->
        ModeCard(
            viewModel, mode, selectedMode == mode, onErrorAction
        )
    }
}

@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun PhrasesCard(
    viewModel: SmartAmbienceViewModel,
    onErrorAction: () -> Unit
) {

    val characterLimit = 64

    var fieldText: String by remember {
        mutableStateOf("")
    }

    val phrasesList by viewModel.phraseList.collectAsState()

    Column (
        modifier = Modifier
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(16.dp)

            )
            .padding(10.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Response Activation Phrases")

        Row (
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .padding(10.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(
                value = fieldText,
                onValueChange = {
                    if (it.length <= characterLimit)
                        fieldText = it
                },
                isError = fieldText == "" || (phrasesList.contains(fieldText)),
                singleLine = true,
                textStyle = TextStyle (
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.85f)
                    .horizontalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background),
                placeholder = {
                    Text("Type your phrase")
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Button(
                onClick = {
                    if (fieldText != "" && !phrasesList.contains(fieldText))
                        viewModel.addPhrase(fieldText, onErrorAction)
                },
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(Green),
                contentPadding = PaddingValues(0.dp),
                enabled = phrasesList.size <= 8
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(R.drawable.add_24px),
                    contentDescription = null
                )
            }
        }
        Column (
            modifier = Modifier.padding(10.dp),
        ) {
            phrasesList.forEach {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp)
                ) {
                    Text(
                        it,
                        Modifier.padding(10.dp),
                    )
                    Button(
                        onClick = { viewModel.deletePhrase(it, onErrorAction) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(Red),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.padding(0.dp).size(42.dp)
                    ) {
                        Image(painterResource(
                            R.drawable.remove_24px),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}