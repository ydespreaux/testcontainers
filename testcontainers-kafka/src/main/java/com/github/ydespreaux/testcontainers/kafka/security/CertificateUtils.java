/*
 * Copyright (C) 2018 Yoann Despréaux
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING . If not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr
 */

package com.github.ydespreaux.testcontainers.kafka.security;

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

public final class CertificateUtils {

    private CertificateUtils() {
    }

    public static KeyStore loadKeyStore(Path keystorePath, String password) {
        try (FileInputStream input = new FileInputStream(keystorePath.toFile())) {
            KeyStore ks = getInstance("JKS");
            ks.load(input, password.toCharArray());
            return ks;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new InvalidCertificateException(keystorePath, e);
        }
    }

    public static String getSubject(KeyStore keyStore, String issuer) {
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate ca = (X509Certificate) keyStore.getCertificate(alias);
                Principal subjectDN = ca.getSubjectDN();
                if (!subjectDN.getName().equals(issuer)) {
                    return subjectDN.getName();
                }
            }
            return null;
        } catch (KeyStoreException e) {
            throw new InvalidCertificateException(e);
        }
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
