package ps.isel.ipl.pt.gabageapp.util.loader

/**
 * Created by goncalo on 12/06/2018.
 */
class Result<T> {
    val result:T?
    val error: ErrorApi?

    constructor(result: T?, error: ErrorApi?){
        this.result = result
        this.error = error
    }
}