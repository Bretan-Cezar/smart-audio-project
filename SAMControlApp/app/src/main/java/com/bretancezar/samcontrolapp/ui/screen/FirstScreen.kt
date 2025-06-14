package com.bretancezar.samcontrolapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.bretancezar.samcontrolapp.R
import com.bretancezar.samcontrolapp.ui.theme.Green
import com.bretancezar.samcontrolapp.utils.Screens
import com.bretancezar.samcontrolapp.viewmodel.NavigationViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight

@Composable
fun FirstScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical=100.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Box (

        ) {
            Icon(
                painterResource(R.drawable.headphones_24px),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
            )
            Icon (
                painterResource(id=R.drawable.close_24px),
                contentDescription = null,
                modifier = Modifier
                    .padding(start=85.dp)
                    .align(Alignment.BottomEnd)
                    .size(40.dp),
                tint = Color.Red
            )
        }
        Button(
            onClick = {

            },
            modifier = Modifier
                .padding(20.dp)
                .size(250.dp,50.dp),
            colors = ButtonDefaults.buttonColors(Green)

        ) {
            Text (
                "Connect",
                fontSize = 20.sp,
                modifier = Modifier
                    .wrapContentHeight(Alignment.CenterVertically),
                textAlign = TextAlign.Center

            )
        }

    }
}