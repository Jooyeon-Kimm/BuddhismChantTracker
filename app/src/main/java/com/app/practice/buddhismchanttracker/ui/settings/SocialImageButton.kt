package com.app.practice.buddhismchanttracker.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import com.app.practice.buddhismchanttracker.R

@Composable
fun SocialImageButton(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    text: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = if (borderColor == Color.Transparent) 0.dp else 1.dp,
            color = borderColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(text)
        }
    }
}
@Composable
fun KakaoLoginButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SocialImageButton(
        modifier = modifier,
        iconRes = R.drawable.ic_kakao,
        text = "카카오 로그인",
        containerColor = Color(0xFFFEE500),
        contentColor = Color(0xFF000000),
        borderColor = Color.Transparent,
        onClick = onClick
    )
}

@Composable
fun GoogleLoginButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SocialImageButton(
        modifier = modifier,
        iconRes = R.drawable.ic_google,
        text = "구글 로그인",
        containerColor = Color.White,
        contentColor = Color(0xFF000000),
        borderColor = Color.Transparent,
        onClick = onClick
    )
}
