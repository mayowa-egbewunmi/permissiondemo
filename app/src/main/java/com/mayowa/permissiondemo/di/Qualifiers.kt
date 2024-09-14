package com.mayowa.permissiondemo.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ioDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class uiDispatcher