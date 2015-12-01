package com.foxelbox.app;

import android.app.Application;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CAUtility {
    private static SSLContext context;

    public static void initialize(Application app) {
        try {
            // *** DEFAULT TRUST MANAGER ***
            // See: http://stackoverflow.com/questions/24555890/using-a-custom-truststore-in-java-as-well-as-the-default-one
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // Using null here initialises the TMF with the default trust store.
            tmf.init((KeyStore)null);
            final X509TrustManager defaultTrustManager = getX509TrustManager(tmf);

            // *** OUR TRUST MANAGER ***
            // See: http://developer.android.com/training/articles/security-ssl.html
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = app.getResources().openRawResource(R.raw.letsencrypt);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("letsencrypt_ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            final X509TrustManager ourTrustManager = getX509TrustManager(tmf);

            // *** MERGED TRUST MANAGER ***
            final X509TrustManager mergedTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    try {
                        defaultTrustManager.checkClientTrusted(chain, authType);
                    } catch (CertificateException e) {
                        ourTrustManager.checkClientTrusted(chain, authType);
                    }
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    try {
                        defaultTrustManager.checkServerTrusted(chain, authType);
                    } catch (CertificateException e) {
                        ourTrustManager.checkServerTrusted(chain, authType);
                    }
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return ourTrustManager.getAcceptedIssuers(); // If we ever do client cert auth, we use our own
                }
            };

            // Create an SSLContext that uses our TrustManager
            context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { mergedTrustManager }, null);
        } catch (CertificateException|KeyStoreException|NoSuchAlgorithmException|KeyManagementException|IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static X509TrustManager getX509TrustManager(TrustManagerFactory tmf) {
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager)tm;
            }
        }
        throw new RuntimeException("Could not find X509TrustManager");
    }

    public static void upgrade(HttpsURLConnection httpsURLConnection) {
        httpsURLConnection.setSSLSocketFactory(context.getSocketFactory());
    }
}
