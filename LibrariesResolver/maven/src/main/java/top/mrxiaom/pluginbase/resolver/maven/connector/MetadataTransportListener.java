/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package top.mrxiaom.pluginbase.resolver.maven.connector;

import top.mrxiaom.pluginbase.resolver.aether.repository.RemoteRepository;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.MetadataTransfer;
import top.mrxiaom.pluginbase.resolver.aether.spi.connector.transport.Transporter;
import top.mrxiaom.pluginbase.resolver.aether.transfer.MetadataNotFoundException;
import top.mrxiaom.pluginbase.resolver.aether.transfer.MetadataTransferException;
import top.mrxiaom.pluginbase.resolver.aether.transfer.TransferEvent;

final class MetadataTransportListener extends TransferTransportListener<MetadataTransfer> {

    private final RemoteRepository repository;

    MetadataTransportListener(
            MetadataTransfer transfer, RemoteRepository repository, TransferEvent.Builder eventBuilder) {
        super(transfer, eventBuilder);
        this.repository = repository;
    }

    @Override
    public void transferFailed(Exception exception, int classification) {
        MetadataTransferException e;
        if (classification == Transporter.ERROR_NOT_FOUND) {
            e = new MetadataNotFoundException(getTransfer().getMetadata(), repository);
        } else {
            e = new MetadataTransferException(getTransfer().getMetadata(), repository, exception);
        }
        getTransfer().setException(e);
        super.transferFailed(e, classification);
    }
}
