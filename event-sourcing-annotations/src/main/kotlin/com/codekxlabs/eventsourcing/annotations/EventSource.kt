package com.codekxlabs.eventsourcing.annotations

/**
 * Created on 7/1/25
 * @author Levi Opunga
 **/


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventSource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventReceiver