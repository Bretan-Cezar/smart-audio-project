package com.bretancezar.samcontrolapp.ui.screen

import android.graphics.Color
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bretancezar.samcontrolapp.ui.theme.Green
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import com.bretancezar.samcontrolapp.viewmodel.SmartAmbienceViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SmartAmbienceScreen(
    viewModel: SmartAmbienceViewModel
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 100.dp, 10.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        ModeList(viewModel)
        PhrasesCard(viewModel)
    }
}

@Composable
fun ModeCard(
    viewModel: SmartAmbienceViewModel,
    mode: SmartAmbienceMode,
    isSelected: Boolean
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
                    viewModel.setSelectedMode(mode)
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

@Composable
fun ModeList(
    viewModel: SmartAmbienceViewModel
) {
    val list: List<SmartAmbienceMode> = viewModel.modeList
    val selectedMode by viewModel.selectedMode.collectAsState()

    list.forEach { mode ->
        ModeCard(
            viewModel, mode, selectedMode == mode
        )
    }
}

@Composable
fun PhrasesCard(
    viewModel: SmartAmbienceViewModel
) {

    var fieldText: String by remember {
        mutableStateOf("")
    }

    val phrasesList by viewModel.phraseList.collectAsState()

    Column (

    ) {
        Text("Response Activation Phrases")

        Row (
            modifier = Modifier
                .height(60.dp)
        ) {
            TextField(
                value = fieldText,
                onValueChange = { fieldText = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.80f)
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
                    viewModel.addPhrase(fieldText)
                },
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(Green),

            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(R.drawable.add_24px),
                    contentDescription = null
                )
            }
        }
        Column (

        ) {
            phrasesList.forEach {
                Text (
                    it
                )
            }
        }
    }
}