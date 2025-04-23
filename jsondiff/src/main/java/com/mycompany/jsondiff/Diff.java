
package com.mycompany.jsondiff;


public class Diff {
    
    private int id;
    private String op;
    private String path;
    private String value;
    private String name;
    private String type;
    private String ultimoPath;
    private int versao;
    
    
    public Diff(){}
    
    public Diff(int id, String op, String path, String value, String name, String type, String ultimoPath){
        this.id = id;
        this.op = op;
        this.path = path;
        this.value = value;
        this.name = name;
        this.type = type;
        this.ultimoPath = ultimoPath;
         this.versao = versao;
    }

     public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getUltimoPath() {
        return ultimoPath;
        
    }
    public void setUltimoPath(String ultimoPath) {
        this.ultimoPath = ultimoPath;
    }
    
      public int getVersao() {
        return versao;
    }

    public void setVersao(int versao) {
        this.versao = versao;
    }
    
}