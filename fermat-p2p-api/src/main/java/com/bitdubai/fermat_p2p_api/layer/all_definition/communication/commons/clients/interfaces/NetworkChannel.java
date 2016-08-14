package com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.clients.interfaces;

import com.bitdubai.fermat_api.layer.all_definition.network_service.enums.NetworkServiceType;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.clients.exceptions.CantSendMessageException;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.PackageContent;

import java.util.UUID;

/**
 * Created by Matias Furszyfer on 2016.07.06..
 */
public interface NetworkChannel {

    void connect();

    void disconnect();

    boolean isConnected();

    public UUID sendMessage(PackageContent packageContent, NetworkServiceType networkServiceType, String destinationPublicKey) throws CantSendMessageException;


}
