package com.issyzone.syzbleprinter.koin_test

import com.issyzone.syzbleprinter.viewmodel.ScanBluViewModel
import com.issyzone.syzbleprinter.viewmodel.TwoInchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val singleModule = module {
    single {
        KoinSingleTest
    }
}
val normalModule = module {
    factory { (number: Int) -> KoinTest(get(), number) }
}
val viewModule = module {
    viewModel {
        TwoInchViewModel()
    }
    viewModel {
        ScanBluViewModel()
    }
}