/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * See License.txt in the project root for license information.
 ******************************************************************************/
package com.microsoft.office365.files.services.odata;

import com.google.common.util.concurrent.*;
import com.microsoft.office365.odata.interfaces.*;
import com.microsoft.office365.files.services.*;

public class CurrentUserRequestContextFetcher extends ODataEntityFetcher<CurrentUserRequestContext,CurrentUserRequestContextOperations> implements Readable<CurrentUserRequestContext> {

	 public CurrentUserRequestContextFetcher(String urlComponent, ODataExecutable parent) {
        super(urlComponent, parent, CurrentUserRequestContext.class,CurrentUserRequestContextOperations.class);
    }

	public CurrentUserRequestContextFetcher addParameter(String name, Object value) {
	    addCustomParameter(name, value);
		return this;
	}
	public DriveFetcher getdrive() {
        return new DriveFetcher("drive", this);
    }
	public ODataCollectionFetcher<Item, ItemFetcher, ItemCollectionOperations> getfiles() {
        return new ODataCollectionFetcher<Item, ItemFetcher,ItemCollectionOperations>("files", this, Item.class,ItemCollectionOperations.class);
    }
}