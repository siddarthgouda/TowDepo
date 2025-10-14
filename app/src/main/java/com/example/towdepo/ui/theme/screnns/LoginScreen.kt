package com.example.towdepo.ui.theme.screnns

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.towdepo.viewmodels.AuthState
import com.example.towdepo.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Custom Email Text Field
        TextFieldHint(
            value = email,
            onValueChange = { email = it },
            hint = "Email",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Password Text Field
        TextFieldHint(
            value = password,
            onValueChange = { password = it },
            hint = "Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Fixed: Now AuthState is properly imported
        when (authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthState.Error -> {
                Text(
                    text = (authState as AuthState.Error).message,  // Fixed: message is now accessible
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LoginButton(email, password, viewModel)
            }
            is AuthState.Success -> {
                LaunchedEffect(Unit) {
                    onLoginSuccess()
                }
            }
            else -> {
                LoginButton(email, password, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Sign up")
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TextFieldHint(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "hint",
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val showHintAbove by remember {
        derivedStateOf {
            isFocused || value.isNotEmpty()
        }
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        interactionSource = interactionSource,
        visualTransformation = visualTransformation,
        textStyle = textFieldTextStyle(),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        decorationBox = { innerTextField ->
            SharedTransitionLayout {
                AnimatedContent(
                    targetState = showHintAbove,
                    transitionSpec = {
                        EnterTransition.None togetherWith ExitTransition.None
                    },
                    label = "hintAnimation"
                ) { showHintAbove ->
                    Column {
                        Box(Modifier.padding(start = 2.dp)) {
                            InvisibleTextAsPlaceholder(exteriorHintTextStyle())
                            if (showHintAbove) {
                                TextAsIndividualLetters(
                                    animatedContentScope = this@AnimatedContent,
                                    text = hint,
                                    style = exteriorHintTextStyle(),
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = "input_$hint"),
                                    animatedVisibilityScope = this@AnimatedContent
                                )
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = Dp.Hairline,
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isFocused) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .3f)
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (!showHintAbove) {
                                TextAsIndividualLetters(
                                    animatedContentScope = this@AnimatedContent,
                                    text = hint,
                                    style = interiorHintTextStyle(),
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun InvisibleTextAsPlaceholder(style: TextStyle) {
    Text(
        text = "",
        modifier = Modifier.alpha(0f),
        style = style,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.TextAsIndividualLetters(
    animatedContentScope: AnimatedContentScope,
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(),
) {
    Row(modifier) {
        text.forEachIndexed { index, letter ->
            Text(
                text = "$letter",
                modifier = Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "hint_${text}_$index"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        spring(
                            stiffness = 25f * (text.length - index),
                            dampingRatio = Spring.DampingRatioLowBouncy,
                        )
                    }
                ),
                style = style,
            )
        }
    }
}

@ReadOnlyComposable
@Composable
fun textFieldTextStyle() = MaterialTheme.typography.labelLarge.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9f),
)

@ReadOnlyComposable
@Composable
fun exteriorHintTextStyle() = MaterialTheme.typography.labelLarge.copy(
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp,
    color = Color(0xFFFF6D00), // Orange color instead of Sky600
)

@ReadOnlyComposable
@Composable
fun interiorHintTextStyle() = textFieldTextStyle().copy(
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .4f),
)

@Composable
fun LoginButton(email: String, password: String, viewModel: AuthViewModel) {
    Button(
        onClick = { viewModel.login(email, password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = email.isNotEmpty() && password.isNotEmpty()
    ) {
        Text("Login")
    }
}