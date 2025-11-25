package org.example.infra;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class DataSourceProvider {

    public static DataSource getDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:database;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}