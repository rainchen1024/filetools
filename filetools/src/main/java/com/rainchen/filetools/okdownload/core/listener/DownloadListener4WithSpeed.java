/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rainchen.filetools.okdownload.core.listener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rainchen.filetools.okdownload.DownloadTask;
import com.rainchen.filetools.okdownload.core.breakpoint.BlockInfo;
import com.rainchen.filetools.okdownload.core.breakpoint.BreakpointInfo;
import com.rainchen.filetools.okdownload.core.cause.EndCause;
import com.rainchen.filetools.okdownload.core.listener.assist.Listener4Assist;
import com.rainchen.filetools.okdownload.core.listener.assist.Listener4SpeedAssistExtend;

public abstract class DownloadListener4WithSpeed extends DownloadListener4
        implements Listener4SpeedAssistExtend.Listener4SpeedCallback {

    DownloadListener4WithSpeed(Listener4SpeedAssistExtend assistExtend) {
        assistExtend.setCallback(this);
        setAssistExtend(assistExtend);
    }

    public DownloadListener4WithSpeed() {
        this(new Listener4SpeedAssistExtend());
    }

    @Override
    public final void infoReady(DownloadTask task, @NonNull BreakpointInfo info,
                                boolean fromBreakpoint,
                                @NonNull Listener4Assist.Listener4Model model) { }

    @Override
    public final void progressBlock(DownloadTask task, int blockIndex, long currentBlockOffset) { }

    @Override public final void progress(DownloadTask task, long currentOffset) { }

    @Override public final void blockEnd(DownloadTask task, int blockIndex, BlockInfo info) { }

    @Override
    public final void taskEnd(DownloadTask task, EndCause cause, @Nullable Exception realCause,
                              @NonNull Listener4Assist.Listener4Model model) { }
}
