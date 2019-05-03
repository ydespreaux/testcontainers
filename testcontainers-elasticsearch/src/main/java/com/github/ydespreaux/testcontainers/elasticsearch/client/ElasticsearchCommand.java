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

package com.github.ydespreaux.testcontainers.elasticsearch.client;

import lombok.*;
import org.springframework.util.Assert;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.DELIMITER_PATH;

/**
 * @author Yoann Despréaux
 * @since 1.0.1
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElasticsearchCommand {

    private RequestMethod requestMethod;
    private String path;
    private String json;
    private boolean skip;

    @Override
    public String toString() {
        return "ElasticsearchCommand{" +
                "requestMethod=" + requestMethod +
                ", path='" + path + '\'' +
                ", json='" + json + '\'' +
                ", skip=" + skip +
                '}';
    }

    /**
     * @param path
     */
    public void setPath(String path) {
        Assert.notNull(path, "path parameter must be provided");
        String formattedPath = path;
        if (!formattedPath.startsWith(DELIMITER_PATH)) {
            formattedPath = DELIMITER_PATH.concat(formattedPath);
        }
        this.path = formattedPath;
    }

    public enum RequestMethod {
        PUT, POST, DELETE;
    }
}
