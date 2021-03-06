/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import android.os.Bundle;
import android.os.ResultReceiver;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.foundation.MASRequest;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Holds information about an MSSO request while it is waiting to be processed.
 */
class MssoRequest {
    private static final AtomicLong nextRequestId = new AtomicLong();
    private final long id;
    private final Object creator;
    private final MssoContext mssoContext;
    private MASRequest request;
    private final ResultReceiver resultReceiver;
    //Extra data for the request
    private Bundle extra;
    private boolean isRunning;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    MssoRequest(Object creator, MssoContext mssoContext, MASRequest request, ResultReceiver resultReceiver) {
        this.id = nextRequestId.incrementAndGet();
        this.creator = creator;
        this.mssoContext = mssoContext;
        this.request = request;
        this.resultReceiver = resultReceiver;
    }

    public long getId() {
        return id;
    }

    public MssoContext getMssoContext() {
        return mssoContext;
    }

    public MASRequest getRequest() {
        return request;
    }

    public ResultReceiver getResultReceiver() {
        return resultReceiver;
    }

    public Object getCreator() {
        return creator;
    }

    public void setExtra(Bundle extra) {
        this.extra = extra;
    }

    public Bundle getExtra() {
        return extra;
    }

}
