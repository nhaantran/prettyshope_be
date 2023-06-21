package com.prettyshopbe.prettyshopbe.controller;

import com.prettyshopbe.prettyshopbe.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;

@RestController
public class TestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test")
    public String test() throws SQLException {

        String dbName = dataSource.getConnection().getMetaData().getDatabaseProductName();
        return "Connected to: " + dbName;
    }
}
