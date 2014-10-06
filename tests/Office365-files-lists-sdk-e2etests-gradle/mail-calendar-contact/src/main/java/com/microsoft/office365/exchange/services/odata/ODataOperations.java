/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365.exchange.services.odata;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.office365.odata.interfaces.*;

public abstract class ODataOperations extends ODataExecutable {
    private String urlComponent;
    private ODataExecutable parent;

    public ODataOperations(String urlComponent, ODataExecutable parent) {
        this.urlComponent = urlComponent;
        this.parent = parent;
    }

    @Override
    ListenableFuture<byte[]> oDataExecute(String path, byte[] content, HttpVerb verb) {
        return parent.oDataExecute(urlComponent + "/" + path, content, verb);
    }

    @Override
    DependencyResolver getResolver() {
        return parent.getResolver();
    }
}
