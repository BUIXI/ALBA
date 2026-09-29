package com.oriol.alba

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.oriol.alba.datos.RepositorioAlarmas
import com.oriol.alba.ui.editor.EditorViewModel
import com.oriol.alba.ui.editor.PantallaEditor
import com.oriol.alba.ui.lista.ListaViewModel
import com.oriol.alba.ui.lista.PantallaLista

/** Navegación de la app. Cada pantalla es una clave de [NavigationKeys.kt]. */
@Composable
fun NavegacionAlba(repositorio: RepositorioAlarmas) {
  val pila = rememberNavBackStack(Lista)

  NavDisplay(
    backStack = pila,
    onBack = { pila.removeLastOrNull() },
    // Cada pantalla guarda su estado y tiene sus propios ViewModel, que mueren con ella.
    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
    entryProvider =
      entryProvider {
        entry<Lista> {
          PantallaLista(
            vm = viewModel { ListaViewModel(repositorio) },
            onNueva = { pila.add(Editor()) },
            onAbrir = { id -> pila.add(Editor(id)) },
          )
        }
        entry<Editor>(metadata = TransicionModal) { clave ->
          PantallaEditor(
            vm = viewModel { EditorViewModel(repositorio, clave.alarmaId) },
            onCerrar = { pila.removeLastOrNull() },
          )
        }
      },
  )
}

// Curva de las hojas modales de iOS: arranca rápida y frena suave al final.
private val CurvaHoja = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

/** La pantalla sube desde abajo sobre la anterior, y al cerrarse baja y la descubre. */
private val TransicionModal =
  NavDisplay.transitionSpec {
    slideInVertically(tween(420, easing = CurvaHoja)) { it } togetherWith ExitTransition.KeepUntilTransitionsFinished
  } +
    NavDisplay.popTransitionSpec {
      // La que se va queda encima mientras baja: la de debajo, detrás (índice z -1).
      (EnterTransition.None togetherWith slideOutVertically(tween(320, easing = CurvaHoja)) { it }).apply {
        targetContentZIndex = -1f
      }
    } +
    NavDisplay.predictivePopTransitionSpec {
      (EnterTransition.None togetherWith slideOutVertically(tween(320, easing = CurvaHoja)) { it }).apply {
        targetContentZIndex = -1f
      }
    }
