package com.campusassistant.android.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusassistant.android.ui.common.CampusButton
import com.campusassistant.android.ui.common.CampusCard
import com.campusassistant.android.ui.common.CampusLoadingState
import com.campusassistant.android.ui.common.CampusMessage
import com.campusassistant.android.ui.common.CampusTextField

@Composable
fun LoginScreen(
    loading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit
) {
    var studentId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "校园助手",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        CampusCard {
            Text(
                text = "登录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampusTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = "学号"
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampusTextField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                visualTransformation = PasswordVisualTransformation()
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                CampusMessage(errorMessage, isError = true)
            }
            Spacer(modifier = Modifier.height(18.dp))
            CampusButton(
                text = if (loading) "登录中" else "登录 / 未注册自动创建",
                onClick = { onLogin(studentId, password) },
                enabled = !loading
            )
            if (loading) {
                Spacer(modifier = Modifier.height(14.dp))
                CampusLoadingState(text = "正在登录，必要时会自动注册", modifier = Modifier.height(64.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "默认连接 http://10.0.2.2:8000/gateway/",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
