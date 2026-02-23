package org.example.services;

import org.example.utils.MyDataBase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServiceClient {

    public String getFullNameById(int clientId) {
        // on teste les 2 cas courants : id / id_client
        String full = queryFullName("SELECT nom, prenom FROM client WHERE id = ?", clientId);
        if (!full.equals("Client")) return full;

        return queryFullName("SELECT nom, prenom FROM client WHERE id_client = ?", clientId);
    }

    private String queryFullName(String sql, int clientId) {
        Connection cn = resolveConnection();
        if (cn == null) return "Client";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String out = ((nom == null ? "" : nom) + " " + (prenom == null ? "" : prenom)).trim();
                    return out.isEmpty() ? "Client" : out;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Client";
    }

    /**
     * Récupère une java.sql.Connection depuis MyDataBase,
     * peu importe le nom exact (getCnx/getConnection/field cnx/connection...).
     * => évite l’erreur "Cannot resolve method ..."
     */
    private Connection resolveConnection() {
        try {
            Class<?> dbClass = MyDataBase.class;

            // 1) Essayer un singleton: MyDataBase.getInstance()
            Object instance = null;
            try {
                Method getInstance = dbClass.getMethod("getInstance");
                instance = getInstance.invoke(null);
            } catch (NoSuchMethodException ignored) {}

            // 2) Essayer méthodes sur instance
            if (instance != null) {
                Connection c = tryMethods(instance, "getConnection", "getCnx", "getCon", "getConn", "getCn");
                if (c != null) return c;

                Connection f = tryFields(instance, "cnx", "connection", "conn", "cn");
                if (f != null) return f;
            }

            // 3) Essayer méthodes statiques
            Connection stat = tryStaticMethods(dbClass, "getConnection", "getCnx", "getCon", "getConn", "getCn");
            if (stat != null) return stat;

            // 4) Essayer champs statiques
            Connection sf = tryStaticFields(dbClass, "cnx", "connection", "conn", "cn");
            if (sf != null) return sf;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Connection tryMethods(Object instance, String... methodNames) {
        for (String name : methodNames) {
            try {
                Method m = instance.getClass().getMethod(name);
                Object val = m.invoke(instance);
                if (val instanceof Connection c) return c;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Connection tryFields(Object instance, String... fieldNames) {
        for (String name : fieldNames) {
            try {
                Field f = instance.getClass().getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(instance);
                if (val instanceof Connection c) return c;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Connection tryStaticMethods(Class<?> cls, String... methodNames) {
        for (String name : methodNames) {
            try {
                Method m = cls.getMethod(name);
                Object val = m.invoke(null);
                if (val instanceof Connection c) return c;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Connection tryStaticFields(Class<?> cls, String... fieldNames) {
        for (String name : fieldNames) {
            try {
                Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(null);
                if (val instanceof Connection c) return c;
            } catch (Exception ignored) {}
        }
        return null;
    }
}