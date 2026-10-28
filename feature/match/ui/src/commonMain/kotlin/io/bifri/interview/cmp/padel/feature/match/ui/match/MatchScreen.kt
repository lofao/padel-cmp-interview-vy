package io.bifri.interview.cmp.padel.feature.match.ui.match

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
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
import io.bifri.interview.cmp.padel.feature.match.domain.model.Match
import io.bifri.interview.cmp.padel.feature.match.domain.model.Match.SetScore
import io.bifri.interview.cmp.padel.feature.match.domain.model.Match.Team
import io.bifri.interview.cmp.padel.feature.match.domain.model.Match.TeamIndex.TeamOne
import io.bifri.interview.cmp.padel.feature.match.domain.model.Match.TeamIndex.TeamTwo
import io.bifri.interview.cmp.padel.feature.match.ui.MatchUiRes
import io.bifri.interview.cmp.padel.feature.match.ui.component.SetColumnWidth
import io.bifri.interview.cmp.padel.feature.match.ui.component.SetScoreCell
import io.bifri.interview.cmp.padel.feature.match.ui.match.MatchNavigationAction.ShowError
import io.bifri.interview.cmp.padel.feature.match.ui.match.MatchState.UiState
import io.bifri.interview.cmp.padel.feature.match.ui.match_court
import io.bifri.interview.cmp.padel.feature.match.ui.match_court_of_total
import io.bifri.interview.cmp.padel.feature.match.ui.match_player_avatar_cd
import io.bifri.interview.cmp.padel.feature.match.ui.match_sets
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val PlayerAvatarSize = 48.dp

@Composable
fun MatchRoute(
    matchId: Long,
    tournamentName: String,
    totalCourts: Int?,
    onBackClick: () -> Unit,
    onError: (Throwable) -> Unit,
    onPlayerClick: (Long) -> Unit = {},
    viewModel: MatchViewModel = koinViewModel {
        parametersOf(matchId, tournamentName, totalCourts)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(null)

    MatchScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRefresh = viewModel::onRefresh,
        onPlayerClick = onPlayerClick,
    )

    LifecycleCollectEffect(
        stream = viewModel.events,
        collector = { processEvents(it, onError) },
    )
}

@Composable
private fun MatchScreen(
    uiState: UiState?,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayerClick: (Long) -> Unit = {},
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
                title = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimens.spacingExtraSmall),
                    ) {
                        Text(text = uiState.match?.roundName.orEmpty())
                        Text(
                            text = uiState.tournamentName.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = { BackIconButton(onClick = onBackClick) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        AppPullToRefreshBox(
            isRefreshing = uiState.isDataLoading,
            onRefresh = onRefresh,
            topPadding = innerPadding.calculateTopPadding(),
        ) {
            val contentPadding = innerPadding.plus(PaddingValues(all = dimens.margin))

            MatchContent(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
                match = uiState.match,
                totalCourts = uiState.totalCourts,
                onPlayerClick = onPlayerClick,
            )
        }
    }
}

@Composable
private fun MatchContent(
    match: Match?,
    totalCourts: Int?,
    onPlayerClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
    ) {
        if (match != null) {
            Court(
                courtName = match.courtName,
                totalCourts = totalCourts,
            )
            ScoreboardCard(
                match = match,
                onPlayerClick = onPlayerClick,
            )
        }
    }
}

@Composable
private fun Court(
    courtName: String?,
    totalCourts: Int?,
) {
    val courtLabel = when {
        courtName != null && totalCourts != null ->
            stringResource(MatchUiRes.string.match_court_of_total, courtName, totalCourts)

        courtName != null -> courtName

        else -> null
    }
    if (courtLabel != null) {
        Column {
            Text(
                text = stringResource(MatchUiRes.string.match_court),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = courtLabel)
        }
    }
}

@Composable
private fun ScoreboardCard(
    match: Match,
    onPlayerClick: (Long) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(dimens.cardPaddingMedium),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
            ) {
                Spacer(Modifier.width(PlayerAvatarSize))
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(MatchUiRes.string.match_sets),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    match.scores.forEach { setScore ->
                        Box(
                            modifier = Modifier.width(SetColumnWidth),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = setScore.setNumber.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            TeamScoreRow(
                team = match.teamOne,
                setGames = match.scores.map { it.teamOneGames },
                isWinner = match.winnerTeam == TeamOne,
                onPlayerClick = onPlayerClick,
            )
            HorizontalDivider()
            TeamScoreRow(
                team = match.teamTwo,
                setGames = match.scores.map { it.teamTwoGames },
                isWinner = match.winnerTeam == TeamTwo,
                onPlayerClick = onPlayerClick,
            )
        }
    }
}

@Composable
private fun TeamScoreRow(
    team: Team?,
    setGames: List<String?>,
    isWinner: Boolean,
    onPlayerClick: (Long) -> Unit,
) {
    team ?: return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
    ) {
        team.players.forEachIndexed { index, player ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
            ) {
                val isFirstPlayer = index == 0
                val fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
                val color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                val playerId = player.id
                val playerClickModifier = if (playerId != null) {
                    Modifier.clickable { onPlayerClick(playerId) }
                } else {
                    Modifier
                }

                Row(
                    modifier = playerClickModifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSmall),
                ) {
                    PlayerAvatar(
                        photoUrl = player.playerPhotoUrl,
                        contentDescription = stringResource(MatchUiRes.string.match_player_avatar_cd, player),
                        modifier = Modifier.size(PlayerAvatarSize),
                    )
                    Text(
                        text = player.name.orEmpty(),
                        fontWeight = fontWeight,
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (isFirstPlayer) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        setGames.forEach { games ->
                            SetScoreCell(
                                score = games,
                                fontWeight = fontWeight,
                                color = color,
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(SetColumnWidth * setGames.size))
                }
            }
        }
    }
}

private fun processEvents(event: Event<MatchNavigationAction>, onError: (Throwable) -> Unit) {
    val action = event.getContentIfNotHandled() ?: return
    when (action) {
        is ShowError -> onError(action.error)
    }
}

@Preview(name = "Match")
@Composable
private fun MatchScreenPreview() {
    AppTheme {
        MatchScreen(
            uiState = UiState(
                match = previewMatch(),
                tournamentName = "Madrid P1",
                totalCourts = 8,
            ),
            onBackClick = {},
            onRefresh = {},
        )
    }
}

@Preview(name = "Loading match")
@Composable
private fun MatchScreenLoadingPreview() {
    AppTheme {
        MatchScreen(
            uiState = UiState(
                isDataLoading = true,
                tournamentName = "Madrid P1",
                totalCourts = 8,
            ),
            onBackClick = {},
            onRefresh = {},
        )
    }
}

private fun previewMatch() = Match(
    id = 1L,
    courtName = "Center Court",
    roundName = "Semifinals",
    teamOne = Team(
        players = listOf(
            Team.Player(
                name = "Ozols",
                playerPhotoUrl = null,
            ),
            Team.Player(
                name = "Kalnins",
                playerPhotoUrl = null,
            ),
        ),
    ),
    teamTwo = Team(
        players = listOf(
            Team.Player(
                name = "Garcia",
                playerPhotoUrl = null,
            ),
            Team.Player(
                name = "Silva",
                playerPhotoUrl = null,
            ),
        ),
    ),
    scores = listOf(
        SetScore(
            setNumber = 1,
            teamOneGames = "6",
            teamTwoGames = "4",
        ),
        SetScore(
            setNumber = 2,
            teamOneGames = "6(5)",
            teamTwoGames = "7",
        ),
        SetScore(
            setNumber = 3,
            teamOneGames = "7",
            teamTwoGames = "6(4)",
        ),
    ),
    winnerTeam = TeamOne,
)
