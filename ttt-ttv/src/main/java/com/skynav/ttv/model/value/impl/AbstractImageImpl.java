/*
 * Copyright 2016 Skynav, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY SKYNAV, INC. AND ITS CONTRIBUTORS “AS IS” AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SKYNAV, INC. OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.skynav.ttv.model.value.impl;

import java.net.URI;

import com.skynav.ttv.model.value.Image;

public abstract class AbstractImageImpl implements Image {
    private URI uri;
    private String typeSpecified;
    private String typeResolved;
    private String formatSpecified;
    private String formatResolved;
    private boolean resolved;
    private boolean resolutionFailure;
    private int width;
    private int height;
    public AbstractImageImpl(URI uri) {
        this.uri = uri;
    }
    public URI getURI() {
        return uri;
    }
    public String getSpecifiedType() {
        return typeSpecified;
    }
    protected void setResolvedType(String type) {
        typeResolved = type;
    }
    public String getResolvedType() {
        if (resolved())
            return typeResolved;
        else
            return null;
    }
    public String getSpecifiedFormat() {
        return formatSpecified;
    }
    protected void setResolvedFormat(String format) {
        formatResolved = format;
    }
    public String getResolvedFormat() {
        if (resolved())
            return formatResolved;
        else
            return null;
    }
    public abstract boolean resolve();
    public boolean resolved() {
        return resolved;
    }
    public boolean resolutionFailed() {
        return resolutionFailure;
    }
    public int getWidth() {
        maybeResolve();
        return width;
    }
    public int getHeight() {
        maybeResolve();
        return height;
    }
    private void maybeResolve() {
        if (!resolved && !resolutionFailure)
            resolved = resolve();
        if (!resolved)
            resolutionFailure = true;
    }
}
