package com.bretancezar.samcontrolapp.service

class ServiceException(val type: ServiceExceptionCauses, override val cause: Throwable?): Exception() {
}