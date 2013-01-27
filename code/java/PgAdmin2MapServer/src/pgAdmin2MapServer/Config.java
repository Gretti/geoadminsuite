/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer;

import java.util.Map;

/**
 * A singleton storing program arguments after parsing
 *
 * @author nicolas
 */
public class Config {

    private static Config instance;
    public String host = "";
    public String port = "";
    public String database = "";
    public String user = "";
    public String pwd = "";
    public String schema = "";
    public String table = "";
    /**
     * the directory where pgAdmin binary is
     */
    public String binDir = "";

    private Config() {
        this.host = "";
        this.port = "";
        this.database = "";
        this.user = "";
        this.pwd = "";
        this.schema = "";
        this.table = "";
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public void parseArgs(String[] args) {
        if (args.length > 5) {
            this.binDir = args[0].replace("bindir=", "");
            this.host = args[1].replace("host=", "");
            this.port = args[2].replace("port=", "");
            this.database = args[3].replace("database=", "");
            this.user = args[4].replace("user=", "");
            this.pwd = args[5].replace("passwd=", "");

            if (args.length > 6) {
                this.schema = args[6].replace("schema=", "");
                // protection against special PgAdmin values
                if ("Schemas".equals(this.schema) || "$$SCHEMA".equals(this.schema)) {
                    this.schema = "";
                }
            }
            if (args.length > 7) {
                this.table = args[7].replace("table=", "");
                // protection against special PgAdmin values
                if ("Tables".equals(this.table) || "$$TABLE".equals(this.table)) {
                    this.table = "";
                }
            }
        }
    }

    public void parseArgs(Map<String, String> params) {
        this.binDir = params.get("binDir");
        this.host = params.get("host");
        this.port = params.get("port");
        this.database = params.get("database");
        this.user = params.get("user") == null ? "" : params.get("user");
        this.pwd = params.get("pwd") == null ? "" : params.get("pwd");
        this.schema = params.get("schema") == null ? "" : params.get("schema");
        this.table = params.get("table") == null ? "" : params.get("table");
        
        // protection against special PgAdmin values
        if ("Schemas".equals(this.schema) || "$$SCHEMA".equals(this.schema)) {
            this.schema = "";
        }
        if ("Tables".equals(this.table) || "$$TABLE".equals(this.table)) {
            this.table = "";
        }

    }

    public String toString() {
        return "bindir= " + binDir + " host=" + host + " port=" + port + " database="
                + database + " user=" + user + " passwd=" + pwd + " schema=" + schema + " table=" + table;
    }

    public void updateParams(String newParams) {
        Pg2MS.log("updateParams: " + newParams);
    }
}
