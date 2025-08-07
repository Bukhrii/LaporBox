package com.pillbox.laporbox.presentation.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.pillbox.laporbox.R
import com.pillbox.laporbox.presentation.ui.theme.BrandBlue
import com.pillbox.laporbox.presentation.ui.theme.CardBlue
import com.pillbox.laporbox.presentation.ui.theme.TextHeading
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onSignUpSuccessToLogin: () -> Unit,
    onNavigateToLogin: () -> Unit

) {

    var textEmail by remember { mutableStateOf("") }
    var textPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var textPasswordConfirm by remember { mutableStateOf("") }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var textUsername by remember { mutableStateOf("") }
    var textPhoneNumber by remember { mutableStateOf("") }

    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            BrandBlue.copy(alpha = 0.95f),
            Color.White
        )
    )

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.RegistrationSuccess -> {
                Toast.makeText(context, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                onSignUpSuccessToLogin()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.authbackground),
                contentDescription = "Onboarding Background",
                modifier = Modifier.fillMaxWidth(),
            )

            ElevatedCard(
                colors = CardDefaults.cardColors(CardBlue, contentColor = CardBlue
                ),
                shape = RoundedCornerShape(topEnd = 40.dp, topStart = 40.dp),
                elevation = CardDefaults.elevatedCardElevation(16.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {

                Spacer(modifier = Modifier.padding(16.dp))

                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.username),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = textUsername,
                        onValueChange = { textUsername = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = stringResource(id = R.string.username),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        label = { Text("Ketikkan nama anda") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.padding(6.dp))

                    Text(
                        text = stringResource(R.string.phone_number),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = textPhoneNumber,
                        onValueChange = { textPhoneNumber = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = stringResource(id = R.string.phone_number),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        label = { Text("Ketikkan nomor telpon") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.padding(6.dp))

                    Text(
                        text = stringResource(R.string.email),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = textEmail,
                        onValueChange = { textEmail = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = stringResource(id = R.string.email),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        label = { Text("Ketikkan nama email") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.padding(6.dp))

                    Text(
                        text = stringResource(R.string.password),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = textPassword,
                        onValueChange = { textPassword = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = stringResource(id = R.string.password),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        label = { Text("Ketikkan kata sandi") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                            val description = if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, description, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.padding(6.dp))

                    Text(
                        text = stringResource(R.string.password_confirm),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = textPasswordConfirm,
                        onValueChange = { textPasswordConfirm = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = stringResource(id = R.string.password_confirm),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        label = { Text("Konfirmasi kata sandi") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordConfirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                            val description = if (passwordConfirmVisible) "Hide password" else "Show password"

                            IconButton(onClick = { passwordConfirmVisible = !passwordConfirmVisible }) {
                                Icon(imageVector = image, description, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.padding(14.dp))

                    ElevatedButton(
                        onClick = {
                            authViewModel.signup(
                                username = textUsername,
                                phoneNumber = textPhoneNumber,
                                email = textEmail,
                                password = textPassword,
                                confirmPassword = textPasswordConfirm
                            )
                        },
                        elevation = ButtonDefaults.buttonElevation(12.dp),
                        enabled = authState !is AuthState.Loading,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 90.dp)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.sign_up),
                                color = Color.Black,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(14.dp))

                    Row(horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.already_have_account),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = {
                                onNavigateToLogin()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.sign_in),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}