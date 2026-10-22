package io.bifri.interview.cmp.padel.core.designsystem.component.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import io.bifri.interview.cmp.padel.core.designsystem.component.placeholder.ShimmerBox
import io.bifri.interview.cmp.padel.core.designsystem.icon.AppIcon

private const val IconSizeFraction = 0.6f

@Composable
fun PlayerAvatar(
    photoUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (!photoUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = photoUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { _ -> ShimmerBox(Modifier.matchParentSize()) },
            error = { DefaultAvatarPlaceholder(contentDescription = contentDescription) },
        )
    } else {
        DefaultAvatarPlaceholder(
            contentDescription = contentDescription,
            modifier = modifier.clip(CircleShape),
        )
    }
}

@Composable
private fun DefaultAvatarPlaceholder(
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcon.Person,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.fillMaxSize(IconSizeFraction),
        )
    }
}
