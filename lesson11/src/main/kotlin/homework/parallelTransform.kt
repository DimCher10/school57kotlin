package homework

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * Задание: Параллельное преобразование элементов списка с использованием async.
 *
 * Преобразуйте каждый элемент списка в отдельной корутине с помощью async.
 *
 * @param items список элементов для преобразования
 * @param transform функция преобразования
 * @return список преобразованных элементов в исходном порядке
 */
suspend fun <T, R> parallelTransform(
    items: List<T>,
    transform: suspend (T) -> R
): List<R> = coroutineScope {
    val defferds = items.map {
        it -> async {
            transform(it)
        }
    }
    defferds.awaitAll()
}