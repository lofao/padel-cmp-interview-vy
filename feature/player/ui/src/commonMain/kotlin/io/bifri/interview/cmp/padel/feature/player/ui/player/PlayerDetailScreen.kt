@file:Suppress("TooManyFunctions")

package io.bifri.interview.cmp.padel.feature.player.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.bifri.interview.cmp.padel.core.designsystem.component.appbar.CollapsingTopAppBar
import io.bifri.interview.cmp.padel.core.designsystem.component.avatar.PlayerAvatar
import io.bifri.interview.cmp.padel.core.designsystem.component.button.BackIconButton
import io.bifri.interview.cmp.padel.core.designsystem.component.placeholder.ShimmerBox
import io.bifri.interview.cmp.padel.core.designsystem.component.pulltorefresh.AppPullToRefreshBox
import io.bifri.interview.cmp.padel.core.designsystem.component.scaffold.AppScaffold
import io.bifri.interview.cmp.padel.core.designsystem.theme.AppTheme
import io.bifri.interview.cmp.padel.core.designsystem.theme.dimens
import io.bifri.interview.cmp.padel.core.ui.Event
import io.bifri.interview.cmp.padel.core.ui.LifecycleCollectEffect
import io.bifri.interview.cmp.padel.feature.player.domain.model.Player
import io.bifri.interview.cmp.padel.feature.player.ui.PlayerUiRes
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailNavigationAction.ShowError
import io.bifri.interview.cmp.padel.feature.player.ui.player.PlayerDetailState.UiState
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_age
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_birthplace
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_country
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_elo
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_gender
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_hand
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_height
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_matches_played
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_matches_won
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_points
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_position
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_rank
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_stats_title
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_title
import io.bifri.interview.cmp.padel.feature.player.ui.player_detail_win_rate
import io.bifri.interview.cmp.padel.feature.player.ui.player_not_found
import io.bifri.interview.cmp.padel.feature.player.ui.players_avatar_cd
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val PlayerHeaderAvatarSize = 120.dp
private val ShimmerHeaderHeight = 200.dp
private val ShimmerCardHeight = 150.dp
private const val PercentageMultiplier = 100

@Composable
fun PlayerDetailRoute(
    playerId: Long,
    onBackClick: () -> Unit,
    onError: (Throwable) -> Unit,
    viewModel: PlayerDetailViewModel = koinViewModel {
        parametersOf(playerId)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(null)

    PlayerDetailScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onBackClick = onBackClick,
    )

    LifecycleCollectEffect(
        stream = viewModel.events,
    ) {
        processEvents(
            event = it,
            onError = onError,
        )
    }
}

@Composable
private fun PlayerDetailScreen(
    uiState: UiState?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    if (uiState == null) return

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
    )

    AppScaffold(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        topBar = {
            CollapsingTopAppBar(
                title = { Text(uiState.player?.name ?: stringResource(PlayerUiRes.string.player_detail_title)) },
                navigationIcon = { BackIconButton(onClick = onBackClick) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val statusBarTopPadding = TopAppBarDefaults.windowInsets.asPaddingValues().calculateTopPadding()
        val columnTopPadding = maxOf(innerPadding.calculateTopPadding(), statusBarTopPadding)

        AppPullToRefreshBox(
            isRefreshing = uiState.isDataLoading,
            onRefresh = onRefresh,
            topPadding = columnTopPadding,
        ) {
            val contentPadding = PaddingValues(
                start = dimens.margin,
                end = dimens.margin,
                top = dimens.spacingSmall,
                bottom = innerPadding.calculateBottomPadding() + dimens.margin,
            )

            val scrollableModifier = Modifier
                .fillMaxSize()
                .padding(top = columnTopPadding)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)

            when {
                uiState.isDataLoading && (uiState.player == null) -> LoadingPlayerDetailContent(
                    modifier = scrollableModifier,
                )

                uiState.player == null -> EmptyPlayerDetailContent(
                    modifier = scrollableModifier,
                )

                else -> PlayerDetailContent(
                    player = uiState.player,
                    modifier = scrollableModifier,
                )
            }
        }
    }
}

@Composable
private fun LoadingPlayerDetailContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShimmerHeaderHeight)
                .clip(MaterialTheme.shapes.medium),
        )
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShimmerCardHeight)
                .clip(MaterialTheme.shapes.medium),
        )
    }
}

@Composable
private fun EmptyPlayerDetailContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(PlayerUiRes.string.player_not_found),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PlayerDetailContent(
    player: Player,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
    ) {
        PlayerHeader(player = player)
        PlayerInformationCard(player = player)
        if (hasStatistics(player)) {
            PlayerStatisticsCard(player = player)
        }
    }
}

@Composable
private fun PlayerHeader(
    player: Player,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.spacingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
    ) {
        PlayerAvatar(
            photoUrl = player.photoUrl,
            contentDescription = stringResource(PlayerUiRes.string.players_avatar_cd, player.name),
            modifier = Modifier.size(PlayerHeaderAvatarSize),
        )
        Text(
            text = player.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        player.ranking?.let { rank ->
            Text(
                text = stringResource(PlayerUiRes.string.player_detail_rank, rank),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PlayerInformationCard(
    player: Player,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(dimens.cardPaddingMedium),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
        ) {
            DetailField(
                label = PlayerUiRes.string.player_detail_country,
                value = player.country,
            )
            DetailField(
                label = PlayerUiRes.string.player_detail_gender,
                value = player.gender?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            )
            DetailField(
                label = PlayerUiRes.string.player_detail_position,
                value = player.position?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            )
            DetailField(
                label = PlayerUiRes.string.player_detail_hand,
                value = player.hand?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            )
            DetailField(
                label = PlayerUiRes.string.player_detail_age,
                value = player.age,
            )
            DetailField(
                label = PlayerUiRes.string.player_detail_birthplace,
                value = player.birthplace,
            )
            DetailField(
                label = PlayerUiRes.string.player_detail_height,
                value = player.height?.let { "$it cm" },
            )
        }
    }
}

@Composable
private fun PlayerStatisticsCard(
    player: Player,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(dimens.cardPaddingMedium),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
        ) {
            Text(
                text = stringResource(PlayerUiRes.string.player_detail_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            player.points?.let { points ->
                DetailField(
                    label = PlayerUiRes.string.player_detail_points,
                    value = points.toString(),
                )
            }
            player.elo?.let { elo ->
                DetailField(
                    label = PlayerUiRes.string.player_detail_elo,
                    value = elo.toString(),
                )
            }
            player.matchesPlayed?.let { matches ->
                DetailField(
                    label = PlayerUiRes.string.player_detail_matches_played,
                    value = matches.toString(),
                )
            }
            player.matchesWon?.let { won ->
                DetailField(
                    label = PlayerUiRes.string.player_detail_matches_won,
                    value = won.toString(),
                )
            }
            val winRate = calculateWinRate(player.matchesWon, player.matchesPlayed)
            winRate?.let { rate ->
                DetailField(
                    label = PlayerUiRes.string.player_detail_win_rate,
                    value = "$rate%",
                )
            }
        }
    }
}

@Composable
private fun DetailField(
    label: StringResource,
    value: String?,
) {
    value ?: return

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingExtraSmall),
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun hasStatistics(player: Player): Boolean =
    player.points != null || player.elo != null || player.matchesPlayed != null || player.matchesWon != null

private fun calculateWinRate(won: Int?, total: Int?): Int? {
    if (won == null || total == null || total == 0) return null
    return (won * PercentageMultiplier) / total
}

private fun processEvents(
    event: Event<PlayerDetailNavigationAction>,
    onError: (Throwable) -> Unit,
) {
    val action = event.getContentIfNotHandled() ?: return
    when (action) {
        is ShowError -> onError(action.error)
    }
}

@Preview(name = "Player Detail")
@Composable
private fun PlayerDetailScreenPreview() {
    AppTheme {
        PlayerDetailScreen(
            uiState = UiState(
                player = Player(
                    id = 1L,
                    name = "Arturo Coello",
                    ranking = 1,
                    country = "ES",
                    gender = "male",
                    photoUrl = null,
                    position = "Drive",
                    matchesPlayed = 98,
                    matchesWon = 88,
                    points = 14200,
                ),
            ),
            onRefresh = {},
        )
    }
}
