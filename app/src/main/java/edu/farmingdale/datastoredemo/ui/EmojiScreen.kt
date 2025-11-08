package edu.farmingdale.datastoredemo.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.farmingdale.datastoredemo.R
import edu.farmingdale.datastoredemo.data.local.LocalEmojiData
import edu.farmingdale.datastoredemo.ui.theme.DataStoreDemoTheme
import android.os.Build
import android.icu.lang.UCharacter

private fun emojiDisplayName(emoji: String): String {

    // Get Unicode names for each code point in the grapheme
    val names = buildList {
        var i = 0
        while (i < emoji.length) {
            val cp = Character.codePointAt(emoji, i)
            UCharacter.getName(cp)?.let { add(it) }
            i += Character.charCount(cp)
        }
    }

    var raw = names.joinToString(" + ")

    raw = raw.replace("VARIATION SELECTOR-16", "")
        .replace("ZERO WIDTH JOINER", "")
        .replace("EMOJI COMPONENT", "")
        .replace('_', ' ')
        .trim()

    raw = raw.replace(Regex("EMOJI MODIFIER FITZPATRICK TYPE-1-2"), "light skin tone")
        .replace(Regex("EMOJI MODIFIER FITZPATRICK TYPE-3"), "medium-light skin tone")
        .replace(Regex("EMOJI MODIFIER FITZPATRICK TYPE-4"), "medium skin tone")
        .replace(Regex("EMOJI MODIFIER FITZPATRICK TYPE-5"), "medium-dark skin tone")
        .replace(Regex("EMOJI MODIFIER FITZPATRICK TYPE-6"), "dark skin tone")

    // Lowercase then sentence case for readability
    val cleaned = raw.lowercase()
        .replace(Regex("\\s*\\+\\s*"), " + ")
        .replace(Regex("\\s{2,}"), " ")
        .trim()

    return cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        .ifBlank { "Emoji" }
}

@Composable
fun EmojiReleaseApp(
    emojiViewModel: EmojiScreenViewModel = viewModel(factory = EmojiScreenViewModel.Factory)
) {
    val state = emojiViewModel.uiState.collectAsState().value

    // Apply light/dark theme from the persisted preference
    DataStoreDemoTheme(darkTheme = state.isDarkTheme) {
        EmojiScreen(
            uiState = state,
            selectLayout = emojiViewModel::selectLayout,
            selectTheme = emojiViewModel::selectTheme
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiScreen(
    uiState: EmojiReleaseUiState,
    selectLayout: (Boolean) -> Unit,
    selectTheme: (Boolean) -> Unit
) {
    val isLinearLayout = uiState.isLinearLayout
    val isDarkTheme = uiState.isDarkTheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.top_bar_name)) },
                actions = {
                    // Layout toggle
                    IconButton(onClick = { selectLayout(!isLinearLayout) }) {
                        Icon(
                            painter = painterResource(uiState.toggleIcon),
                            contentDescription = stringResource(uiState.toggleContentDescription),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    // Theme toggle
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { selectTheme(it) }
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary
                )
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(
            top = dimensionResource(R.dimen.padding_medium),
            start = dimensionResource(R.dimen.padding_medium),
            end = dimensionResource(R.dimen.padding_medium),
        )
        if (isLinearLayout) {
            EmojiReleaseLinearLayout(
                modifier = modifier.fillMaxWidth(),
                contentPadding = innerPadding
            )
        } else {
            EmojiReleaseGridLayout(
                modifier = modifier,
                contentPadding = innerPadding
            )
        }
    }
}

/*
 * Each card is clickable and shows a short Toast with the selected emoji.
 */
@Composable
fun EmojiReleaseLinearLayout(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
    ) {
        items(items = LocalEmojiData.EmojiList, key = { it }) { e ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.clickable {
                    // shows a Toast describing the clicked emoji.
                    Toast.makeText(context, emojiDisplayName(e), Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(
                    text = e,
                    fontSize = 50.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


 //Grid layout of emojis.
@Composable
fun EmojiReleaseGridLayout(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(3),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        items(items = LocalEmojiData.EmojiList, key = { it }) { e ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .height(110.dp)
                    .clickable {
                        Toast.makeText(context, emojiDisplayName(e), Toast.LENGTH_SHORT).show()
                    },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = e,
                    fontSize = 50.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically)
                        .padding(dimensionResource(R.dimen.padding_small))
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
