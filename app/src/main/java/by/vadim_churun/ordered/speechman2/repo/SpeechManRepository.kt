package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import by.vadim_churun.ordered.speechman2.db.SpeechManDatabase
import by.vadim_churun.ordered.speechman2.db.daos.*
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


abstract class SpeechManRepository(val appContext: Context)
{
    protected class FiilteredItemsStreamBuilder<ItemType, FilterConfigType>
    {
        private var lastItems = kotlin.collections.emptyList<ItemType>()
        private var lastFilters: FilterConfigType? = null


        /** From two sources - the source of items and the source of filters - constructs
          * one [Observable] which makes emissions when either of the sources does
          * and emits a sublist of the original list to match the current filter configuration. **/
        fun create(itemsSource: Observable< List<ItemType> >,
            filtersSource: Observable<FilterConfigType>,
            predicate: (item: ItemType, filters: FilterConfigType) -> Boolean
        ): Observable< List<ItemType> >
            = itemsSource.observeOn(Schedulers.single()).map { newItems ->
                lastItems = newItems
                newItems to lastFilters
            }.mergeWith(filtersSource
                .debounce(256, TimeUnit.MILLISECONDS)
                .map< Pair<List<ItemType>, FilterConfigType> > { newFilters ->
                    lastFilters = newFilters
                    lastItems to newFilters
            }).map { pair ->
                // If filters haven't been set, just pass everything.
                if(pair.second == null)
                    return@map pair.first

                val result = ArrayList<ItemType>(pair.first.size / 2)
                for(item in pair.first)
                {
                    if( predicate(item, pair.second!!) )
                        result.add(item)
                }
                result
            }
    }


    protected val peopleDAO: PeopleDAO
        get() = SpeechManDatabase.getInstance(appContext).getPeopleDAO()

    protected val seminarsDAO: SeminarsDAO
        get() = SpeechManDatabase.getInstance(appContext).getSeminarsDAO()

    protected val productsDAO: ProductsDAO
        get() = SpeechManDatabase.getInstance(appContext).getProductsDAO()

    protected val associationsDAO: AssociationsDAO
        get() = SpeechManDatabase.getInstance(appContext).getAssociationsDAO()
}