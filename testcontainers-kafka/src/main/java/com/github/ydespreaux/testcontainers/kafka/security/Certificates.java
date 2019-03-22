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

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.ydespreaux.testcontainers.kafka.security.CertificateUtils.getSubject;
import static com.github.ydespreaux.testcontainers.kafka.security.CertificateUtils.loadKeyStore;
import static java.lang.String.format;

public class Certificates implements InitializingBean {

    private final String keystoreFilename;
    @Getter
    private final String keystorePassword;
    private final String truststoreFilename;
    @Getter
    private final String truststorePassword;


    @Getter
    private Path keystorePath;
    @Getter
    private Path truststorePath;
    @Getter
    private String user;

    /**
     * @param keystoreFilename
     * @param keystorePassword
     */
    public Certificates(String keystoreFilename, String keystorePassword) {
        this(keystoreFilename, keystorePassword, null, null);
    }

    /**
     * @param keystoreFilename
     * @param keystorePassword
     * @param truststoreFilename
     * @param truststorePassword
     */
    public Certificates(String keystoreFilename, String keystorePassword, String truststoreFilename, String truststorePassword) {
        Assert.notNull(keystoreFilename, "keystoreFilename parameter is mandatory");
        Assert.notNull(keystorePassword, "keystorePassword parameter is mandatory");
        this.keystoreFilename = keystoreFilename;
        this.keystorePassword = keystorePassword;
        this.truststoreFilename = truststoreFilename;
        this.truststorePassword = truststorePassword;
        afterPropertiesSet();
    }

    @Override
    public void afterPropertiesSet() {
        String truststoreSubject = null;
        if (this.truststoreFilename != null) {
            this.truststorePath = buildPath(this.truststoreFilename);
            truststoreSubject = getSubject(loadKeyStore(this.truststorePath, this.truststorePassword), null);
        }
        this.keystorePath = buildPath(this.keystoreFilename);
        this.user = getSubject(loadKeyStore(this.keystorePath, this.keystorePassword), truststoreSubject);
    }

    /**
     * @param filename
     * @return
     */
    private Path buildPath(String filename) {
        MountableFile mountableFile = MountableFile.forClasspathResource(filename);
        Path credentialsPath = Paths.get(mountableFile.getResolvedPath());
        File credentialsFile = credentialsPath.toFile();
        if (!credentialsFile.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", credentialsPath.toString()));
        }
        if (!credentialsFile.isFile()) {
            throw new IllegalArgumentException(format("Resource with path %s must be a file", credentialsPath.toString()));
        }
        return credentialsPath;
    }
}
