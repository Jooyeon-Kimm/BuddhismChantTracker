import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.app.practice.buddhismchanttracker.ui.settings.EmailAuthMode
import com.app.practice.buddhismchanttracker.ui.settings.SettingsUiState
import com.app.practice.buddhismchanttracker.R
import com.app.practice.buddhismchanttracker.ui.settings.GoogleLoginButton
import com.app.practice.buddhismchanttracker.ui.settings.KakaoLoginButton
import com.app.practice.buddhismchanttracker.ui.settings.SocialImageButton

@Composable
fun AccountCard(
    ui: SettingsUiState,
    onSignInKakao: () -> Unit,
    onSignInGoogle: () -> Unit,
    onSignInFirebase: (String, String) -> Unit,
    onSignUpFirebase: (String, String) -> Unit,
    onSignOut: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "계정 정보",
                style = MaterialTheme.typography.titleMedium
            )

            if (ui.loggedIn) {
                // ----------------- 로그인 상태 -----------------
                Text("이름: ${ui.userName ?: "-"}")
                Text("이메일: ${ui.userEmail ?: "-"}")
                Text("로그인 제공자: ${ui.providerLabel ?: "-"}")

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onSignOut) {
                    Text("로그아웃")
                }
            } else {
                // ----------------- 로그아웃 상태 -----------------
                Text("로그인되어 있지 않습니다.")

                Spacer(Modifier.height(12.dp))

                // 1) 카카오 / Google 로그인 아이콘 버튼
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KakaoLoginButton(
                        onClick = onSignInKakao
                    )

                    GoogleLoginButton(
                        onClick = onSignInGoogle
                    )
                }


                Spacer(Modifier.height(16.dp))

                // 2) 이메일 로그인 / 이메일 회원가입 토글 버튼
                var emailAuthMode by remember { mutableStateOf(EmailAuthMode.NONE) }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            emailAuthMode = if (emailAuthMode == EmailAuthMode.LOGIN) {
                                EmailAuthMode.NONE
                            } else {
                                EmailAuthMode.LOGIN
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text("이메일 로그인")
                    }

                    Button(
                        onClick = {
                            emailAuthMode = if (emailAuthMode == EmailAuthMode.SIGNUP) {
                                EmailAuthMode.NONE
                            } else {
                                EmailAuthMode.SIGNUP
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text("이메일 회원가입")
                    }
                }

                // 3) 선택된 모드에 따라 이메일/비밀번호 입력 폼 표시
                if (emailAuthMode != EmailAuthMode.NONE) {
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("이메일") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("비밀번호") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (emailAuthMode == EmailAuthMode.LOGIN) {
                                onSignInFirebase(email, password)
                            } else {
                                onSignUpFirebase(email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when (emailAuthMode) {
                                EmailAuthMode.LOGIN -> "로그인"
                                EmailAuthMode.SIGNUP -> "회원가입"
                                EmailAuthMode.NONE -> ""
                            }
                        )
                    }
                }

                // 4) (선택) 에러 / 성공 메시지 표시
                ui.lastAuthError?.let { msg ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                ui.lastAuthMessage?.let { msg ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
