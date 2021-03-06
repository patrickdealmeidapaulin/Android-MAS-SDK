/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * NFC for cross device session sharing
 */
public class NFCRenderer extends PollingRenderer {

    /**
     * Error when NFC is not available
     */
    public static final int NFC_ERR = 100;
    /**
     * Error when unable to start Bluetooth Service
     */
    public static final int BLUETOOTH_ERR = 101;
    /**
     * Error when failed to start Bluetooth server
     */
    public static final int BLUETOOTH_CONN_ERR = 102;

    public static final String UUID = "uuid";
    public static final String PROVIDER_URL = "provider_url";
    public static final String ADDRESS = "address";

    private ListenerThread listener = null;
    private UUID serverUuid = null;
    private NfcAdapter adapter = null;
    private String serverAddress = null;

    @Override
    public View render() {
        return null;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRenderCompleted() {
        adapter = NfcAdapter.getDefaultAdapter(context);
        if (adapter == null) {
            onError(NFC_ERR, "NFC is not available", null);
            return;
        } else if (!adapter.isEnabled()) {
            onError(NFC_ERR, "NFC is not enabled", null);
            return;
        }

        serverUuid = java.util.UUID.randomUUID();

        try {
            listener = new ListenerThread();
            listener.start();
        } catch (Exception e) {
            if (DEBUG) Log.w(TAG,
                    "Failed to start Bluetooth Service, will use polling for NFC notification", e);
            onError(BLUETOOTH_ERR, "Unable to start Bluetooth Service", e);
        }

        adapter.setOnNdefPushCompleteCallback(new NfcAdapter.OnNdefPushCompleteCallback() {
            @Override
            public void onNdefPushComplete(NfcEvent event) {
                poll();
            }
        }, (Activity) context);

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UUID, serverUuid.toString());
            jsonObject.put(PROVIDER_URL, provider.getUrl().toString());
            jsonObject.put(ADDRESS, serverAddress);
            // Register callback
            adapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {

                @Override
                public NdefMessage createNdefMessage(NfcEvent event) {
                    return new NdefMessage(
                            new NdefRecord[]{NdefRecord.createMime(
                                    "application/vnd.com.ca.mas.core.beam", jsonObject.toString().getBytes())
                            });
                }

            }, (Activity) context);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "Failed to set NFC Push Message Callback", e);
        }


        super.onRenderCompleted();
    }

    @Override
    protected void onError(int code, String message, Exception e) {
        //Ignore error by default
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void close() {
        super.close();
        if (listener != null) {
            listener.cancel();
        }
        if (adapter != null) {
            adapter.setNdefPushMessageCallback(null, ((Activity) context));
        }
    }

    @Override
    protected boolean startPollingOnStartup() {
        return false;
    }


    private class ListenerThread extends Thread {

        private BluetoothServerSocket serverSocket;

        @SuppressWarnings("MissingPermission")
        public ListenerThread() throws Exception {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                adapter.cancelDiscovery();
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("BluetoothRenderer", serverUuid);
                serverAddress = adapter.getAddress();
            } else {
                throw new IllegalStateException("Unable to acquire BluetoothAdapter");
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            try {
                if (serverSocket != null) {
                    socket = serverSocket.accept();
                    proceed();
                }
            } catch (IOException ignore) {
                //The socket is closed when shutdown the listener
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        if (DEBUG) Log.d(TAG,
                                "Failed to close BlueTooth Socket: " + e.getMessage());
                    }
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        if (DEBUG) Log.d(TAG,
                                "Failed to close BlueTooth ServerSocket: " + e.getMessage());
                    }
                }

            }
        }

        public void cancel() {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    if (DEBUG) Log.d(TAG,
                            "Failed to close BlueTooth ServerSocket: " + e.getMessage());
                }
            }
        }
    }
}
