package com.lsp.callguard.ui.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    val isSubscribed = false

    var blockUnknown by remember { mutableStateOf(true) }
    var blockPrivateNumbers by remember { mutableStateOf(true) }
    var useContactsAutomatically by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                SettingsHeader(onBack = onBack)
            }

            item {
                SettingsSectionTitle("Conta e assinatura")
            }

            item {
                SubscriptionCard(
                    isSubscribed = isSubscribed,
                    onOpenPaywall = onOpenPaywall
                )
            }

            item {
                SettingsSectionTitle("Proteção de chamadas")
            }

            item {
                ProtectionSettingsCard(
                    blockUnknown = blockUnknown,
                    onBlockUnknownChange = { blockUnknown = it },
                    blockPrivateNumbers = blockPrivateNumbers,
                    onBlockPrivateNumbersChange = { blockPrivateNumbers = it }
                )
            }

            item {
                SettingsSectionTitle("Whitelist e contatos")
            }

            item {
                ContactsSettingsCard(
                    isSubscribed = isSubscribed,
                    useContactsAutomatically = useContactsAutomatically,
                    onUseContactsAutomaticallyChange = { useContactsAutomatically = it },
                    onOpenPaywall = onOpenPaywall
                )
            }

            item {
                SettingsSectionTitle("Privacidade")
            }

            item {
                PrivacySettingsCard(
                    onOpenTerms = onOpenTerms,
                    onOpenPrivacyPolicy = onOpenPrivacyPolicy
                )
            }

            item {
                SettingsSectionTitle("Aplicativo")
            }

            item {
                AppSettingsCard(
                    onOpenLanguage = onOpenLanguage
                )
            }

            item {
                AppInfoCard()
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Configurações",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Controle privacidade, proteção e assinatura.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SubscriptionCard(
    isSubscribed: Boolean,
    onOpenPaywall: () -> Unit
) {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.WorkspacePremium,
            title = if (isSubscribed) "CallGuard Premium" else "Plano gratuito",
            description = if (isSubscribed) {
                "Sua assinatura está ativa."
            } else {
                "Você está usando recursos básicos com limite de whitelist."
            }
        )

        if (!isSubscribed) {
            Spacer(modifier = Modifier.height(14.dp))

            FilledTonalButton(
                onClick = onOpenPaywall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Ver Premium")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoText(
            icon = Icons.Outlined.CreditCard,
            text = "A assinatura libera contatos automáticos, whitelist ilimitada e futura recuperação de dados."
        )
    }
}

@Composable
private fun ProtectionSettingsCard(
    blockUnknown: Boolean,
    onBlockUnknownChange: (Boolean) -> Unit,
    blockPrivateNumbers: Boolean,
    onBlockPrivateNumbersChange: (Boolean) -> Unit
) {
    SettingsCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Security,
            title = "Bloquear números desconhecidos",
            description = "Bloqueia chamadas que não estejam nos contatos ou na whitelist.",
            checked = blockUnknown,
            onCheckedChange = onBlockUnknownChange
        )

        SettingsDivider()

        SettingsSwitchRow(
            icon = Icons.Outlined.Block,
            title = "Bloquear números privados",
            description = "Impede chamadas ocultas ou sem identificação.",
            checked = blockPrivateNumbers,
            onCheckedChange = onBlockPrivateNumbersChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoText(
            icon = Icons.Outlined.Info,
            text = "Essas opções serão aplicadas quando a proteção por chamadas estiver configurada."
        )
    }
}

@Composable
private fun ContactsSettingsCard(
    isSubscribed: Boolean,
    useContactsAutomatically: Boolean,
    onUseContactsAutomaticallyChange: (Boolean) -> Unit,
    onOpenPaywall: () -> Unit
) {
    SettingsCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Lock,
            title = "Usar contatos automaticamente",
            description = if (isSubscribed) {
                "Permite considerar seus contatos como números confiáveis."
            } else {
                "Disponível apenas no Premium. Nenhuma permissão será solicitada no plano gratuito."
            },
            checked = useContactsAutomatically,
            enabled = isSubscribed,
            onCheckedChange = onUseContactsAutomaticallyChange
        )

        if (!isSubscribed) {
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onOpenPaywall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text("Desbloquear contatos com Premium")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoText(
            icon = Icons.Outlined.PrivacyTip,
            text = "O acesso aos contatos só será solicitado após assinatura e consentimento explícito."
        )
    }
}

@Composable
private fun PrivacySettingsCard(
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.VerifiedUser,
            title = "Privacidade primeiro",
            description = "O CallGuard não exibe anúncios e não monetiza seus dados."
        )

        SettingsDivider()

        SettingsActionRow(
            icon = Icons.Outlined.Policy,
            title = "Termos de uso",
            description = "Leia as regras de uso do aplicativo.",
            onClick = onOpenTerms
        )

        SettingsDivider()

        SettingsActionRow(
            icon = Icons.Outlined.PrivacyTip,
            title = "Política de privacidade",
            description = "Entenda como seus dados e permissões são tratados.",
            onClick = onOpenPrivacyPolicy
        )
    }
}

@Composable
private fun AppSettingsCard(
    onOpenLanguage: () -> Unit
) {
    SettingsCard {
        SettingsActionRow(
            icon = Icons.Outlined.Language,
            title = "Idioma",
            description = "Alterar idioma do aplicativo.",
            onClick = onOpenLanguage
        )

        SettingsDivider()

        SettingsItemHeader(
            icon = Icons.Outlined.NotificationsOff,
            title = "Sem publicidade",
            description = "Mesmo na versão gratuita, o app não exibirá anúncios."
        )
    }
}

@Composable
private fun AppInfoCard() {
    SettingsCard {
        SettingsItemHeader(
            icon = Icons.Outlined.Settings,
            title = "CallGuard",
            description = "Versão 1.0.0"
        )
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon = icon)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIcon(icon = icon)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsItemHeader(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        SettingsIcon(icon = icon)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsInfoText(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsIcon(
    icon: ImageVector
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = Modifier.size(38.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(14.dp))
}