/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.storage;

import com.ca.mas.foundation.MASConstants;

public class MASSecureStorageAppTest extends MASStorageTest {

    @Override
    int getMode() {
        return MASConstants.MAS_APPLICATION;
    }

    @Override
    MASStorage getMASStorage() {
        return new MASSecureStorage();
    }

}
