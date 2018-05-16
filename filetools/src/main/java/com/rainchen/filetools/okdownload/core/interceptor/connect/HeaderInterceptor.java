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

package com.rainchen.filetools.okdownload.core.interceptor.connect;

import android.support.annotation.NonNull;

import com.rainchen.filetools.okdownload.DownloadTask;
import com.rainchen.filetools.okdownload.OkDownload;
import com.rainchen.filetools.okdownload.core.Util;
import com.rainchen.filetools.okdownload.core.breakpoint.BlockInfo;
import com.rainchen.filetools.okdownload.core.breakpoint.BreakpointInfo;
import com.rainchen.filetools.okdownload.core.connection.DownloadConnection;
import com.rainchen.filetools.okdownload.core.download.DownloadChain;
import com.rainchen.filetools.okdownload.core.download.DownloadStrategy;
import com.rainchen.filetools.okdownload.core.exception.InterruptException;
import com.rainchen.filetools.okdownload.core.interceptor.Interceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rainchen.filetools.okdownload.core.Util.CONTENT_LENGTH;
import static com.rainchen.filetools.okdownload.core.Util.CONTENT_RANGE;
import static com.rainchen.filetools.okdownload.core.Util.IF_MATCH;
import static com.rainchen.filetools.okdownload.core.Util.RANGE;


public class HeaderInterceptor implements Interceptor.Connect {
    private static final String TAG = "HeaderInterceptor";

    @NonNull @Override
    public DownloadConnection.Connected interceptConnect(DownloadChain chain) throws IOException {
        final BreakpointInfo info = chain.getInfo();
        final DownloadConnection connection = chain.getConnectionOrCreate();
        final DownloadTask task = chain.getTask();

        // add user customize header
        final Map<String, List<String>> userRequestHeaderField = task.getHeaderMapFields();
        if (userRequestHeaderField != null) {
            for (Map.Entry<String, List<String>> entry : userRequestHeaderField.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                for (String value : values) {
                    connection.addHeader(key, value);
                }
            }
        }

        // add range header
        final int blockIndex = chain.getBlockIndex();
        final BlockInfo blockInfo = info.getBlock(blockIndex);
        if (blockInfo == null) {
            throw new IOException("No block-info found on " + blockIndex);
        }

        String range = "bytes=" + blockInfo.getRangeLeft() + "-";
        range += blockInfo.getRangeRight();

        connection.addHeader(RANGE, range);
        Util.d(TAG, "AssembleHeaderRange (" + task.getId() + ") block(" + blockIndex + ") "
                + "downloadFrom(" + blockInfo.getRangeLeft() + ") currentOffset("
                + blockInfo.getCurrentOffset() + ")");

        // add etag if exist
        final String etag = info.getEtag();
        if (!Util.isEmpty(etag)) {
            connection.addHeader(IF_MATCH, etag);
        }

        if (chain.getCache().isInterrupt()) {
            throw InterruptException.SIGNAL;
        }

        OkDownload.with().callbackDispatcher().dispatch()
                .connectStart(task, blockIndex, connection.getRequestProperties());

        DownloadConnection.Connected connected = chain.processConnect();

        Map<String, List<String>> responseHeaderFields = connected.getResponseHeaderFields();
        if (responseHeaderFields == null) responseHeaderFields = new HashMap<>();

        OkDownload.with().callbackDispatcher().dispatch().connectEnd(task, blockIndex,
                connected.getResponseCode(), responseHeaderFields);
        if (chain.getCache().isInterrupt()) {
            throw InterruptException.SIGNAL;
        }

        // if precondition failed.
        final DownloadStrategy strategy = OkDownload.with().downloadStrategy();
        final DownloadStrategy.ResumeAvailableResponseCheck responseCheck =
                strategy.resumeAvailableResponseCheck(connected, blockIndex, info);
        responseCheck.inspect();

        final long contentLength;
        final String contentLengthField = connected.getResponseHeaderField(CONTENT_LENGTH);
        if (contentLengthField == null || contentLengthField.length() == 0) {
            final String contentRangeField = connected.getResponseHeaderField(CONTENT_RANGE);
            contentLength = Util.parseContentLengthFromContentRange(contentRangeField);
        } else {
            contentLength = Util.parseContentLength(contentLengthField);
        }

        chain.setResponseContentLength(contentLength);
        return connected;
    }
}
