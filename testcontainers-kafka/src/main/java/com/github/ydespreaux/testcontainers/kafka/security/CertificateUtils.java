package fr.laposte.an.testcontainers.kafka.security;

import fr.laposte.an.testcontainers.kafka.acls.AclsCommand;
import fr.laposte.an.testcontainers.kafka.acls.OperationAcls;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import static java.security.KeyStore.getInstance;

public class CertificateUtils {

    public static KeyStore loadKeyStore(Path keystorePath, String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = getInstance("JKS");
        ks.load(new FileInputStream(keystorePath.toFile()), password.toCharArray());
        return ks;
    }

    public static String getUser(KeyStore keyStore) throws KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate ca = (X509Certificate) keyStore.getCertificate(alias);
            Principal issuerDN = ca.getIssuerDN();
            Principal subjectDN = ca.getSubjectDN();
            if (!subjectDN.getName().equals(issuerDN.getName())) {
                return subjectDN.getName();
            }
        }
        return null;
    }

    public static Path generateCredentials(String password) throws IOException {
        File credentialsFile = File.createTempFile("credentials", "");
        Path path = credentialsFile.toPath();
        try (BufferedWriter writer = Files.newBufferedWriter(path))
        {
            writer.write(password);
            writer.flush();
        }
        return path;
    }
}
