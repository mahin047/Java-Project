package com.example.demo_java_project.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Common contract for a DAO that performs create/read/update database
 * access for one entity type. ResourceDAO implements this to show a DAO
 * can be used polymorphically through this interface.
 */
public interface Crud<T> {
    List<T> findAll() throws SQLException;
    int insert(T item) throws SQLException;
    void update(T item) throws SQLException;
}