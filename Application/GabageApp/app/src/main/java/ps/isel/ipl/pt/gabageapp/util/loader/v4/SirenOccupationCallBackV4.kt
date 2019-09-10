package ps.isel.ipl.pt.gabageapp.util.loader.v4

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import ps.isel.ipl.pt.gabageapp.util.loader.Result
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenOccupationRangeDto

/**
 * Created by goncalo on 23/06/2018.
 */
abstract class SirenOccupationCallBackV4(val context: Context, val keyWord: String) : LoaderCallbacksErrorV4<SirenOccupationRangeDto> {

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenOccupationRangeDto>> {
        return AsyncTaskLoaderApiV4<SirenOccupationRangeDto>(SirenOccupationRangeDto::class.java, args.getParcelable(keyWord), context )
    }

    override fun onLoaderReset(loader: Loader<Result<SirenOccupationRangeDto>>?) {
        loader?.stopLoading()
    }
}