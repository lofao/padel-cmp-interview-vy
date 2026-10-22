package io.bifri.interview.cmp.padel.feature.player.domain.di

import io.bifri.interview.cmp.padel.feature.player.domain.usecase.GetPlayersUseCase
import org.koin.dsl.module

val PlayerDomainModule = module {
    factory { GetPlayersUseCase(repository = get()) }
}
