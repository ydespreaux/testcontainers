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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
public class CertificatesTest {

    @Test
    public void certificates() {
        Certificates certificates = new Certificates(
                "secrets/server.keystore.jks",
                "0123456789",
                "secrets/truststore.jks",
                "0123456789");
        assertThat(certificates.getKeystorePath(), is(notNullValue()));
        assertThat(certificates.getTruststorePassword(), is(notNullValue()));
        assertThat(certificates.getKeystorePassword(), is(equalTo("0123456789")));
        assertThat(certificates.getTruststorePassword(), is(equalTo("0123456789")));
        assertThat(certificates.getUser(), is(equalTo("CN=kafka.server, OU=test, O=test, L=test, ST=github, C=fr")));
    }
}
