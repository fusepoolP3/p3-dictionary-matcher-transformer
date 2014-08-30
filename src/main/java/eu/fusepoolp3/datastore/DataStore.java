/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabor
 */
public class DataStore {
    Connection connection = null;

    /**
     * Default constructor.
     */
    public DataStore() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Creates a database connection to a local sqlite database object.
     */
    public void Connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/datastore.db");
        } catch (ClassNotFoundException | SQLException ex) {
            // if the error message is "out of memory", it probably means no database file is found
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Closes the database connection.
     */
    public void Disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Selects all taxonomies from the local data store and returns a
     * list of Taxonomy objects.
     *
     * @return
     */
    public List<Taxonomy> GetTaxonomies() {
        List<Taxonomy> taxonomies = null;
        Statement statement;
        Connect();
        try {
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            
            taxonomies = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("select * from taxonomy")) {
                while (rs.next()) {
                    // read the result set
                    taxonomies.add(new Taxonomy(rs.getInt("id"), rs.getString("name"), rs.getString("uri")));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            Disconnect();
        }
        
        return taxonomies;
    }
    
    /**
     * Selects a single taxonomy from the local data store and returns a
     * Taxonomy object.
     *
     * @return
     */
    public Taxonomy GetTaxonomy(String uri) {
        Taxonomy taxonomy = null;
        PreparedStatement statement;
        Connect();
        try {         
            
            statement = connection.prepareStatement("select * from taxonomy where uri=?");
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.setString(1, uri);
            
            ResultSet rs = statement.executeQuery();    
            if (rs.next()) {
                // read the result set
                taxonomy = new Taxonomy(rs.getInt("id"), rs.getString("name"), rs.getString("uri"));
            }
            
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            Disconnect();
        }
        
        return taxonomy;
    }
    
    /**
     * Adds a taxonomy from the local data store.
     *
     * @param c
     * @return
     */
    public boolean AddTaxonomy(Taxonomy t) {
        boolean success = false;
        PreparedStatement statement;
        Connect();
        try {
            statement = connection.prepareStatement("insert into taxonomy (name,uri) values(?,?)");
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.setString(1, t.getName());
            statement.setString(2, t.getUri());
            statement.executeUpdate();
            success = true;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            Disconnect();
        }
        
        return success;
    }
    
    /**
     * Deletes a taxonomy from the local data store.
     *
     * @param id
     * @return
     */
    public boolean DeleteTaxonomy(String uri) {
        boolean success = false;
        PreparedStatement statement;
        Connect();
        try {
            statement = connection.prepareStatement("delete from taxonomy where uri = ?");
            statement.setQueryTimeout(30);
            statement.setString(1, uri);
            statement.executeUpdate();
            success = true;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            Disconnect();
        }
        
        return success;
    }
}
