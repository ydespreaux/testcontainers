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

package com.github.ydespreaux.testcontainers.common.jdbc;

import com.github.ydespreaux.testcontainers.common.IContainer;
import lombok.Getter;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @param <SELF>
 * @since 1.1.2
 */
public abstract class AbstractJdbcContainer<SELF extends AbstractJdbcContainer<SELF>> extends JdbcDatabaseContainer<SELF> implements IContainer<SELF> {

    private String username = "user";
    private String password = UUID.randomUUID().toString();
    private String databaseName = "db_unit";

    /**
     * Defines the name of the spring boot property corresponding to the jdbc driver class.
     */
    @Getter
    private String driverClassSystemProperty = "spring.datasource.driver-class-name";
    /**
     * Defines the name of the spring boot property corresponding to the url database.
     */
    @Getter
    private String urlSystemProperty = "spring.datasource.url";
    /**
     * Defines the name of the spring boot property corresponding to the user name.
     */
    @Getter
    private String usernameSystemProperty = "spring.datasource.username";
    /**
     * Defines the name of the spring boot property corresponding to the user's password.
     */
    @Getter
    private String passwordSystemProperty = "spring.datasource.password";
    /**
     * Defines the name of the spring boot property corresponding to the databse's platform.
     */
    @Getter
    private String platformSystemProperty = "spring.datasource.platform";

    /**
     * @param dockerImageName
     */
    public AbstractJdbcContainer(String dockerImageName) {
        super(dockerImageName);
    }

    /**
     * @param image
     */
    public AbstractJdbcContainer(Future<String> image) {
        super(image);
    }

    /**
     * @param driverClassSystemProperty
     * @return
     */
    public SELF withDriverClassSystemProperty(String driverClassSystemProperty) {
        this.driverClassSystemProperty = driverClassSystemProperty;
        return this.self();
    }

    /**
     * @param urlSystemProperty
     * @return
     */
    public SELF withUrlSystemProperty(String urlSystemProperty) {
        this.urlSystemProperty = urlSystemProperty;
        return this.self();
    }

    /**
     * @param usernameSystemProperty
     * @return
     */
    public SELF withUsernameSystemProperty(String usernameSystemProperty) {
        this.usernameSystemProperty = usernameSystemProperty;
        return this.self();
    }

    /**
     * @param passwordSystemProperty
     * @return
     */
    public SELF withPasswordSystemProperty(String passwordSystemProperty) {
        this.passwordSystemProperty = passwordSystemProperty;
        return this.self();
    }

    /**
     * @param platformSystemProperty
     * @return
     */
    public SELF withPlatformSystemProperty(String platformSystemProperty) {
        this.platformSystemProperty = platformSystemProperty;
        return this.self();
    }

    @Override
    public SELF withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this.self();
    }

    @Override
    public SELF withUsername(String username) {
        this.username = username;
        return this.self();
    }

    @Override
    public SELF withPassword(String password) {
        this.password = password;
        return this.self();
    }

    @Override
    public String getDatabaseName() {
        return this.databaseName;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
