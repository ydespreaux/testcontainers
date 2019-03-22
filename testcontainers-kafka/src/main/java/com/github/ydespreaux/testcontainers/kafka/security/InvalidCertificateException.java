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

import java.nio.file.Path;

import static java.lang.String.format;

/**
 * @author Yoann Despréaux
 * @since 1.1.1
 */
public class InvalidCertificateException extends RuntimeException {

    public InvalidCertificateException(Path certificatePath, Throwable cause) {
        super(format("The certificate %s is invalid: ", certificatePath.getFileName()), cause);
    }

    public InvalidCertificateException(Throwable cause) {
        super(cause);
    }

}